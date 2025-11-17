package com.biddergod.user_service.service;

import com.biddergod.user_service.security.CognitoUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdTokenService Tests")
class IdTokenServiceTest {

    @Mock
    private JwtDecoder jwtDecoder;

    @InjectMocks
    private IdTokenService idTokenService;

    private Jwt validIdToken;
    private Jwt accessToken;

    @BeforeEach
    void setUp() {
        Map<String, Object> idTokenClaims = new HashMap<>();
        idTokenClaims.put("sub", "cognito-sub-123");
        idTokenClaims.put("token_use", "id");
        idTokenClaims.put("cognito:username", "testuser");
        idTokenClaims.put("email", "test@example.com");
        idTokenClaims.put("email_verified", true);
        idTokenClaims.put("given_name", "John");
        idTokenClaims.put("family_name", "Doe");
        idTokenClaims.put("name", "John Doe");

        validIdToken = new Jwt(
            "valid-token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "RS256"),
            idTokenClaims
        );

        Map<String, Object> accessTokenClaims = new HashMap<>();
        accessTokenClaims.put("sub", "cognito-sub-123");
        accessTokenClaims.put("token_use", "access");
        accessTokenClaims.put("username", "testuser");

        accessToken = new Jwt(
            "access-token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "RS256"),
            accessTokenClaims
        );
    }

    @Test
    @DisplayName("Should parse valid ID token")
    void parseIdToken_ValidIdToken_ReturnsUserDetails() {
        // Given
        when(jwtDecoder.decode("valid-id-token")).thenReturn(validIdToken);

        // When
        Optional<CognitoUserDetails> result = idTokenService.parseIdToken("valid-id-token");

        // Then
        assertThat(result).isPresent();
        verify(jwtDecoder, times(1)).decode("valid-id-token");
    }

    @Test
    @DisplayName("Should parse ID token with Bearer prefix")
    void parseIdToken_BearerPrefix_RemovesPrefixAndParses() {
        // Given
        when(jwtDecoder.decode("valid-id-token")).thenReturn(validIdToken);

        // When
        Optional<CognitoUserDetails> result = idTokenService.parseIdToken("Bearer valid-id-token");

        // Then
        assertThat(result).isPresent();
        verify(jwtDecoder, times(1)).decode("valid-id-token");
    }

    @Test
    @DisplayName("Should return empty when token is null")
    void parseIdToken_NullToken_ReturnsEmpty() {
        // When
        Optional<CognitoUserDetails> result = idTokenService.parseIdToken(null);

        // Then
        assertThat(result).isEmpty();
        verify(jwtDecoder, never()).decode(anyString());
    }

    @Test
    @DisplayName("Should return empty when token is empty string")
    void parseIdToken_EmptyToken_ReturnsEmpty() {
        // When
        Optional<CognitoUserDetails> result = idTokenService.parseIdToken("");

        // Then
        assertThat(result).isEmpty();
        verify(jwtDecoder, never()).decode(anyString());
    }

    @Test
    @DisplayName("Should return empty when token is whitespace only")
    void parseIdToken_WhitespaceToken_ReturnsEmpty() {
        // When
        Optional<CognitoUserDetails> result = idTokenService.parseIdToken("   ");

        // Then
        assertThat(result).isEmpty();
        verify(jwtDecoder, never()).decode(anyString());
    }

    @Test
    @DisplayName("Should return empty when token_use is not id")
    void parseIdToken_AccessToken_ReturnsEmpty() {
        // Given
        when(jwtDecoder.decode("access-token")).thenReturn(accessToken);

        // When
        Optional<CognitoUserDetails> result = idTokenService.parseIdToken("access-token");

        // Then
        assertThat(result).isEmpty();
        verify(jwtDecoder, times(1)).decode("access-token");
    }

    @Test
    @DisplayName("Should return empty when JWT decoding fails")
    void parseIdToken_DecodingFails_ReturnsEmpty() {
        // Given
        when(jwtDecoder.decode("invalid-token")).thenThrow(new JwtException("Invalid token"));

        // When
        Optional<CognitoUserDetails> result = idTokenService.parseIdToken("invalid-token");

        // Then
        assertThat(result).isEmpty();
        verify(jwtDecoder, times(1)).decode("invalid-token");
    }

