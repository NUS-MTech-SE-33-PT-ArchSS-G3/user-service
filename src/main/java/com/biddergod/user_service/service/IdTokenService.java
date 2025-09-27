package com.biddergod.user_service.service;

import com.biddergod.user_service.security.CognitoUserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IdTokenService {

    @Autowired
    private JwtDecoder jwtDecoder;

    /**
     * Parse and validate ID token from request body
     */
    public Optional<CognitoUserDetails> parseIdToken(String idToken) {
        if (idToken == null || idToken.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            // Remove "Bearer " prefix if present
            String cleanToken = idToken.startsWith("Bearer ") ?
                idToken.substring(7) : idToken;

            // Decode and validate the ID token
            Jwt jwt = jwtDecoder.decode(cleanToken);

            // Verify it's actually an ID token
            String tokenUse = jwt.getClaimAsString("token_use");
            if (!"id".equals(tokenUse)) {
                return Optional.empty();
            }

            return Optional.of(new CognitoUserDetails(jwt));

        } catch (Exception e) {
            // Log the error but don't fail the request
            System.err.println("Failed to parse ID token: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Extract enhanced user information from ID token
     */
    public UserProfile extractUserProfile(CognitoUserDetails idTokenDetails) {
        return new UserProfile(
            idTokenDetails.getEmail(),
            idTokenDetails.getCognitoUsername(),
            idTokenDetails.getClaim("email_verified"),
            idTokenDetails.getClaim("given_name"),
            idTokenDetails.getClaim("family_name"),
            idTokenDetails.getClaim("name")
        );
    }

    /**
     * Enhanced user profile from ID token
     */
    public static class UserProfile {
        private final String email;
        private final String cognitoUsername;
        private final Object emailVerified;
        private final Object givenName;
        private final Object familyName;
        private final Object name;

        public UserProfile(String email, String cognitoUsername, Object emailVerified,
                          Object givenName, Object familyName, Object name) {
            this.email = email;
            this.cognitoUsername = cognitoUsername;
            this.emailVerified = emailVerified;
            this.givenName = givenName;
            this.familyName = familyName;
            this.name = name;
        }

        // Getters
        public String getEmail() { return email; }
        public String getCognitoUsername() { return cognitoUsername; }
        public Object getEmailVerified() { return emailVerified; }
        public Object getGivenName() { return givenName; }
        public Object getFamilyName() { return familyName; }
        public Object getName() { return name; }
    }
}