package com.vsp.reviewservice.service;

import com.vsp.reviewservice.dto.GetReviewResponse;
import com.vsp.reviewservice.dto.ReviewRequest;
import com.vsp.reviewservice.dto.ReviewResponse;
import com.vsp.reviewservice.event.RatingUpdateEvent;
import com.vsp.reviewservice.model.Rating;
import com.vsp.reviewservice.model.Review;
import com.vsp.reviewservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    private final KafkaTemplate<String, RatingUpdateEvent> kafkaTemplate;

    private final String RATING_UPDATE_TOPIC = "rating.update";

    public List<GetReviewResponse> getReviews(String movieId) {
        return reviewRepository.findByMovieId(movieId)
                .stream()
                .map(this::mapToGetReviewResponse)
                .toList();
    }

    public ReviewResponse addReview(ReviewRequest reviewRequest) {

        Review review = Review.builder()
                .movieId(reviewRequest.getMovieId())
                .comment(reviewRequest.getComment())
                .rating(reviewRequest.getRating().getValue())
                .reviewerName(reviewRequest.getReviewerName())
                .reviewerId(reviewRequest.getReviewerId())
                .build();

        Review saved = reviewRepository.save(review);

        //Publish rating.update event for content-service
        RatingUpdateEvent event = RatingUpdateEvent.builder()
                .movieId(saved.getMovieId())
                .rating(reviewRepository.findAvgRating(saved.getMovieId()))
                .build();

        kafkaTemplate.send(RATING_UPDATE_TOPIC,saved.getMovieId(),event);

        return mapToResponse(saved);
    }

    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse response = ReviewResponse.builder()
                .id(review.getId())
                .comment(review.getComment())
                .movieId(review.getMovieId())
                .rating(Rating.fromValue(review.getRating()))
                .reviewerName(review.getReviewerName())
                .reviewerId(review.getReviewerId())
                .build();

        return response;
    }

    private GetReviewResponse mapToGetReviewResponse(Review review) {
        GetReviewResponse response = GetReviewResponse.builder()
                .id(review.getId())
                .comment(review.getComment())
                .rating(Rating.fromValue(review.getRating()))
                .reviewerName(review.getReviewerName())
                .posted_at(review.getCreated_at())
                .build();

        return response;
    }

    public void deleteReview(String reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}