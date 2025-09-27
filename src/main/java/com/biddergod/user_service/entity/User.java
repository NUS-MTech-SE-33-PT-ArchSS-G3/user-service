package com.biddergod.user_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    private String username;

    @Email
    @NotBlank
    @Size(max = 100)
    private String email;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Column
    private Double averageRating = 0.0;

    @Column
    private Integer totalReviews = 0;

    @Column
    private Integer reputationScore = 0;

    @OneToMany(mappedBy = "reviewee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Feedback> receivedFeedback;

    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Feedback> givenFeedback;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors
    public User() {}

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }

    public Integer getReputationScore() { return reputationScore; }
    public void setReputationScore(Integer reputationScore) { this.reputationScore = reputationScore; }

    public List<Feedback> getReceivedFeedback() { return receivedFeedback; }
    public void setReceivedFeedback(List<Feedback> receivedFeedback) { this.receivedFeedback = receivedFeedback; }

    public List<Feedback> getGivenFeedback() { return givenFeedback; }
    public void setGivenFeedback(List<Feedback> givenFeedback) { this.givenFeedback = givenFeedback; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods for reputation
    @JsonIgnore
    public String getTrustLevel() {
        if (reputationScore >= 1000) return "EXCELLENT";
        if (reputationScore >= 500) return "GOOD";
        if (reputationScore >= 100) return "FAIR";
        return "NEW";
    }
}