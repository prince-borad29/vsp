package com.vsp.reviewservice.repository;

import com.vsp.reviewservice.dto.GetReviewResponse;
import com.vsp.reviewservice.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review,String> {

    List<Review> findByMovieId(String movieId);

    @Query("SELECT ROUND(AVG(r.rating), 2) FROM Review r WHERE r.movieId = :movieId")
    Double findAvgRating(@Param("movieId") String movieId);
}
