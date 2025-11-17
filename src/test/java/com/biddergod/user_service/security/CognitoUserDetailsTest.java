package com.biddergod.user_service.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CognitoUserDetails Tests")
class CognitoUserDetailsTest {

    @Test
    @DisplayName("Should create CognitoUserDetails from ID token")
    void constructor_IdToken_ExtractsAllFields() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "cognito-sub-123");
        claims.put("token_use", "id");
        claims.put("cognito:username", "testuser");
        claims.put("email", "test@example.com");

        Jwt jwt = createJwt(claims);

        // When
        CognitoUserDetails userDetails = new CognitoUserDetails(jwt);

        // Then
        assertThat(userDetails.getCognitoSub()).isEqualTo("cognito-sub-123");
        assertThat(userDetails.getCognitoUsername()).isEqualTo("testuser");
        assertThat(userDetails.getEmail()).isEqualTo("test@example.com");
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should create CognitoUserDetails from access token")
    void constructor_AccessToken_ExtractsAllFields() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "cognito-sub-456");
        claims.put("token_use", "access");
        claims.put("username", "accessuser");
        claims.put("email", "access@example.com");

        Jwt jwt = createJwt(claims);

        // When
        CognitoUserDetails userDetails = new CognitoUserDetails(jwt);

        // Then
        assertThat(userDetails.getCognitoSub()).isEqualTo("cognito-sub-456");
        assertThat(userDetails.getCognitoUsername()).isEqualTo("accessuser");
        assertThat(userDetails.getEmail()).isEqualTo("access@example.com");
        assertThat(userDetails.getUsername()).isEqualTo("accessuser");
    }

    @Test
    @DisplayName("Should return email when username is null in access token")
    void getUsername_UsernameNull_ReturnsEmail() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "cognito-sub-789");
        claims.put("token_use", "access");
        claims.put("email", "email@example.com");

        Jwt jwt = createJwt(claims);

        // When
        CognitoUserDetails userDetails = new CognitoUserDetails(jwt);

        // Then
        assertThat(userDetails.getUsername()).isEqualTo("email@example.com");
        assertThat(userDetails.getCognitoUsername()).isNull();
    }

    @Test
    @DisplayName("Should handle null email in ID token")
    void constructor_IdTokenNullEmail_HandlesGracefully() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "cognito-sub-100");
        claims.put("token_use", "id");
        claims.put("cognito:username", "nonemailuser");

        Jwt jwt = createJwt(claims);

        // When
        CognitoUserDetails userDetails = new CognitoUserDetails(jwt);

        // Then
        assertThat(userDetails.getCognitoSub()).isEqualTo("cognito-sub-100");
        assertThat(userDetails.getCognitoUsername()).isEqualTo("nonemailuser");
        assertThat(userDetails.getEmail()).isNull();
    }

    @Test
    @DisplayName("Should return empty authorities")
    void getAuthorities_ReturnsEmptyCollection() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "cognito-sub-200");
        claims.put("token_use", "id");

        Jwt jwt = createJwt(claims);
        CognitoUserDetails userDetails = new CognitoUserDetails(jwt);

        // When & Then
        assertThat(userDetails.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("Should return null password")
    void getPassword_ReturnsNull() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "cognito-sub-300");
        claims.put("token_use", "id");

        Jwt jwt = createJwt(claims);
        CognitoUserDetails userDetails = new CognitoUserDetails(jwt);

        // When & Then
        assertThat(userDetails.getPassword()).isNull();
    }

    @Test
    @DisplayName("Should return account as non-expired")
    void isAccountNonExpired_ReturnsTrue() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "cognito-sub-400");
        claims.put("token_use", "id");

        Jwt jwt = createJwt(claims);
        CognitoUserDetails userDetails = new CognitoUserDetails(jwt);

        // When & Then
        assertThat(userDetails.isAccountNonExpired()).isTrue();
    }

    @Test
    @DisplayName("Should return account as non-locked")
    void isAccountNonLocked_ReturnsTrue() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "cognito-sub-500");
        claims.put("token_use", "id");

        Jwt jwt = createJwt(claims);
        CognitoUserDetails userDetails = new CognitoUserDetails(jwt);

        // When & Then
        assertThat(userDetails.isAccountNonLocked()).isTrue();
    }

    @Test
    @DisplayName("Should return credentials as non-expired")
    void isCredentialsNonExpired_ReturnsTrue() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "cognito-sub-600");
        claims.put("token_use", "id");

        Jwt jwt = createJwt(claims);
        CognitoUserDetails userDetails = new CognitoUserDetails(jwt);

        // When & Then
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    }

    @Test
    @DisplayName("Should return account as enabled")
    void isEnabled_ReturnsTrue() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "cognito-sub-700");
        claims.put("token_use", "id");

        Jwt jwt = createJwt(claims);
        CognitoUserDetails userDetails = new CognitoUserDetails(jwt);

        // When & Then
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should return the JWT object")
    void getJwt_ReturnsJwtObject() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "cognito-sub-800");
        claims.put("token_use", "id");

        Jwt jwt = createJwt(claims);
        CognitoUserDetails userDetails = new CognitoUserDetails(jwt);

        // When & Then
        assertThat(userDetails.getJwt()).isNotNull();
        assertThat(userDetails.getJwt()).isEqualTo(jwt);
    }

    @Test
    @DisplayName("Should get claim from JWT")
    void getClaim_ExistingClaim_ReturnsValue() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "cognito-sub-900");
        claims.put("token_use", "id");
        claims.put("custom_claim", "custom_value");

        Jwt jwt = createJwt(claims);
        CognitoUserDetails userDetails = new CognitoUserDetails(jwt);

        // When
        Object claimValue = userDetails.getClaim("custom_claim");

        // Then
        assertThat(claimValue).isEqualTo("custom_value");
    }

    @Test
    @DisplayName("Should return null for non-existent claim")
    void getClaim_NonExistentClaim_ReturnsNull() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "cognito-sub-1000");
        claims.put("token_use", "id");

        Jwt jwt = createJwt(claims);
        CognitoUserDetails userDetails = new CognitoUserDetails(jwt);

        // When
        Object claimValue = userDetails.getClaim("non_existent");

        // Then
        assertThat(claimValue).isNull();
    }

    @Test
    @DisplayName("Should handle ID token without cognito:username claim")
    void constructor_IdTokenNoUsername_SetsUsernameNull() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "cognito-sub-1100");
        claims.put("token_use", "id");
        claims.put("email", "nouser@example.com");

        Jwt jwt = createJwt(claims);

        // When
        CognitoUserDetails userDetails = new CognitoUserDetails(jwt);

        // Then
        assertThat(userDetails.getCognitoUsername()).isNull();
        assertThat(userDetails.getUsername()).isEqualTo("nouser@example.com");
    }

    @Test
    @DisplayName("Should handle access token without username or email")
    void constructor_AccessTokenNoUsernameOrEmail_ReturnsNull() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "cognito-sub-1200");
        claims.put("token_use", "access");

        Jwt jwt = createJwt(claims);

        // When
        CognitoUserDetails userDetails = new CognitoUserDetails(jwt);

        // Then
        assertThat(userDetails.getCognitoUsername()).isNull();
        assertThat(userDetails.getEmail()).isNull();
        assertThat(userDetails.getUsername()).isNull();
    }

    @Test
    @DisplayName("Should extract complex claim values")
    void getClaim_ComplexValues_ReturnsCorrectly() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "cognito-sub-1300");
        claims.put("token_use", "id");
        claims.put("email_verified", true);
        claims.put("auth_time", 1234567890L);

        Jwt jwt = createJwt(claims);
        CognitoUserDetails userDetails = new CognitoUserDetails(jwt);

        // When & Then
        assertThat(userDetails.getClaim("email_verified")).isEqualTo(true);
        assertThat(userDetails.getClaim("auth_time")).isEqualTo(1234567890L);
    }

    @Test
    @DisplayName("Should handle token with unknown token_use value")
    void constructor_UnknownTokenUse_DefaultsToAccessTokenBehavior() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "cognito-sub-1400");
        claims.put("token_use", "unknown");
        claims.put("username", "unknownuser");
        claims.put("email", "unknown@example.com");

        Jwt jwt = createJwt(claims);

        // When
        CognitoUserDetails userDetails = new CognitoUserDetails(jwt);

        // Then
        assertThat(userDetails.getCognitoUsername()).isEqualTo("unknownuser");
        assertThat(userDetails.getEmail()).isEqualTo("unknown@example.com");
    }

    // Helper method to create JWT for testing
    private Jwt createJwt(Map<String, Object> claims) {
        return new Jwt(
            "test-token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "RS256", "typ", "JWT"),
            claims
        );
    }
}
