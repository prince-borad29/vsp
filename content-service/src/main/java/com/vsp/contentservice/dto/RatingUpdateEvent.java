package com.vsp.contentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class RatingUpdateEvent {
    private String movieId;
    private double rating;
}

