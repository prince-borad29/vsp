package com.vsp.reviewservice.controller;

import com.vsp.reviewservice.dto.ReviewRequest;
import com.vsp.reviewservice.dto.ReviewResponse;
import com.vsp.reviewservice.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<?> getReviews(@PathVariable String movieId){
        return ResponseEntity.ok().body(reviewService.getReviews(movieId));
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> addReview(@Valid @RequestBody ReviewRequest reviewRequest) {
        return ResponseEntity.ok().body(reviewService.addReview(reviewRequest));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable String reviewId){
         reviewService.deleteReview(reviewId);
         return ResponseEntity.ok().body(null);
    }

}
