package com.vsp.contentservice.dto;

import com.vsp.contentservice.model.Genre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MovieUpdateResponse {
    private String title;
    private String description;
    private Genre genre;
}
