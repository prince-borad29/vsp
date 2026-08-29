package com.vsp.encodingservice.service;

import com.vsp.encodingservice.event.VideoUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoEventConsumer {

    private final EncodingService encodingService;

    /*
     * Listen to video.uploaded kafka topic
     * Triggered when video service uploads a raw video to S3
     *
     * FLOW :
     * 1. video service to s3 upload  -> kafka (video.uploaded)
     *                                -> This consumer
     *                                -> EncodingService -> FFmpeg -> S3
     *                                -> kafka (video.encoded)
     */

    @KafkaListener(
            topics = "video.uploaded",
            groupId = "encoding-service-group"
    )
    public void consumeVideoUploadedEvent(VideoUploadedEvent event){
        log.info("Consumed video uploaded event for movie : {} file : {}",
                event.getMovieId(),event.getOriginalFileName());

        try{
            encodingService.encodeVideo(event);
        } catch (Exception e) {
            log.error("failed to process for encoding movie : {} - {}", event.getVideoKey(),e.getMessage());
        }


    }
}
