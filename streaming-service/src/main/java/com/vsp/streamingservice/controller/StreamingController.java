package com.vsp.streamingservice.controller;

import com.vsp.streamingservice.dto.StreamingResponse;
import com.vsp.streamingservice.service.StreamingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stream")
@Slf4j
@RequiredArgsConstructor
public class StreamingController {

    private final StreamingService streamingService;

    /**
     * Get Streaming URL for movie
     * Returns presigned hls master playlist url
     *
     * GET /api/v1/stream/{movieId}
     */

    @GetMapping("/{movieId}")
    public ResponseEntity<StreamingResponse> getStreamingUrl(
            @PathVariable String movieId
    ){

        log.info("streaming req for movie : {}",movieId);

        try {
            StreamingResponse response = streamingService
                    .getStreamingUrl(movieId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * signed m3u8 playlist content
     * called by hls player for each quality playlist
     * @param movieId
     * @param path
     * @return
     */
    @GetMapping("/{movieId}/playlist")
    public ResponseEntity<String> getSignedPlaylist(
            @PathVariable String movieId,
            @RequestParam String path
    ){

        String signedPlaylist = streamingService.getSignedPlaylist(movieId,path);

        return ResponseEntity.ok()
                .header("Content-Type","application/x-mpegURL")
                .body(signedPlaylist);

    }

}
