package com.biddergod.user_service.dto;

public class ReputationResponse {

    private Long userId;
    private String username;
    private Integer reputationScore;
    private Double averageRating;
    private Integer totalReviews;
    private Double reputationPercentile;
    private Boolean isPremiumEligible;

    // Constructors
    public ReputationResponse() {}

    public ReputationResponse(Long userId, String username, Integer reputationScore,
                             Double averageRating, Integer totalReviews,
                             Double reputationPercentile, Boolean isPremiumEligible) {
        this.userId = userId;
        this.username = username;
        this.reputationScore = reputationScore;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.reputationPercentile = reputationPercentile;
        this.isPremiumEligible = isPremiumEligible;
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getReputationScore() { return reputationScore; }
    public void setReputationScore(Integer reputationScore) { this.reputationScore = reputationScore; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }


    public Double getReputationPercentile() { return reputationPercentile; }
    public void setReputationPercentile(Double reputationPercentile) { this.reputationPercentile = reputationPercentile; }

    public Boolean getIsPremiumEligible() { return isPremiumEligible; }
    public void setIsPremiumEligible(Boolean isPremiumEligible) { this.isPremiumEligible = isPremiumEligible; }
}