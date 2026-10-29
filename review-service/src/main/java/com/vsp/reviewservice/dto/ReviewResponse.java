package com.vsp.reviewservice.dto;

import com.vsp.reviewservice.model.Rating;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private String id;

    private String movieId;

    private String comment;

    private Rating rating;

    private String reviewerId;

    private String reviewerName;

}
