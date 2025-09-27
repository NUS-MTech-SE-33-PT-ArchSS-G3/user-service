package com.biddergod.user_service.controller;

import com.biddergod.user_service.dto.ReputationResponse;
import com.biddergod.user_service.entity.User;
import com.biddergod.user_service.repository.UserRepository;
import com.biddergod.user_service.service.ReputationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reputation")
@CrossOrigin(origins = "*")
@Tag(name = "Reputation Management", description = "Operations for managing user reputation and trust levels")
public class ReputationController {

    @Autowired
    private ReputationService reputationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get reputation details for a specific user
     * GET /api/reputation/user/{userId}
     */
    @Operation(summary = "Get user reputation", description = "Retrieve reputation details for a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved reputation information"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<ReputationResponse> getUserReputation(
        @Parameter(description = "User ID to get reputation for") @PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    ReputationResponse response = buildReputationResponse(user);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get users with high reputation (score >= 500)
     * GET /api/reputation/top-users?minScore=500
     */
    @GetMapping("/top-users")
    public ResponseEntity<List<ReputationResponse>> getTopUsers(@RequestParam(defaultValue = "500") Integer minScore) {
        List<User> topUsers = userRepository.findUsersWithHighReputation(minScore);

        List<ReputationResponse> responses = topUsers.stream()
                .map(this::buildReputationResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Manually recalculate reputation for a user
     * POST /api/reputation/user/{userId}/recalculate
     */
    @PostMapping("/user/{userId}/recalculate")
    public ResponseEntity<ReputationResponse> recalculateUserReputation(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    reputationService.updateUserReputation(user);
                    ReputationResponse response = buildReputationResponse(user);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Batch recalculate all user reputations (admin function)
     * POST /api/reputation/recalculate-all
     */
    @PostMapping("/recalculate-all")
    public ResponseEntity<String> recalculateAllReputations() {
        reputationService.recalculateAllReputations();
        return ResponseEntity.ok("Reputation recalculation completed for all users");
    }


    /**
     * Check if user is eligible for premium features
     * GET /api/reputation/user/{userId}/premium-eligible
     */
    @GetMapping("/user/{userId}/premium-eligible")
    public ResponseEntity<Boolean> isPremiumEligible(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    boolean eligible = reputationService.isPremiumEligible(user);
                    return ResponseEntity.ok(eligible);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Helper method to build reputation response
     */
    private ReputationResponse buildReputationResponse(User user) {
        return new ReputationResponse(
                user.getId(),
                user.getUsername(),
                user.getReputationScore(),
                user.getAverageRating(),
                user.getTotalReviews(),
                reputationService.getUserReputationPercentile(user),
                reputationService.isPremiumEligible(user)
        );
    }
}