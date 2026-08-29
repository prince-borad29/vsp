package com.vsp.streamingservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * consumed from kafka video.encoded topic
 * published by encoding service after ffmpeg processing
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoEncodedEvent {

    private String movieId;
    private String hlsUrl;              // master playlits url for streaming
    private String masterPlaylistKey;   // S3 key if master.m3u8
    private boolean success;
    private String errorMessage;        // if encoding failed
}
