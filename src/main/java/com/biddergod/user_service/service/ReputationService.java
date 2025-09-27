package com.biddergod.user_service.service;

import com.biddergod.user_service.entity.Feedback;
import com.biddergod.user_service.entity.User;
import com.biddergod.user_service.repository.FeedbackRepository;
import com.biddergod.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReputationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    // Reputation scoring weights
    private static final int RATING_WEIGHT = 20;      // Points per star rating
    private static final int VERIFIED_BONUS = 10;     // Bonus for verified transactions
    private static final int VOLUME_MULTIPLIER = 5;   // Multiplier based on transaction volume
    private static final int TIME_DECAY_DAYS = 365;   // Days for time-based decay

    /**
     * Calculate and update user's reputation score based on all feedback
     */
    public void updateUserReputation(User user) {
        List<Feedback> allFeedback = feedbackRepository.findByRevieweeOrderByCreatedAtDesc(user);

        int totalScore = 0;
        int verifiedCount = 0;
        double totalRating = 0;
        int ratingCount = 0;

        for (Feedback feedback : allFeedback) {
            // Base score from rating
            int ratingScore = feedback.getRating() * RATING_WEIGHT;

            // Apply time decay (recent feedback worth more)
            double timeDecayFactor = calculateTimeDecayFactor(feedback.getCreatedAt());
            ratingScore = (int) (ratingScore * timeDecayFactor);

            totalScore += ratingScore;

            // Verified transaction bonus
            if (feedback.getIsVerified()) {
                totalScore += VERIFIED_BONUS;
                verifiedCount++;
            }

            totalRating += feedback.getRating();
            ratingCount++;
        }

        // Volume-based multiplier (more transactions = higher reputation ceiling)
        if (verifiedCount > 10) {
            totalScore = (int) (totalScore * 1.2);  // 20% bonus for high-volume users
        } else if (verifiedCount > 5) {
            totalScore = (int) (totalScore * 1.1);  // 10% bonus for medium-volume users
        }

        // Update user metrics
        user.setReputationScore(Math.max(0, totalScore));
        user.setTotalReviews(ratingCount);
        user.setAverageRating(ratingCount > 0 ? totalRating / ratingCount : 0.0);

        userRepository.save(user);
    }

    /**
     * Calculate time decay factor (recent feedback is weighted more heavily)
     */
    private double calculateTimeDecayFactor(LocalDateTime feedbackDate) {
        long daysDiff = java.time.Duration.between(feedbackDate, LocalDateTime.now()).toDays();

        if (daysDiff <= 30) {
            return 1.0;  // Full weight for recent feedback
        } else if (daysDiff <= 90) {
            return 0.9;  // 90% weight for 1-3 months old
        } else if (daysDiff <= 180) {
            return 0.8;  // 80% weight for 3-6 months old
        } else if (daysDiff <= TIME_DECAY_DAYS) {
            return 0.7;  // 70% weight for 6-12 months old
        } else {
            return 0.5;  // 50% weight for very old feedback
        }
    }

    /**
     * Get user's trust level based on reputation score and metrics
     */
    public String getUserTrustLevel(User user) {
        int score = user.getReputationScore();
        int reviews = user.getTotalReviews();
        double avgRating = user.getAverageRating();

        if (score >= 1000 && reviews >= 20 && avgRating >= 4.5) {
            return "EXCELLENT";
        } else if (score >= 500 && reviews >= 10 && avgRating >= 4.0) {
            return "GOOD";
        } else if (score >= 100 && reviews >= 5 && avgRating >= 3.5) {
            return "FAIR";
        } else if (reviews >= 2) {
            return "BASIC";
        } else {
            return "NEW";
        }
    }

    /**
     * Calculate reputation percentile compared to all users
     */
    public double getUserReputationPercentile(User user) {
        List<User> allUsers = userRepository.findAll();

        long betterCount = allUsers.stream()
                .mapToInt(User::getReputationScore)
                .filter(score -> score > user.getReputationScore())
                .count();

        return ((double)(allUsers.size() - betterCount) / allUsers.size()) * 100;
    }

    /**
     * Check if user is eligible for premium features based on reputation
     */
    public boolean isPremiumEligible(User user) {
        return user.getReputationScore() >= 500 &&
               user.getAverageRating() >= 4.0 &&
               user.getTotalReviews() >= 10;
    }

    /**
     * Recalculate reputation for all users (batch operation)
     */
    @Transactional
    public void recalculateAllReputations() {
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            updateUserReputation(user);
        }
    }
}