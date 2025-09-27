package com.biddergod.user_service.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;

public class CognitoUserDetails implements UserDetails {

    private final Jwt jwt;
    private final String username;
    private final String email;
    private final String cognitoSub;

    public CognitoUserDetails(Jwt jwt) {
        this.jwt = jwt;
        this.cognitoSub = jwt.getSubject();

        // Handle both access tokens and ID tokens
        String tokenUse = jwt.getClaimAsString("token_use");

        if ("id".equals(tokenUse)) {
            // ID Token - has cognito:username and email
            this.username = jwt.getClaimAsString("cognito:username");
            this.email = jwt.getClaimAsString("email");
        } else {
            // Access Token - username claim contains sub value, email might be null
            this.username = jwt.getClaimAsString("username");
            this.email = jwt.getClaimAsString("email");
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Extract roles/groups from Cognito token if needed
        // For now, return empty collection
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return null; // Not needed for JWT authentication
    }

    @Override
    public String getUsername() {
        return username != null ? username : email;
    }

    public String getEmail() {
        return email;
    }

    public String getCognitoSub() {
        return cognitoSub;
    }

    public String getCognitoUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Jwt getJwt() {
        return jwt;
    }

    // Helper method to get any claim from JWT
    public Object getClaim(String claimName) {
        return jwt.getClaim(claimName);
    }
}