package com.vsp.contentservice.dto;

import com.vsp.contentservice.model.Genre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MovieUpdateRequest {

    private String id;

    private String title;

    private String description;

    private Genre genre;
}
