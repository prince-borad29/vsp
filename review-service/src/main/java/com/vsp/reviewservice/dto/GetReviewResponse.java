package com.vsp.reviewservice.dto;

import com.vsp.reviewservice.model.Rating;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetReviewResponse {

    private String id;

    private String reviewerName;

    private String comment;

    private Rating rating;

    private Timestamp posted_at;
}
