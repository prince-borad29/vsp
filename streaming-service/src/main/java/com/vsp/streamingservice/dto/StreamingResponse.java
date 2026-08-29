package com.vsp.streamingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreamingResponse {
    private String movieId;
    private String streamingURL;        //pre signed hls master playlist url
    private String quality;             //available qualities
    private long expiresInMinutes;      //URL expiry time
}
