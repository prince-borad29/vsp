package com.vsp.encodingservice.service;

import com.vsp.encodingservice.event.VideoEncodedEvent;
import com.vsp.encodingservice.event.VideoUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EncodingService {

    private final S3Client s3Client;
    private final KafkaTemplate<String, VideoEncodedEvent> kafkaTemplate;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    @Value("${encoding.base-path}")
    private String basePath;

    private static final String VIDEO_ENCODED_TOPIC = "video.encoded";

    private static final List<int[]> VIDEO_QUALITIES = Arrays.asList(
            new int[]{1920, 5000, 1080},  // 1080p - 5000k bitrate
            new int[]{1280, 2800, 720},   // 720p - 2800k bitrate
            new int[]{854, 1200, 480},    // 480p - 1200k bitrate
            new int[]{640, 800, 360}      // 360p - 800k bitrate
    );

    public void encodeVideo(VideoUploadedEvent event) {
        log.info("Starting encoding platform for movie: {}", event.getMovieId());

        String jobPath = basePath + "/" + event.getMovieId();

        try {
            // Create temp directories
            Files.createDirectories(Paths.get(jobPath));
            Files.createDirectories(Paths.get(jobPath + "/encoded"));

            // Download video from s3
            String localVideoPath = jobPath + "/raw_video.mp4";
            downloadFromS3(event.getVideoKey(), localVideoPath);
            log.info("raw video downloaded to {}", localVideoPath);

            // ==========================================
            // NEW: GENERATE THUMBNAIL IF REQUESTED
            // ==========================================
            if (event.isGenerateThumbnail()) {
                log.info("Auto-generating thumbnail for movie: {}", event.getMovieId());
                generateAndUploadThumbnail(localVideoPath, event.getMovieId(), jobPath);
            } else {
                log.info("Custom thumbnail was provided, skipping auto-generation.");
            }

            // encode to multiple qualities and generate hls
            for (int[] quailities : VIDEO_QUALITIES) {
                int width = quailities[0];
                int bitrate = quailities[1];
                int height = quailities[2];

                String qualityDir = jobPath + "/encoded/" + height + "p";
                Files.createDirectories(Paths.get(qualityDir));

                encodeToHLS(localVideoPath, qualityDir, width, height, bitrate);
                log.info("Encoded {}p successfully", height);
            }

            // generate master playlist
            String masterPlaylistPath = jobPath + "/encoded/master.m3u8";
            generateMasterPlaylist(masterPlaylistPath);
            log.info("master playlist generated");

            // upload all resources file back to S3
            String encodedPrefix = "encoded/" + event.getMovieId() + "/";
            uploadEncodedFileToS3(jobPath + "/encoded", encodedPrefix);
            log.info("All encoded files uploaded to s3");

            // publish video encoded event
            String masterPlaylistKey = encodedPrefix + "master.m3u8";
            String hlsUrl = "https://" + bucketName + ".s3.amazonaws.com/" + masterPlaylistKey;

            VideoEncodedEvent videoEncodedEvent = new VideoEncodedEvent(
                    event.getMovieId(),
                    hlsUrl,
                    masterPlaylistKey,
                    true,
                    null
            );

            kafkaTemplate.send(VIDEO_ENCODED_TOPIC, event.getMovieId(), videoEncodedEvent);
            log.info("Video encoded event published for movie : {}", event.getMovieId());

        } catch (Exception e) {
            log.error("encoding failed for movie : {} \nError : {}", event.getMovieId(), e.getMessage());

            VideoEncodedEvent failureEvent = new VideoEncodedEvent(
                    event.getMovieId(),
                    null,
                    null,
                    false,
                    e.getMessage()
            );

            kafkaTemplate.send(VIDEO_ENCODED_TOPIC, event.getMovieId(), failureEvent);
        } finally {
            cleanUpTempFiles(jobPath);
        }
    }

    /**
     * Extracts 1 frame at the 1-second mark and uploads it directly to S3.
     */
    private void generateAndUploadThumbnail(String inputVideoPath, String movieId, String jobPath) {
        String localThumbPath = jobPath + "/thumbnail.jpg";

        List<String> command = Arrays.asList(
                ffmpegPath,
                "-i", inputVideoPath,
                "-ss", "00:00:01.000",  // 1 second in (avoids black screens at 00:00:00)
                "-vframes", "1",        // Extract exactly 1 frame
                localThumbPath
        );

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                String s3Key = "encoded/" + movieId + "/thumbnail.jpg";
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .contentType("image/jpeg")
                        .build();

                s3Client.putObject(putObjectRequest, RequestBody.fromFile(new File(localThumbPath)));
                log.info("Successfully generated and uploaded thumbnail to S3: {}", s3Key);
            } else {
                log.error("FFmpeg thumbnail generation failed with exit code: {}", exitCode);
            }
        } catch (Exception e) {
            log.error("Failed to generate thumbnail: {}", e.getMessage());
        }
    }

    private void downloadFromS3(String s3Key, String localPath) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        s3Client.getObject(getObjectRequest, Paths.get(localPath));
    }

    private void encodeToHLS(String inputPath, String outputDir, int width, int bitrate, int height)
            throws IOException, InterruptedException {
        String playlistPath = outputDir + "/playlist.m3u8";
        String segmentPattern = outputDir + "/segment_%03d.ts";

        List<String> command = Arrays.asList(
                ffmpegPath,
                "-i", inputPath,
                "-vf", "scale=" + width + ":" + height,
                "-c:v", "libx264",
                "-b:v", bitrate + "k",
                "-c:a", "aac",
                "-b:a", "128k",
                "-hls_time", "10",
                "-hls_list_size", "0",
                "-hls_segment_filename", segmentPattern,
                "-f", "hls",
                playlistPath
        );

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        processBuilder.inheritIO();
        Process process = processBuilder.start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg encoding failed with exit code: " + exitCode);
        }
    }

    private void generateMasterPlaylist(String masterPlaylistPath) throws IOException {
        StringBuilder master = new StringBuilder();
        master.append("#EXTM3U\n");
        master.append("#EXT-X-VERSION:3\n\n");

        int[][] qualities = {
                {1920, 5000, 1080},
                {1280, 2800, 720},
                {854, 1200, 480},
                {640, 800, 360}
        };

        for (int[] q : qualities) {
            int width = q[0];
            int bitrate = q[1];
            int height = q[2];

            master.append("#EXT-X-STREAM-INF:BANDWIDTH=")
                    .append(bitrate * 1000)
                    .append(",Resolution=").append(width).append("x").append(height)
                    .append(",CODECS=\"avc1.42e01e,mp4a.40.2\"\n");
            master.append(height).append("p/playlist.m3u8\n\n");
        }

        Files.writeString(Paths.get(masterPlaylistPath), master.toString());
    }

    private void uploadEncodedFileToS3(String localDir, String s3Prefix) {
        File directory = new File(localDir);
        uploadDirectoryToS3(directory, localDir, s3Prefix);
    }

    private void uploadDirectoryToS3(File dir, String baseDir, String s3Prefix) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                uploadDirectoryToS3(file, baseDir, s3Prefix);
            } else {
                String relativePath = file.getAbsolutePath()
                        .substring(baseDir.length() + 1)
                        .replace("\\", "/");

                String s3Key = s3Prefix + relativePath;

                String contentType = file.getName().endsWith(".m3u8")
                        ? "application/x-mpegURL"
                        : (file.getName().endsWith(".jpg") ? "image/jpeg" : "video/MP2T");

                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .contentType(contentType)
                        .build();

                s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
                log.debug("Uploaded : {}", s3Key);
            }
        }
    }

    private void cleanUpTempFiles(String jobPath) {
        try {
            Path dirPath = Path.of(jobPath);

            if (Files.exists(dirPath)) {
                Files.walk(dirPath)
                        .sorted(java.util.Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);

                log.info("temp files are cleaned up for job : {}", jobPath);
            }
        } catch (IOException e) {
            log.warn("failed to cleanup temp files: {}", e.getMessage());
        }
    }
}