package com.biddergod.user_service.service;

import com.biddergod.user_service.entity.Feedback;
import com.biddergod.user_service.entity.User;
import com.biddergod.user_service.repository.FeedbackRepository;
import com.biddergod.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReputationService reputationService;

    /**
     * Submit feedback for a user after a transaction
     */
    public Feedback submitFeedback(Long reviewerId, Long revieweeId, Integer rating,
                                 String comment, String transactionId, Feedback.FeedbackType feedbackType) {

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));
        User reviewee = userRepository.findById(revieweeId)
                .orElseThrow(() -> new RuntimeException("Reviewee not found"));

        // Check if feedback already exists for this transaction
        if (feedbackRepository.existsByReviewerAndRevieweeAndTransactionId(reviewer, reviewee, transactionId)) {
            throw new RuntimeException("Feedback already submitted for this transaction");
        }

        // Prevent self-review
        if (reviewerId.equals(revieweeId)) {
            throw new RuntimeException("Cannot review yourself");
        }

        Feedback feedback = new Feedback(reviewer, reviewee, rating, comment, transactionId, feedbackType);
        feedback.setIsVerified(true); // In a real system, this would be validated against actual transactions

        Feedback savedFeedback = feedbackRepository.save(feedback);

        // Update reviewee's reputation
        reputationService.updateUserReputation(reviewee);

        return savedFeedback;
    }

    /**
     * Get paginated feedback for a user
     */
    public Page<Feedback> getFeedbackForUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return feedbackRepository.findByRevieweeOrderByCreatedAtDesc(user, pageable);
    }

    /**
     * Get feedback given by a user
     */
    public List<Feedback> getFeedbackByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return feedbackRepository.findByReviewerOrderByCreatedAtDesc(user);
    }

    /**
     * Check if feedback can be submitted (transaction validation would go here)
     */
    public boolean canSubmitFeedback(Long reviewerId, Long revieweeId, String transactionId) {
        // 1. Transaction exists and is completed
        // 2. Reviewer was part of the transaction
        // 3. No existing feedback for this transaction

        return !feedbackRepository.existsByReviewerAndRevieweeAndTransactionId(
                userRepository.findById(reviewerId).orElse(null),
                userRepository.findById(revieweeId).orElse(null),
                transactionId
        );
    }

    /**
     * Verify feedback (admin function)
     */
    public Feedback verifyFeedback(Long feedbackId, boolean verified) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

        feedback.setIsVerified(verified);
        Feedback savedFeedback = feedbackRepository.save(feedback);

        // Recalculate reputation after verification status change
        reputationService.updateUserReputation(feedback.getReviewee());

        return savedFeedback;
    }
}