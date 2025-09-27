package com.biddergod.user_service.repository;

import com.biddergod.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.reputationScore >= :minScore ORDER BY u.reputationScore DESC")
    List<User> findUsersWithHighReputation(@Param("minScore") Integer minScore);

    @Query("SELECT u FROM User u WHERE u.averageRating >= :minRating AND u.totalReviews >= :minReviews ORDER BY u.averageRating DESC, u.totalReviews DESC")
    List<User> findTrustedUsers(@Param("minRating") Double minRating, @Param("minReviews") Integer minReviews);
}