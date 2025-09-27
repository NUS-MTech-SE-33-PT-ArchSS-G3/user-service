package com.biddergod.user_service.service;

import com.biddergod.user_service.entity.User;
import com.biddergod.user_service.repository.UserRepository;
import com.biddergod.user_service.security.CognitoUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class JwtService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CognitoUserService cognitoUserService;

    /**
     * Get the currently authenticated user from JWT token
     */
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            Jwt jwt = jwtToken.getToken();
            CognitoUserDetails userDetails = new CognitoUserDetails(jwt);

            // Use the enhanced CognitoUserService
            User user = cognitoUserService.findOrCreateUser(userDetails);
            return Optional.of(user);
        }

        return Optional.empty();
    }

    /**
     * Get current user ID from JWT token
     */
    public Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(User::getId);
    }

    /**
     * Get Cognito user details from current authentication
     */
    public Optional<CognitoUserDetails> getCurrentCognitoUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            Jwt jwt = jwtToken.getToken();
            return Optional.of(new CognitoUserDetails(jwt));
        }

        return Optional.empty();
    }

    /**
     * Extract user information from JWT and find/create user in database
     */
    private Optional<User> findOrCreateUser(CognitoUserDetails userDetails) {
        String email = userDetails.getEmail();
        String username = userDetails.getCognitoUsername();
        String cognitoSub = userDetails.getCognitoSub();

        // For access tokens, we might not have email, so use cognitoSub as primary identifier

        // First try to find by cognitoSub (stored in username field for consistency)
        Optional<User> existingUser = userRepository.findByUsername(cognitoSub);
        if (existingUser.isPresent()) {
            return existingUser;
        }

        // Then try to find by email if available
        if (email != null) {
            existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                // Update the user with cognitoSub for future lookups
                User user = existingUser.get();
                user.setUsername(cognitoSub);
                userRepository.save(user);
                return Optional.of(user);
            }
        }

        // Create new user using cognitoSub
        User newUser = new User();
        newUser.setUsername(cognitoSub); // Use cognitoSub as username
        newUser.setEmail(email != null ? email : cognitoSub + "@cognito.local"); // Fallback email

        User savedUser = userRepository.save(newUser);
        return Optional.of(savedUser);
    }

    /**
     * Validate JWT token (additional validation if needed)
     */
    public boolean isTokenValid() {
        return getCurrentCognitoUserDetails().isPresent();
    }

    /**
     * Get specific claim from JWT token
     */
    public Optional<Object> getTokenClaim(String claimName) {
        return getCurrentCognitoUserDetails()
                .map(userDetails -> userDetails.getClaim(claimName));
    }

    /**
     * Check if current user has specific role/group
     */
    public boolean hasRole(String role) {
        return getCurrentCognitoUserDetails()
                .map(userDetails -> {
                    // Extract groups/roles from cognito:groups claim
                    Object groups = userDetails.getClaim("cognito:groups");
                    if (groups instanceof java.util.List<?> groupList) {
                        return groupList.contains(role);
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Get user's Cognito groups
     */
    @SuppressWarnings("unchecked")
    public java.util.List<String> getUserGroups() {
        return getCurrentCognitoUserDetails()
                .map(userDetails -> {
                    Object groups = userDetails.getClaim("cognito:groups");
                    if (groups instanceof java.util.List<?>) {
                        return (java.util.List<String>) groups;
                    }
                    return java.util.Collections.<String>emptyList();
                })
                .orElse(java.util.Collections.emptyList());
    }
}