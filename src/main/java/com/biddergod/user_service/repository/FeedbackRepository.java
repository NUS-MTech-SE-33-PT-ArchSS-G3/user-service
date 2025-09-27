package com.biddergod.user_service.repository;

import com.biddergod.user_service.entity.Feedback;
import com.biddergod.user_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByRevieweeOrderByCreatedAtDesc(User reviewee);

    Page<Feedback> findByRevieweeOrderByCreatedAtDesc(User reviewee, Pageable pageable);

    List<Feedback> findByReviewerOrderByCreatedAtDesc(User reviewer);

    Optional<Feedback> findByReviewerAndRevieweeAndTransactionId(User reviewer, User reviewee, String transactionId);

    boolean existsByReviewerAndRevieweeAndTransactionId(User reviewer, User reviewee, String transactionId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.reviewee = :user AND f.isVerified = true")
    Double getAverageRatingForUser(@Param("user") User user);

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.reviewee = :user AND f.isVerified = true")
    Long getTotalReviewsForUser(@Param("user") User user);

    @Query("SELECT f FROM Feedback f WHERE f.reviewee = :user AND f.rating >= :minRating ORDER BY f.createdAt DESC")
    List<Feedback> findPositiveFeedbackForUser(@Param("user") User user, @Param("minRating") Integer minRating);
}