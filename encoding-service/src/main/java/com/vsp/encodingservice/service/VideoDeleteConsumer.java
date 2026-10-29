package com.vsp.encodingservice.service;

import com.vsp.encodingservice.event.MovieDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoDeleteConsumer {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.thumbnail-bucket}")
    private String publicBucket;

    @KafkaListener(
            topics = "video.deleted",
            groupId = "encoding-service-group",
            properties = {
                    "spring.json.value.default.type=com.vsp.encodingservice.event.MovieDeletedEvent"
            }
    )
    public void handleMovieDeleted(MovieDeletedEvent event) {
        String movieId = event.getMovieId();
        log.info("Received cleanup event for deleted movie: {}. Starting S3 batch purge...", movieId);

        try {
            // 1. Delete RAW video (Private Bucket)
            deleteS3Directory(bucketName, "raw/" + movieId + "/");

            // 2. Delete AES Encryption Key (Private Bucket)
            deleteS3Directory(bucketName, "keys/" + movieId + "/");

            // 3. Delete HLS Chunks and Playlists (Private Bucket)
            deleteS3Directory(bucketName, "encoded/" + movieId + "/");

            // 4. Delete Thumbnail (Public Bucket)
            deleteS3Directory(publicBucket, "thumbnails/" + movieId + "/");

            log.info("Successfully purged all S3 data for movie: {}", movieId);
        } catch (Exception e) {
            log.error("Critical error cleaning up S3 for movie: {}. Manual cleanup may be required.", movieId, e);
        }
    }

    /**
     * Helper method to safely delete hundreds/thousands of S3 objects under a specific prefix.
     * Uses AWS SDK v2 Batch Deletion (up to 1000 at a time) to prevent S3 rate limits.
     */
    private void deleteS3Directory(String targetBucket, String prefix) {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(targetBucket)
                .prefix(prefix)
                .build();

        // The paginator handles fetching in batches of up to 1000 objects automatically
        ListObjectsV2Iterable paginator = s3Client.listObjectsV2Paginator(listRequest);

        int deletedCount = 0;

        for (software.amazon.awssdk.services.s3.model.ListObjectsV2Response page : paginator) {

            if (!page.hasContents() || page.contents().isEmpty()) {
                continue;
            }

            // Map the raw S3Objects into ObjectIdentifiers required by the Batch Delete API
            List<ObjectIdentifier> keysToDelete = page.contents().stream()
                    .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key()).build())
                    .collect(Collectors.toList());

            // Fire a SINGLE batch delete request for up to 1000 files
            DeleteObjectsRequest multiObjectDeleteRequest = DeleteObjectsRequest.builder()
                    .bucket(targetBucket)
                    .delete(Delete.builder().objects(keysToDelete).build())
                    .build();

            s3Client.deleteObjects(multiObjectDeleteRequest);
            deletedCount += keysToDelete.size();
        }

        if (deletedCount > 0) {
            log.info("Batch deleted {} objects in s3://{}/{}", deletedCount, targetBucket, prefix);
        }
    }
}