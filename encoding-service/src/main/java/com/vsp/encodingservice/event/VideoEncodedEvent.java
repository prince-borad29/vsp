package com.vsp.encodingservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

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
