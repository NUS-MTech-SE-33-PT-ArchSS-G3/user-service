package com.biddergod.user_service.controller;

import com.biddergod.user_service.dto.UserDetailsResponse;
import com.biddergod.user_service.dto.UserProfileUpdateRequest;
import com.biddergod.user_service.entity.User;
import com.biddergod.user_service.security.CognitoUserDetails;
import com.biddergod.user_service.service.IdTokenService;
import com.biddergod.user_service.service.JwtService;
import com.biddergod.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Tag(name = "User Management", description = "Operations for user profile management and authentication")
public class UserController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private IdTokenService idTokenService;

    @Autowired
    private UserService userService;

    /**
     * Get current user information with optional enhancement
     * GET /api/users/me?idToken=<optional_id_token>
     * POST /api/users/me (with idToken in request body)
     * Requires: Authorization: Bearer <cognito_access_token>
     * Optional: idToken parameter for enhanced profile information
     */
    @Operation(summary = "Get current user information", description = "Retrieve current user profile with optional enhancement from ID token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user information"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing access token"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
        @Parameter(description = "Optional ID token for enhanced profile information")
        @RequestParam(required = false) String idToken) {
        return getUserInfo(idToken);
    }

    @PostMapping("/me")
    public ResponseEntity<?> getCurrentUserPost(@RequestBody(required = false) UserProfileUpdateRequest request) {
        String idToken = request != null ? request.getIdToken() : null;
        return getUserInfo(idToken);
    }

    private ResponseEntity<?> getUserInfo(String idToken) {
        try {
            Optional<User> userOpt = jwtService.getCurrentUser();
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found or invalid token");
            }

            User user = userOpt.get();

            // Build base response with user info
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("firstName", user.getFirstName());
            userInfo.put("lastName", user.getLastName());

            // Add access token information
            Optional<CognitoUserDetails> accessTokenDetails = jwtService.getCurrentCognitoUserDetails();
            if (accessTokenDetails.isPresent()) {
                CognitoUserDetails details = accessTokenDetails.get();
                userInfo.put("cognitoSub", details.getCognitoSub());
                userInfo.put("cognitoUsername", details.getCognitoUsername());
                userInfo.put("groups", jwtService.getUserGroups());
            }

            // If idToken is provided, add enhanced information
            if (idToken != null && !idToken.trim().isEmpty()) {
                Optional<CognitoUserDetails> idTokenDetails = idTokenService.parseIdToken(idToken);

                if (idTokenDetails.isPresent()) {
                    IdTokenService.UserProfile idProfile = idTokenService.extractUserProfile(idTokenDetails.get());

                    // Add enhanced ID token information
                    Map<String, Object> enhancedInfo = new HashMap<>();
                    enhancedInfo.put("email", idProfile.getEmail());
                    enhancedInfo.put("email_verified", idProfile.getEmailVerified());
                    enhancedInfo.put("given_name", idProfile.getGivenName());
                    enhancedInfo.put("family_name", idProfile.getFamilyName());
                    enhancedInfo.put("name", idProfile.getName());
                    enhancedInfo.put("token_use", "id");
                    userInfo.put("enhancedProfile", enhancedInfo);

                    // Update user profile if ID token has better info
                    boolean updated = false;
                    if (idProfile.getEmail() != null && !idProfile.getEmail().equals(user.getEmail())) {
                        user.setEmail(idProfile.getEmail());
                        updated = true;
                    }
                    if (idProfile.getGivenName() != null && user.getFirstName() == null) {
                        user.setFirstName(idProfile.getGivenName().toString());
                        updated = true;
                    }
                    if (idProfile.getFamilyName() != null && user.getLastName() == null) {
                        user.setLastName(idProfile.getFamilyName().toString());
                        updated = true;
                    }

                    if (updated) {
                        userInfo.put("profileUpdated", true);
                        // Update the response with new values
                        userInfo.put("firstName", user.getFirstName());
                        userInfo.put("lastName", user.getLastName());
                        userInfo.put("email", user.getEmail());
                    }
                } else {
                    userInfo.put("idTokenError", "Invalid or malformed ID token");
                }
            }

            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving user information: " + e.getMessage());
        }
    }

    /**
     * Get current user's profile (User entity only)
     * GET /api/users/profile
     * Requires: Authorization: Bearer <cognito_access_token>
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        try {
            Optional<User> userOpt = jwtService.getCurrentUser();
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found or invalid token");
            }

            return ResponseEntity.ok(userOpt.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving user profile: " + e.getMessage());
        }
    }

    /**
     * Get current user's Cognito groups/roles
     * GET /api/users/groups
     * Requires: Authorization: Bearer <cognito_access_token>
     */
    @GetMapping("/groups")
    public ResponseEntity<?> getUserGroups() {
        try {
            if (!jwtService.isTokenValid()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid token");
            }

            List<String> groups = jwtService.getUserGroups();
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving user groups: " + e.getMessage());
        }
    }

    /**
     * Get JWT token claims (for debugging)
     * GET /api/users/token-info
     * Requires: Authorization: Bearer <cognito_access_token>
     */
    @GetMapping("/token-info")
    public ResponseEntity<?> getTokenInfo() {
        try {
            Optional<CognitoUserDetails> cognitoDetails = jwtService.getCurrentCognitoUserDetails();
            if (cognitoDetails.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid token");
            }

            CognitoUserDetails details = cognitoDetails.get();
            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("subject", details.getCognitoSub());
            tokenInfo.put("username", details.getCognitoUsername());
            tokenInfo.put("email", details.getEmail());
            tokenInfo.put("groups", jwtService.getUserGroups());

            // Add some common JWT claims
            tokenInfo.put("issuer", details.getClaim("iss"));
            tokenInfo.put("audience", details.getClaim("aud"));
            tokenInfo.put("tokenUse", details.getClaim("token_use"));
            tokenInfo.put("clientId", details.getClaim("client_id"));

            return ResponseEntity.ok(tokenInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving token information: " + e.getMessage());
        }
    }

    /**
     * Update user profile with ID token data
     * PUT /api/users/profile
     * Requires: Authorization: Bearer <cognito_access_token>
     * Required: ID token in request body for profile update
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody UserProfileUpdateRequest request) {
        try {
            // Get user from access token (authorization)
            Optional<User> userOpt = jwtService.getCurrentUser();
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found or invalid access token");
            }

            User user = userOpt.get();

            // Parse ID token for enhanced profile data
            if (request.getIdToken() != null) {
                Optional<CognitoUserDetails> idTokenDetails = idTokenService.parseIdToken(request.getIdToken());

                if (idTokenDetails.isPresent()) {
                    IdTokenService.UserProfile idProfile = idTokenService.extractUserProfile(idTokenDetails.get());

                    // Update user with ID token information
                    if (idProfile.getEmail() != null) {
                        user.setEmail(idProfile.getEmail());
                    }
                    if (idProfile.getGivenName() != null) {
                        user.setFirstName(idProfile.getGivenName().toString());
                    }
                    if (idProfile.getFamilyName() != null) {
                        user.setLastName(idProfile.getFamilyName().toString());
                    }
                } else {
                    return ResponseEntity.badRequest().body("Invalid ID token provided");
                }
            }

            // Update with manual profile data if provided
            if (request.getFirstName() != null) {
                user.setFirstName(request.getFirstName());
            }
            if (request.getLastName() != null) {
                user.setLastName(request.getLastName());
            }

            // Save updated user (you would uncomment this in real implementation)
            // userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "user", user
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating user profile: " + e.getMessage());
        }
    }

    // ========== Public user apis for payment service ==========

    /**
     * Get user details by user ID(s)
     * GET /api/users?id=1 (single user)
     * GET /api/users?id=1,2,3 (multiple users)
     * Public endpoint for other microservices (payment-service, auction-service, etc.)
     */
    @Operation(summary = "Get user details by ID(s)", description = "Retrieve user profile information by one or more user IDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user details"),
        @ApiResponse(responseCode = "400", description = "No IDs provided"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<?> getUsersByIds(@RequestParam List<Long> id) {
        try {
            if (id == null || id.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No user IDs provided"));
            }

            List<UserDetailsResponse> users = id.stream()
                .map(userService::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(UserDetailsResponse::new)
                .toList();

            return ResponseEntity.ok(Map.of(
                "users", users,
                "found", users.size(),
                "requested", id.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error retrieving users", "message", e.getMessage()));
        }
    }
}