package com.biddergod.user_service.service;

import com.biddergod.user_service.entity.User;
import com.biddergod.user_service.repository.UserRepository;
import com.biddergod.user_service.security.CognitoUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
class JwtServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CognitoUserService cognitoUserService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JwtAuthenticationToken jwtAuthenticationToken;

    @InjectMocks
    private JwtService jwtService;

    private Jwt mockJwt;
    private User testUser;
    private CognitoUserDetails cognitoUserDetails;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("cognito-sub-123");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "cognito-sub-123");
        claims.put("username", "testuser"); // For access token
        claims.put("cognito:username", "testuser");
        claims.put("email", "test@example.com");
        claims.put("cognito:groups", Arrays.asList("USER", "ADMIN"));
        claims.put("token_use", "access");
        claims.put("client_id", "test-client-id");
        claims.put("iss", "https://cognito-idp.region.amazonaws.com/pool");
        claims.put("aud", "client-id");

        mockJwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claims(c -> c.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        cognitoUserDetails = new CognitoUserDetails(mockJwt);
    }

    @Nested
    @DisplayName("getCurrentUser Tests")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Should return user when JWT authentication is valid")
        void getCurrentUser_ValidJwt_ReturnsUser() {
            when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
            when(jwtAuthenticationToken.getToken()).thenReturn(mockJwt);
            when(cognitoUserService.findOrCreateUser(any(CognitoUserDetails.class))).thenReturn(testUser);

            Optional<User> result = jwtService.getCurrentUser();

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
            assertThat(result.get().getUsername()).isEqualTo("cognito-sub-123");
            verify(cognitoUserService, times(1)).findOrCreateUser(any(CognitoUserDetails.class));
        }

        @Test
        @DisplayName("Should return empty when authentication is not JWT")
        void getCurrentUser_NotJwtAuthentication_ReturnsEmpty() {
            Authentication nonJwtAuth = mock(Authentication.class);
            when(securityContext.getAuthentication()).thenReturn(nonJwtAuth);

            Optional<User> result = jwtService.getCurrentUser();

            assertThat(result).isEmpty();
            verify(cognitoUserService, never()).findOrCreateUser(any());
        }

        @Test
        @DisplayName("Should return empty when authentication is null")
        void getCurrentUser_NullAuthentication_ReturnsEmpty() {
            when(securityContext.getAuthentication()).thenReturn(null);

            Optional<User> result = jwtService.getCurrentUser();

            assertThat(result).isEmpty();
            verify(cognitoUserService, never()).findOrCreateUser(any());
        }
    }

    @Nested
    @DisplayName("getCurrentUserId Tests")
    class GetCurrentUserIdTests {

        @Test
        @DisplayName("Should return user ID when user exists")
        void getCurrentUserId_UserExists_ReturnsId() {
            when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
            when(jwtAuthenticationToken.getToken()).thenReturn(mockJwt);
            when(cognitoUserService.findOrCreateUser(any(CognitoUserDetails.class))).thenReturn(testUser);

            Optional<Long> result = jwtService.getCurrentUserId();

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should return empty when user does not exist")
        void getCurrentUserId_UserNotExists_ReturnsEmpty() {
            when(securityContext.getAuthentication()).thenReturn(null);

            Optional<Long> result = jwtService.getCurrentUserId();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCurrentCognitoUserDetails Tests")
    class GetCurrentCognitoUserDetailsTests {

        @Test
        @DisplayName("Should return Cognito user details when JWT is valid")
        void getCurrentCognitoUserDetails_ValidJwt_ReturnsDetails() {
            when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
            when(jwtAuthenticationToken.getToken()).thenReturn(mockJwt);

            Optional<CognitoUserDetails> result = jwtService.getCurrentCognitoUserDetails();

            assertThat(result).isPresent();
            assertThat(result.get().getCognitoSub()).isEqualTo("cognito-sub-123");
            assertThat(result.get().getCognitoUsername()).isEqualTo("testuser");
            assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should return empty when authentication is not JWT")
        void getCurrentCognitoUserDetails_NotJwtAuth_ReturnsEmpty() {
            Authentication nonJwtAuth = mock(Authentication.class);
            when(securityContext.getAuthentication()).thenReturn(nonJwtAuth);

            Optional<CognitoUserDetails> result = jwtService.getCurrentCognitoUserDetails();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("isTokenValid Tests")
    class IsTokenValidTests {

        @Test
        @DisplayName("Should return true when token is valid")
        void isTokenValid_ValidToken_ReturnsTrue() {
            when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
            when(jwtAuthenticationToken.getToken()).thenReturn(mockJwt);

            boolean result = jwtService.isTokenValid();

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when token is invalid")
        void isTokenValid_InvalidToken_ReturnsFalse() {
            when(securityContext.getAuthentication()).thenReturn(null);

            boolean result = jwtService.isTokenValid();

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getTokenClaim Tests")
    class GetTokenClaimTests {

        @Test
        @DisplayName("Should return claim value when claim exists")
        void getTokenClaim_ClaimExists_ReturnsValue() {
            when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
            when(jwtAuthenticationToken.getToken()).thenReturn(mockJwt);

            Optional<Object> result = jwtService.getTokenClaim("email");

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should return empty when claim does not exist")
        void getTokenClaim_ClaimNotExists_ReturnsEmpty() {
            when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
            when(jwtAuthenticationToken.getToken()).thenReturn(mockJwt);

            Optional<Object> result = jwtService.getTokenClaim("nonexistent");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty when authentication is null")
        void getTokenClaim_NoAuthentication_ReturnsEmpty() {
            when(securityContext.getAuthentication()).thenReturn(null);

            Optional<Object> result = jwtService.getTokenClaim("email");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasRole Tests")
    class HasRoleTests {

        @Test
        @DisplayName("Should return true when user has the role")
        void hasRole_UserHasRole_ReturnsTrue() {
            when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
            when(jwtAuthenticationToken.getToken()).thenReturn(mockJwt);

            boolean result = jwtService.hasRole("USER");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when user does not have the role")
        void hasRole_UserDoesNotHaveRole_ReturnsFalse() {
            when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
            when(jwtAuthenticationToken.getToken()).thenReturn(mockJwt);

            boolean result = jwtService.hasRole("SUPERADMIN");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when authentication is null")
        void hasRole_NoAuthentication_ReturnsFalse() {
            when(securityContext.getAuthentication()).thenReturn(null);

            boolean result = jwtService.hasRole("USER");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when groups claim is not a list")
        void hasRole_GroupsNotList_ReturnsFalse() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", "cognito-sub-123");
            claims.put("cognito:groups", "USER");

            Jwt jwtWithStringGroups = Jwt.withTokenValue("token")
                    .header("alg", "RS256")
                    .claims(c -> c.putAll(claims))
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

            when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
            when(jwtAuthenticationToken.getToken()).thenReturn(jwtWithStringGroups);

            boolean result = jwtService.hasRole("USER");

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getUserGroups Tests")
    class GetUserGroupsTests {

        @Test
        @DisplayName("Should return user groups when groups exist")
        void getUserGroups_GroupsExist_ReturnsGroups() {
            when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
            when(jwtAuthenticationToken.getToken()).thenReturn(mockJwt);

            List<String> result = jwtService.getUserGroups();

            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder("USER", "ADMIN");
        }

        @Test
        @DisplayName("Should return empty list when no groups exist")
        void getUserGroups_NoGroups_ReturnsEmptyList() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", "cognito-sub-123");

            Jwt jwtWithoutGroups = Jwt.withTokenValue("token")
                    .header("alg", "RS256")
                    .claims(c -> c.putAll(claims))
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

            when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
            when(jwtAuthenticationToken.getToken()).thenReturn(jwtWithoutGroups);

            List<String> result = jwtService.getUserGroups();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when authentication is null")
        void getUserGroups_NoAuthentication_ReturnsEmptyList() {
            when(securityContext.getAuthentication()).thenReturn(null);

            List<String> result = jwtService.getUserGroups();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when groups claim is not a list")
        void getUserGroups_GroupsNotList_ReturnsEmptyList() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", "cognito-sub-123");
            claims.put("cognito:groups", "USER");

            Jwt jwtWithStringGroups = Jwt.withTokenValue("token")
                    .header("alg", "RS256")
                    .claims(c -> c.putAll(claims))
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

            when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
            when(jwtAuthenticationToken.getToken()).thenReturn(jwtWithStringGroups);

            List<String> result = jwtService.getUserGroups();

            assertThat(result).isEmpty();
        }
    }
}
