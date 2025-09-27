package com.biddergod.user_service.service;

import com.biddergod.user_service.entity.User;
import com.biddergod.user_service.repository.UserRepository;
import com.biddergod.user_service.security.CognitoUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CognitoUserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Find or create user based on Cognito token information
     * This method handles both access tokens and ID tokens
     */
    public User findOrCreateUser(CognitoUserDetails cognitoDetails) {
        String cognitoSub = cognitoDetails.getCognitoSub();
        String email = cognitoDetails.getEmail();
        String cognitoUsername = cognitoDetails.getCognitoUsername();

        // Strategy 1: Find by cognitoSub (most reliable)
        Optional<User> existingUser = userRepository.findByUsername(cognitoSub);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update email if we have it now and it was missing before
            if (email != null && (user.getEmail() == null || user.getEmail().endsWith("@cognito.local"))) {
                user.setEmail(email);
                userRepository.save(user);
            }
            return user;
        }

        // Strategy 2: Find by email if available
        if (email != null) {
            existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                // Update username to cognitoSub for future lookups
                user.setUsername(cognitoSub);
                userRepository.save(user);
                return user;
            }
        }

        // Strategy 3: Create new user
        return createNewUser(cognitoSub, email, cognitoUsername);
    }

    /**
     * Create a new user from Cognito information
     */
    private User createNewUser(String cognitoSub, String email, String cognitoUsername) {
        User newUser = new User();

        // Set username as cognitoSub for consistent lookup
        newUser.setUsername(cognitoSub);

        // Set email - prefer real email, fallback to cognito.local
        if (email != null && !email.isEmpty()) {
            newUser.setEmail(email);
        } else {
            newUser.setEmail(cognitoSub + "@cognito.local");
        }

        // Set display name if we have cognito:username
        if (cognitoUsername != null && !cognitoUsername.equals(cognitoSub)) {
            newUser.setFirstName(cognitoUsername);
        }

        return userRepository.save(newUser);
    }

    /**
     * Update user information from token if needed
     */
    public User updateUserFromToken(User user, CognitoUserDetails cognitoDetails) {
        boolean updated = false;

        String email = cognitoDetails.getEmail();
        if (email != null && !email.equals(user.getEmail())) {
            // Only update if current email is a fallback or null
            if (user.getEmail() == null || user.getEmail().endsWith("@cognito.local")) {
                user.setEmail(email);
                updated = true;
            }
        }

        if (updated) {
            return userRepository.save(user);
        }

        return user;
    }

    /**
     * Get user information with enhanced details
     */
    public UserInfo getUserInfo(User user, CognitoUserDetails cognitoDetails) {
        return new UserInfo(
            user,
            cognitoDetails.getCognitoSub(),
            cognitoDetails.getEmail(),
            cognitoDetails.getCognitoUsername(),
            cognitoDetails.getClaim("token_use"),
            cognitoDetails.getClaim("client_id"),
            cognitoDetails.getClaim("scope")
        );
    }

    /**
     * Enhanced user information combining database and token data
     */
    public static class UserInfo {
        private final User user;
        private final String cognitoSub;
        private final String tokenEmail;
        private final String cognitoUsername;
        private final Object tokenUse;
        private final Object clientId;
        private final Object scope;

        public UserInfo(User user, String cognitoSub, String tokenEmail,
                       String cognitoUsername, Object tokenUse, Object clientId, Object scope) {
            this.user = user;
            this.cognitoSub = cognitoSub;
            this.tokenEmail = tokenEmail;
            this.cognitoUsername = cognitoUsername;
            this.tokenUse = tokenUse;
            this.clientId = clientId;
            this.scope = scope;
        }

        // Getters
        public User getUser() { return user; }
        public String getCognitoSub() { return cognitoSub; }
        public String getTokenEmail() { return tokenEmail; }
        public String getCognitoUsername() { return cognitoUsername; }
        public Object getTokenUse() { return tokenUse; }
        public Object getClientId() { return clientId; }
        public Object getScope() { return scope; }
    }
}