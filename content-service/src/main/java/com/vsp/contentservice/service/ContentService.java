package com.vsp.contentservice.service;

import com.vsp.contentservice.dto.MovieRequest;
import com.vsp.contentservice.dto.MovieResponse;
import com.vsp.contentservice.model.Genre;
import com.vsp.contentservice.model.Movie;
import com.vsp.contentservice.model.VideoStatus;
import com.vsp.contentservice.repository.ContentRepostiory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepostiory contentRepostiory;

    //add a new movie to the catalog , video is not uploaded at this stage
    public MovieResponse addMovie(MovieRequest request){

        log.info("Adding new movie : {}",request.getTitle());

        Movie movie = Movie.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .genre(request.getGenre())
                .director(request.getDirector())
                .cast(request.getCast())
                .releaseYear(request.getReleaseYear())
                .rating(request.getRating())
                .thumbnailUrl(request.getThumbnailUrl())
                .durationInMinutes(request.getDurationInMinutes())
                .videoStatus(VideoStatus.PENDING)
                .build();

        Movie savedMovie = contentRepostiory.save(movie);
        log.info("Movie added with Id : {}",savedMovie.getId());

        return mapToResponse(savedMovie);
    }

    // for converting Movie -> MovieResponse
    private MovieResponse mapToResponse(Movie movie){
        MovieResponse response = MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .genre(movie.getGenre())
                .director(movie.getDirector())
                .cast(movie.getCast())
                .releaseYear(movie.getReleaseYear())
                .rating(movie.getRating())
                .thumbnailUrl(movie.getThumbnailUrl())
                .durationInMinutes(movie.getDurationInMinutes())
                .videoKey(movie.getVideoKey())
                .hlsUrl(movie.getHlsUrl())
                .videoStatus(movie.getVideoStatus())
                .createdAt(movie.getCreatedRAt())
                .build();

        return response;
    }

    // get all movies in the catalog
    public List<MovieResponse> getAllMovies(){
        return contentRepostiory.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // get movie by id
    public MovieResponse getMovieById(String movieId){
        Movie movie = contentRepostiory.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie Not Found: " + movieId ));

        return mapToResponse(movie);
    }

    // get movie by genre
    public List<MovieResponse> getMoviesByGenre(Genre genre){
        return contentRepostiory.findAllByGenre(genre)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // search movies by title
    public List<MovieResponse> searchMovies(String title){
        return contentRepostiory.findAllByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    //update video key
    public void updateVideoKey(String movieId,String videoKey){
        log.info("updating video key for movie : {}",movieId);

        Movie movie = contentRepostiory.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found : "+movieId));

        movie.setVideoKey(videoKey);
        movie.setVideoStatus(VideoStatus.UPLOADED);

        contentRepostiory.save(movie);
    }

    public void updateVideoStatus(String movieId,VideoStatus videoStatus){
        Movie movie = contentRepostiory.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found : "+movieId));

        movie.setVideoStatus(videoStatus);

        contentRepostiory.save(movie);
    }

    // update hlsUrl
    public void updateHlsUrl(String movieId,String hlsUrl){
        log.info("updating hlsUrl for movie : {}",movieId);

        Movie movie = contentRepostiory.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found : "+movieId));

        movie.setHlsUrl(hlsUrl);
        movie.setVideoStatus(VideoStatus.READY);

        contentRepostiory.save(movie);

        log.info("Movie is now ready for streaming : {}",movie);
    }
}
