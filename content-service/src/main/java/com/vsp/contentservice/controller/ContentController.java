package com.vsp.contentservice.controller;

import com.vsp.contentservice.dto.MovieRequest;
import com.vsp.contentservice.dto.MovieResponse;
import com.vsp.contentservice.model.Genre;
import com.vsp.contentservice.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/movies")
@Slf4j
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    //Add Movie to catalog
    @PostMapping
    public ResponseEntity<MovieResponse>  addMovie(@Valid @RequestBody MovieRequest movieRequest){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contentService.addMovie(movieRequest));
    }

    //Get all movies
    @GetMapping
    public ResponseEntity<List<MovieResponse>> getAllMovies(){
        return ResponseEntity.ok(contentService.getAllMovies());
    }

    //movies by genre
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<MovieResponse>> getMovieByGenre(@PathVariable Genre genre){
        return ResponseEntity.ok(contentService.getMoviesByGenre(genre));
    }

    //movies by id
    @GetMapping("/{movieId}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable String movieId){
        return ResponseEntity.ok(contentService.getMovieById(movieId));
    }

    //search movies
    @GetMapping("/search")
    public ResponseEntity<List<MovieResponse>> searchMovies(
            @RequestParam String title){
        return ResponseEntity.ok(contentService.searchMovies(title));
    }
}
