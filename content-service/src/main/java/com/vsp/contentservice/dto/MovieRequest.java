package com.vsp.contentservice.dto;

import com.vsp.contentservice.model.Genre;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieRequest {

    @NotBlank(message = "Title is required")
    private String title;
    private String description;
    @NotBlank(message = "Genre is required")
    private Genre genre;

    private String director;
    private String cast;
    private int releaseYear;

    private int rating;
    private String thumbnailUrl;
    private int durationInMinutes;
}
