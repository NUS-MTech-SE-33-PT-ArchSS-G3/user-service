package com.biddergod.user_service.controller;

import com.biddergod.user_service.dto.FeedbackRequest;
import com.biddergod.user_service.entity.Feedback;
import com.biddergod.user_service.entity.User;
import com.biddergod.user_service.service.FeedbackService;
import com.biddergod.user_service.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*")
@Tag(name = "Feedback Management", description = "Operations for managing user feedback and reviews")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private JwtService jwtService;

    /**
     * Submit feedback for a user
     * POST /api/feedback
     * Requires: Authorization: Bearer <cognito_access_token>
     */
    @Operation(summary = "Submit feedback", description = "Submit feedback/review for another user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Feedback submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing access token")
    })
    @SecurityRequirement(name = "bearer-jwt")
    @PostMapping
    public ResponseEntity<?> submitFeedback(@Valid @RequestBody FeedbackRequest request) {
        try {
            // Get current user from JWT token
            Optional<User> currentUserOpt = jwtService.getCurrentUser();
            if (currentUserOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found or invalid token");
            }

            User currentUser = currentUserOpt.get();
            Feedback feedback = feedbackService.submitFeedback(
                currentUser.getId(),
                request.getRevieweeId(),
                request.getRating(),
                request.getComment(),
                request.getTransactionId(),
                request.getFeedbackType()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(feedback);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get feedback received by a user with pagination
     * GET /api/feedback/user/{userId}?page=0&size=20
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Feedback>> getUserFeedback(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            // Enforce reasonable limits
            size = Math.min(size, 100); // Max 100 items per page

            Pageable pageable = PageRequest.of(page, size);
            Page<Feedback> feedback = feedbackService.getFeedbackForUser(userId, pageable);
            return ResponseEntity.ok(feedback);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get feedback given by a user
     * GET /api/feedback/given/{userId}
     */
    @GetMapping("/given/{userId}")
    public ResponseEntity<List<Feedback>> getFeedbackGivenByUser(@PathVariable Long userId) {
        try {
            List<Feedback> feedback = feedbackService.getFeedbackByUser(userId);
            return ResponseEntity.ok(feedback);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Check if feedback can be submitted
     * GET /api/feedback/can-submit?revieweeId=1&transactionId=abc123
     * Requires: Authorization: Bearer <cognito_access_token>
     */
    @GetMapping("/can-submit")
    public ResponseEntity<?> canSubmitFeedback(@RequestParam Long revieweeId,
                                             @RequestParam String transactionId) {
        try {
            // Get current user from JWT token
            Optional<User> currentUserOpt = jwtService.getCurrentUser();
            if (currentUserOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found or invalid token");
            }

            User currentUser = currentUserOpt.get();
            boolean canSubmit = feedbackService.canSubmitFeedback(currentUser.getId(), revieweeId, transactionId);
            return ResponseEntity.ok(canSubmit);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Verify feedback (admin endpoint)
     * PUT /api/feedback/{feedbackId}/verify
     */
    @PutMapping("/{feedbackId}/verify")
    public ResponseEntity<Feedback> verifyFeedback(@PathVariable Long feedbackId,
                                                 @RequestParam boolean verified) {
        try {
            Feedback feedback = feedbackService.verifyFeedback(feedbackId, verified);
            return ResponseEntity.ok(feedback);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}