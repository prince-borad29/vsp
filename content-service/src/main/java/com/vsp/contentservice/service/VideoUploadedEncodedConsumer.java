package com.vsp.contentservice.service;

import com.vsp.contentservice.model.VideoStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoUploadedEncodedConsumer {

    private final ContentService contentService;

    @KafkaListener(
            topics = "video.uploaded"
    )
    public void consumerVideoUploadedEvent(
            @Payload Map<String,Object> payload
    ){
        String movieId = payload.get("movieId").toString();
        String videoKey = payload.get("videoKey").toString();

        log.info("Video uploaded for movie : {} - key : {}",movieId,videoKey);
        contentService.updateVideoKey(movieId,videoKey);
    }

    @KafkaListener(
            topics = "video.encoded"
    )
    public void consumerVideoEncodedEvent(
            @Payload Map<String,Object> payload
    ){
        String movieId = payload.get("movieId").toString();
        String hlsUrl = payload.get("hlsUrl").toString();
        boolean success = (Boolean) payload.get("success");

        if (success){
            contentService.updateHlsUrl(movieId,hlsUrl);
        }else{
            String error = payload.get("errorMessage").toString();

            contentService.updateVideoStatus(movieId, VideoStatus.FAILED);
        }

    }
}
