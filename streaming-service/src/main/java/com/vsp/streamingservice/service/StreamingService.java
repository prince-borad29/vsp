package com.vsp.streamingservice.service;

import com.vsp.streamingservice.dto.ContentResponse;
import com.vsp.streamingservice.dto.StreamingResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import javax.swing.text.AbstractDocument;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamingService {

    private final S3Presigner s3Presigner;
    private final RedisTemplate<String,String> redisTemplate;
    private final S3Client s3Client;
    private final RestClient restClient;

    @Value("${aws.s3.bucket-name}")
    private String bucket_name;

    @Value("${aws.s3.presigned-url-expiry}")
    private long preSignedUrlExpiry;

    //Redis key for caching streaming url
    private final static String STREAMING_URL_CACHE_PREFIX = "streaming:url:";
    private final static String MASTER_PLAYLIST_KEY_PREFIX = "streaming:playlist:";

    /**
     * get streaming url for movie
     *
     * Flow :
     *
     * 1. check redis cache for streaming url
     * 2. If cached - return immediately
     * 3. If not cached - generate new presigned url from s3
     * 4. Cache the url in redis
     * 5. return streaming url
     *
     * Why presigned url ?
     * - S3 bucket is private locker room - videos are not publicly available
     * - Presigned urls give temporary access to it (X minutes)
     * - Prevent unauthorized video downloads
     */

    public StreamingResponse getStreamingUrl(String movieId){
        log.info("Getting Streaming url for movie {}",movieId);

        // Check Redis cache first
        String cacheKey = STREAMING_URL_CACHE_PREFIX + movieId;
        String cachedUrl = redisTemplate.opsForValue().get(cacheKey);

        if (cachedUrl != null) {
            log.info("Returning cached streaming URL for movie: {}", movieId);
            return new StreamingResponse(movieId, cachedUrl,
                    "1080p, 720p, 480p, 360p", preSignedUrlExpiry);
        }

        // Get master playlist key from Redis
        String playlistKey = redisTemplate.opsForValue()
                .get(MASTER_PLAYLIST_KEY_PREFIX + movieId);

        if (playlistKey == null) {
            //if playlistKey not in redis then get it from content-service
            playlistKey = "encoded/" + movieId + "/master.m3u8";

            redisTemplate.opsForValue().set(MASTER_PLAYLIST_KEY_PREFIX + movieId ,playlistKey);
        }

        //generate presigned url from s3
        log.info("generating presigned url movie : {}",movieId);

        String presignedUrl = generatePresignedUrl(playlistKey);

        // cache in redis for 55 minutes
        // 5 minutes less than actual expiry to avoid edge cases
        redisTemplate.opsForValue().set(
                cacheKey,
                presignedUrl,
                55,
                TimeUnit.MINUTES
        );

        log.info("streaming url generated and cached for movie : {}",movieId);

        return new StreamingResponse(
                movieId,
                presignedUrl,
                "1080p, 720p, 480p, 360p",
                preSignedUrlExpiry
        );
    }

    /**
     * generate presigned url for s3 object
     * URL expires after configure time
     */

    private String generatePresignedUrl(String key){
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket_name)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(preSignedUrlExpiry))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    /**
     * this is key method that makes everything secure
     * @param movieId
     * @param path
     * @return
     */
    public String getSignedPlaylist(String movieId,String path){

        //get base path for playlist
        String basePath = path.substring(0,path.lastIndexOf('/') + 1);

        //read m3u8 content from s3
        String m3u8Content = readFromS3(path);

        //rewrite each line that is a segment or playlist reference
        String signedContent = rewriteM3urSignedUrls(
                m3u8Content,basePath);
        return signedContent;
    }

    private String rewriteM3urSignedUrls(String m3u8Content,String basePath){
        StringBuilder rewritten = new StringBuilder();

        for (String line : m3u8Content.split("\n")){
            String trimmed = line.trim();

            if(trimmed.isEmpty() || trimmed.startsWith("#")){
                rewritten.append(line).append("\n");
                continue;
            }

            //this is a segment or playlist reference
            //build full s3 key and sign it
            String fullKey = basePath + trimmed;
            String signedUrl = generatePresignedUrl(fullKey);

            rewritten.append(signedUrl).append("\n");
        }

        return rewritten.toString();
    }

    /**
     * read file content from s3
     */
    private String readFromS3(String s3Key){
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket_name)
                .key(s3Key)
                .build();

        ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);

        return new BufferedReader(new InputStreamReader(response))
                .lines()
                .collect(Collectors.joining("\n"));
    }


    /**
     * invalidate cached streaming url
     * called when video is re-encoded or updated
     */

    public void invalidateCache(String movieId){
        String cacheKey = STREAMING_URL_CACHE_PREFIX + movieId;
        redisTemplate.delete(cacheKey);
        log.info("Streaming url cache invalidated for movie : {}",movieId);
    }

}