    @Test
    @DisplayName("Should return empty when generic exception occurs")
    void parseIdToken_GenericException_ReturnsEmpty() {
        // Given
        when(jwtDecoder.decode("problematic-token")).thenThrow(new RuntimeException("Unexpected error"));

        // When
        Optional<CognitoUserDetails> result = idTokenService.parseIdToken("problematic-token");

        // Then
        assertThat(result).isEmpty();
        verify(jwtDecoder, times(1)).decode("problematic-token");
    }

    @Test
    @DisplayName("Should extract user profile from ID token details")
    void extractUserProfile_ValidIdToken_ReturnsProfile() {
        // Given
        CognitoUserDetails cognitoDetails = new CognitoUserDetails(validIdToken);

        // When
        IdTokenService.UserProfile profile = idTokenService.extractUserProfile(cognitoDetails);

        // Then
        assertThat(profile).isNotNull();
        assertThat(profile.getEmail()).isEqualTo("test@example.com");
        assertThat(profile.getCognitoUsername()).isEqualTo("testuser");
        assertThat(profile.getEmailVerified()).isEqualTo(true);
        assertThat(profile.getGivenName()).isEqualTo("John");
        assertThat(profile.getFamilyName()).isEqualTo("Doe");
        assertThat(profile.getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should extract profile with missing optional fields")
    void extractUserProfile_MissingOptionalFields_ReturnsPartialProfile() {
        // Given
        Map<String, Object> minimalClaims = new HashMap<>();
        minimalClaims.put("sub", "cognito-sub-456");
        minimalClaims.put("token_use", "id");
        minimalClaims.put("cognito:username", "minimaluser");
        minimalClaims.put("email", "minimal@example.com");

        Jwt minimalToken = new Jwt(
            "minimal-token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "RS256"),
            minimalClaims
        );

        CognitoUserDetails cognitoDetails = new CognitoUserDetails(minimalToken);

        // When
        IdTokenService.UserProfile profile = idTokenService.extractUserProfile(cognitoDetails);

        // Then
        assertThat(profile).isNotNull();
        assertThat(profile.getEmail()).isEqualTo("minimal@example.com");
        assertThat(profile.getCognitoUsername()).isEqualTo("minimaluser");
        assertThat(profile.getEmailVerified()).isNull();
        assertThat(profile.getGivenName()).isNull();
        assertThat(profile.getFamilyName()).isNull();
        assertThat(profile.getName()).isNull();
    }

    @Test
    @DisplayName("UserProfile should have all getters working")
    void userProfile_GettersWork() {
        // Given
        IdTokenService.UserProfile profile = new IdTokenService.UserProfile(
            "email@test.com",
            "username",
            true,
            "FirstName",
            "LastName",
            "FirstName LastName"
        );

        // Then
        assertThat(profile.getEmail()).isEqualTo("email@test.com");
        assertThat(profile.getCognitoUsername()).isEqualTo("username");
        assertThat(profile.getEmailVerified()).isEqualTo(true);
        assertThat(profile.getGivenName()).isEqualTo("FirstName");
        assertThat(profile.getFamilyName()).isEqualTo("LastName");
        assertThat(profile.getName()).isEqualTo("FirstName LastName");
    }

    @Test
    @DisplayName("Should handle null values in UserProfile")
    void userProfile_NullValues_HandlesGracefully() {
        // Given
        IdTokenService.UserProfile profile = new IdTokenService.UserProfile(
            null,
            null,
            null,
            null,
            null,
            null
        );

        // Then
        assertThat(profile.getEmail()).isNull();
        assertThat(profile.getCognitoUsername()).isNull();
        assertThat(profile.getEmailVerified()).isNull();
        assertThat(profile.getGivenName()).isNull();
        assertThat(profile.getFamilyName()).isNull();
        assertThat(profile.getName()).isNull();
    }
}
