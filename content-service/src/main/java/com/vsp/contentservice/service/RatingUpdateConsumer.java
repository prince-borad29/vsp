package com.vsp.contentservice.service;

import com.vsp.contentservice.dto.RatingUpdateEvent;
import com.vsp.contentservice.repository.ContentRepostiory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class RatingUpdateConsumer {

    private final ContentRepostiory contentRepostiory;

    @KafkaListener(
            groupId = "encoding-service-group",
            topics = "rating.update"
    )
    public void handleRaingUpdate(
            @Payload Map<String,Object> payload
            ) {
        log.info("consuming rating.update event");

        String movieId = payload.get("movieId").toString();
        double rating = (double) payload.get("rating");

        contentRepostiory.updateRating(movieId,rating);

        log.info("rating updated for movieId : {} , rating : {}",movieId,rating);
    }
}
