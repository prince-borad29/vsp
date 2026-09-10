package com.vsp.videoservice.service;

import com.vsp.videoservice.event.VideoUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoService {

    private final S3Client s3Client;
    private final KafkaTemplate<String, VideoUploadedEvent> kafkaTemplate;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private static final String VIDEO_UPLOADED_TOPIC = "video.uploaded";

    public String uploadVideo(String movieId, MultipartFile videoFile, MultipartFile thumbnailFile) throws IOException {
        log.info("Starting upload for movie : {} file : {}", movieId, videoFile.getOriginalFilename());

        boolean generateThumbnail = true;

        // 1. Process Custom Thumbnail (If Provided)
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            // Using a static name ensures S3 overwrites the old image on updates
            String thumbnailKey = "encoded/" + movieId + "/thumbnail.jpg";

            PutObjectRequest thumbRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(thumbnailKey)
                    .contentType(thumbnailFile.getContentType())
                    .build();

            s3Client.putObject(thumbRequest, RequestBody.fromBytes(thumbnailFile.getBytes()));
            log.info("Custom thumbnail uploaded to S3, key : {}", thumbnailKey);

            generateThumbnail = false; // Tell encoding service to skip extraction
        }

        // 2. Process Raw Video Upload
        String videoKey = "raw/" + movieId + "/" + UUID.randomUUID() + "_" + videoFile.getOriginalFilename();

        PutObjectRequest videoRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(videoKey)
                .contentType(videoFile.getContentType())
                .contentLength(videoFile.getSize())
                .build();

        s3Client.putObject(videoRequest, RequestBody.fromBytes(videoFile.getBytes()));
        log.info("Video uploaded to S3 successfully , key : {}", videoKey);

        // 3. Publish Kafka Event
        VideoUploadedEvent event = new VideoUploadedEvent(
                movieId,
                videoKey,
                bucketName,
                videoFile.getOriginalFilename(),
                videoFile.getSize(),
                generateThumbnail
        );

        kafkaTemplate.send(VIDEO_UPLOADED_TOPIC, movieId, event);
        log.info("Published video.uploaded for {}. Auto-generate thumbnail: {}", movieId, generateThumbnail);

        return videoKey;
    }
}