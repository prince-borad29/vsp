package com.vsp.contentservice.dto;

import com.vsp.contentservice.model.Genre;
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
public class MovieRequest {

    @NotBlank(message = "Title is required")
    private String title;
    private String description;
    @NotNull(message = "Genre is required")
    private Genre genre;

    private String director;
    private String cast;
    private int releaseYear;

    @Builder.Default
    private double rating = 0.0;
    private String thumbnailUrl;
    private int durationInMinutes;
}
