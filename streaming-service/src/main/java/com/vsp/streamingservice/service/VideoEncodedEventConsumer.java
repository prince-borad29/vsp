package com.vsp.streamingservice.service;

import com.vsp.streamingservice.event.VideoEncodedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoEncodedEventConsumer {

    private final RedisTemplate<String,String> redisTemplate;

    private static final String MASTER_PLAYLIST_KEY_PREFIX = "streaming:playlist:";

    /**
     * listen to video.encoded kafka topic
     * stores master playlist key in redis when encoding is complete
     * this allows streaming service to quickly find the playlist key by movieId
     */
    @KafkaListener(
            topics = "video.encoded",
            groupId = "streaming-service-group"
    )
    public void consumeVideoEncodedEvent(VideoEncodedEvent event){
        log.info("consumed video encoded event for movie : {} , success : {}",event.getMovieId(),event.isSuccess());

        if(event.isSuccess()){
            //store master playlist key in redis
            String cacheKey = MASTER_PLAYLIST_KEY_PREFIX + event.getMovieId();

            redisTemplate.opsForValue().set(cacheKey, event.getMasterPlaylistKey());
            log.info("master playlist key stored in redis for movie : {} ",event.getMovieId());
        }else {
            log.error("encoding failed for movie : {} - {}",
                    event.getMovieId(), event.getErrorMessage());
        }

    }

}
