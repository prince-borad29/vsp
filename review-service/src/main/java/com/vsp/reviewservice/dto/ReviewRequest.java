package com.vsp.reviewservice.dto;

import com.vsp.reviewservice.model.Rating;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {

    @NotBlank
    private String movieId;

    @NotBlank
    private String comment;

    @NotNull
    private Rating rating;

    @NotBlank
    private String reviewerId;

    @NotBlank
    private String reviewerName;

}
