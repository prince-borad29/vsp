package com.vsp.contentservice.repository;

import com.vsp.contentservice.model.Genre;
import com.vsp.contentservice.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContentRepostiory extends JpaRepository<Movie,String> {
    List<Movie> findAllByGenre(Genre genre);
    List<Movie> findAllByTitleContainingIgnoreCase(String title);

    @Query("SELECT m.hlsUrl FROM Movie m WHERE m.id = :movieId")
    Optional<String> findHlsUrlByMovieId(@Param("movieId") String movieId);
}
