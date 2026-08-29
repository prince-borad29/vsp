package com.vsp.videoservice.service;

import com.vsp.videoservice.event.VideoUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.UncheckedIOException;
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

    // uploading video to aws and publishing video update event for kafka

    /**
     * FLOW
     * 1. receive multipart-file
     * 2. generate unique s3 key
     * 3. upload to s3
     * 4. publish videoUploadedEvent to kafka
     * 5. encoding service picks up and start FFmpeg
     */

    public String uploadVideo(String movieId , MultipartFile file) throws IOException {
        log.info("Starting video upload for movie : {} file : {}",movieId,file.getOriginalFilename());

        //Generate unique S3 key for video
        // format : raw/movieId/uuid_filename

        String videoKey = "raw/" + movieId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(videoKey)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

//        s3Client.putObject(putObjectRequest,
//                RequestBody.fromInputStream(file.getInputStream(),file.getSize()));

        // Replace the stream line with this:
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

        log.info("Video uploaded to S3 successfully , key : {}",videoKey);

        //publish kafka event
        //encoding service consume this and start working
        VideoUploadedEvent event = new VideoUploadedEvent(
                movieId,
                videoKey,
                bucketName,
                file.getOriginalFilename(),
                file.getSize()
        );

        kafkaTemplate.send(VIDEO_UPLOADED_TOPIC,movieId,event);
        log.info("Video uploaded event published for movie {}", movieId);

        return videoKey;
    }
}
