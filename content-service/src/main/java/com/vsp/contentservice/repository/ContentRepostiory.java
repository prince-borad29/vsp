package com.vsp.contentservice.repository;

import com.vsp.contentservice.model.Genre;
import com.vsp.contentservice.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentRepostiory extends JpaRepository<Movie,String> {
    List<Movie> findAllByGenre(Genre genre);
    List<Movie> findAllByTitleContainingIgnoreCase(String title);
}
