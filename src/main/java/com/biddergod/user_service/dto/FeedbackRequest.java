package com.biddergod.user_service.dto;

import com.biddergod.user_service.entity.Feedback;
import jakarta.validation.constraints.*;

public class FeedbackRequest {

    @NotNull(message = "Reviewee ID is required")
    private Long revieweeId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String comment;

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @NotNull(message = "Feedback type is required")
    private Feedback.FeedbackType feedbackType;

    // Constructors
    public FeedbackRequest() {}

    public FeedbackRequest(Long revieweeId, Integer rating, String comment,
                          String transactionId, Feedback.FeedbackType feedbackType) {
        this.revieweeId = revieweeId;
        this.rating = rating;
        this.comment = comment;
        this.transactionId = transactionId;
        this.feedbackType = feedbackType;
    }

    // Getters and Setters
    public Long getRevieweeId() { return revieweeId; }
    public void setRevieweeId(Long revieweeId) { this.revieweeId = revieweeId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public Feedback.FeedbackType getFeedbackType() { return feedbackType; }
    public void setFeedbackType(Feedback.FeedbackType feedbackType) { this.feedbackType = feedbackType; }
}