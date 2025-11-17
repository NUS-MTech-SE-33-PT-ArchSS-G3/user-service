package com.biddergod.user_service.controller;

import com.biddergod.user_service.dto.UserProfileUpdateRequest;
import com.biddergod.user_service.entity.User;
import com.biddergod.user_service.security.CognitoUserDetails;
import com.biddergod.user_service.service.IdTokenService;
import com.biddergod.user_service.service.JwtService;
import com.biddergod.user_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JwtService jwtService;

    @Mock
    private IdTokenService idTokenService;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;
    private User testUser;
    private CognitoUserDetails testAccessTokenDetails;
    private CognitoUserDetails testIdTokenDetails;
    private IdTokenService.UserProfile testIdProfile;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        testAccessTokenDetails = mock(CognitoUserDetails.class);
        lenient().when(testAccessTokenDetails.getCognitoSub()).thenReturn("test-cognito-sub");
        lenient().when(testAccessTokenDetails.getCognitoUsername()).thenReturn("testuser");
        lenient().when(testAccessTokenDetails.getEmail()).thenReturn(null);
        lenient().when(testAccessTokenDetails.getClaim("client_id")).thenReturn("test-client-id");
        lenient().when(testAccessTokenDetails.getClaim("token_use")).thenReturn("access");
        lenient().when(testAccessTokenDetails.getClaim("scope")).thenReturn("aws.cognito.signin.user.admin");

        testIdTokenDetails = mock(CognitoUserDetails.class);
        lenient().when(testIdTokenDetails.getCognitoSub()).thenReturn("test-cognito-sub");
        lenient().when(testIdTokenDetails.getCognitoUsername()).thenReturn("testuser");
        lenient().when(testIdTokenDetails.getEmail()).thenReturn("enhanced@example.com");
        lenient().when(testIdTokenDetails.getClaim("email")).thenReturn("enhanced@example.com");
        lenient().when(testIdTokenDetails.getClaim("email_verified")).thenReturn(true);
        lenient().when(testIdTokenDetails.getClaim("given_name")).thenReturn("Enhanced");
        lenient().when(testIdTokenDetails.getClaim("family_name")).thenReturn("User");
        lenient().when(testIdTokenDetails.getClaim("name")).thenReturn("Enhanced User");
        lenient().when(testIdTokenDetails.getClaim("token_use")).thenReturn("id");

        testIdProfile = new IdTokenService.UserProfile(
            "enhanced@example.com",
            "testuser",
            true,
            "Enhanced",
            "User",
            "Enhanced User"
        );
    }

    @Test
    void getCurrentUser_StandardResponse_Success() throws Exception {
        when(jwtService.getCurrentUser()).thenReturn(Optional.of(testUser));
        when(jwtService.getCurrentCognitoUserDetails()).thenReturn(Optional.of(testAccessTokenDetails));
        when(jwtService.getUserGroups()).thenReturn(List.of("USER"));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.cognitoSub").value("test-cognito-sub"))
                .andExpect(jsonPath("$.cognitoUsername").value("testuser"))
                .andExpect(jsonPath("$.groups[0]").value("USER"))
                .andExpect(jsonPath("$.enhancedProfile").doesNotExist());
    }

    @Test
    void getCurrentUser_WithIdTokenQueryParam_EnhancedResponse() throws Exception {
        when(jwtService.getCurrentUser()).thenReturn(Optional.of(testUser));
        when(jwtService.getCurrentCognitoUserDetails()).thenReturn(Optional.of(testAccessTokenDetails));
        when(jwtService.getUserGroups()).thenReturn(List.of("USER"));
        when(idTokenService.parseIdToken("valid-id-token")).thenReturn(Optional.of(testIdTokenDetails));
        when(idTokenService.extractUserProfile(testIdTokenDetails)).thenReturn(testIdProfile);

        mockMvc.perform(get("/api/users/me")
                        .param("idToken", "valid-id-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.enhancedProfile.email").value("enhanced@example.com"))
                .andExpect(jsonPath("$.enhancedProfile.email_verified").value(true))
                .andExpect(jsonPath("$.enhancedProfile.given_name").value("Enhanced"))
                .andExpect(jsonPath("$.enhancedProfile.family_name").value("User"))
                .andExpect(jsonPath("$.enhancedProfile.name").value("Enhanced User"))
                .andExpect(jsonPath("$.enhancedProfile.token_use").value("id"));
    }

    @Test
    void getCurrentUserPost_WithIdTokenInBody_EnhancedResponse() throws Exception {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setIdToken("valid-id-token");

        when(jwtService.getCurrentUser()).thenReturn(Optional.of(testUser));
        when(jwtService.getCurrentCognitoUserDetails()).thenReturn(Optional.of(testAccessTokenDetails));
        when(jwtService.getUserGroups()).thenReturn(List.of("USER"));
        when(idTokenService.parseIdToken("valid-id-token")).thenReturn(Optional.of(testIdTokenDetails));
        when(idTokenService.extractUserProfile(testIdTokenDetails)).thenReturn(testIdProfile);

        mockMvc.perform(post("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enhancedProfile.email").value("enhanced@example.com"))
                .andExpect(jsonPath("$.enhancedProfile.email_verified").value(true))
                .andExpect(jsonPath("$.enhancedProfile.given_name").value("Enhanced"))
                .andExpect(jsonPath("$.enhancedProfile.family_name").value("User"));
    }

    @Test
    void getCurrentUser_WithInvalidIdToken_ErrorInResponse() throws Exception {
        when(jwtService.getCurrentUser()).thenReturn(Optional.of(testUser));
        when(jwtService.getCurrentCognitoUserDetails()).thenReturn(Optional.of(testAccessTokenDetails));
        when(jwtService.getUserGroups()).thenReturn(List.of("USER"));
        when(idTokenService.parseIdToken("invalid-id-token")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/me")
                        .param("idToken", "invalid-id-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.idTokenError").value("Invalid or malformed ID token"))
                .andExpect(jsonPath("$.enhancedProfile").doesNotExist());
    }

    @Test
    void getCurrentUser_ProfileUpdateWithIdToken_Success() throws Exception {
        User userWithoutNames = new User();
        userWithoutNames.setId(1L);
        userWithoutNames.setUsername("testuser");
        userWithoutNames.setEmail("test@example.com");
        userWithoutNames.setFirstName(null);
        userWithoutNames.setLastName(null);

        when(jwtService.getCurrentUser()).thenReturn(Optional.of(userWithoutNames));
        when(jwtService.getCurrentCognitoUserDetails()).thenReturn(Optional.of(testAccessTokenDetails));
        when(jwtService.getUserGroups()).thenReturn(List.of("USER"));
        when(idTokenService.parseIdToken("valid-id-token")).thenReturn(Optional.of(testIdTokenDetails));
        when(idTokenService.extractUserProfile(testIdTokenDetails)).thenReturn(testIdProfile);

        mockMvc.perform(get("/api/users/me")
                        .param("idToken", "valid-id-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileUpdated").value(true))
                .andExpect(jsonPath("$.firstName").value("Enhanced"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value("enhanced@example.com"));
    }

    @Test
    void getCurrentUser_EmptyIdToken_StandardResponse() throws Exception {
        when(jwtService.getCurrentUser()).thenReturn(Optional.of(testUser));
        when(jwtService.getCurrentCognitoUserDetails()).thenReturn(Optional.of(testAccessTokenDetails));
        when(jwtService.getUserGroups()).thenReturn(List.of("USER"));

        mockMvc.perform(get("/api/users/me")
                        .param("idToken", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.enhancedProfile").doesNotExist())
                .andExpect(jsonPath("$.idTokenError").doesNotExist());
    }

    @Test
    void getCurrentUser_NoAccessTokenDetails_PartialResponse() throws Exception {
        when(jwtService.getCurrentUser()).thenReturn(Optional.of(testUser));
        when(jwtService.getCurrentCognitoUserDetails()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.cognitoSub").doesNotExist())
                .andExpect(jsonPath("$.cognitoUsername").doesNotExist())
                .andExpect(jsonPath("$.groups").doesNotExist());
    }

    @Test
    void getCurrentUser_UserNotFound_Unauthorized() throws Exception {
        when(jwtService.getCurrentUser()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not found or invalid token"));
    }

    @Test
    void getCurrentUser_ServiceException_InternalServerError() throws Exception {
        when(jwtService.getCurrentUser()).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error retrieving user information: Service error"));
    }

    @Test
    void getCurrentUserPost_EmptyRequestBody_StandardResponse() throws Exception {
        when(jwtService.getCurrentUser()).thenReturn(Optional.of(testUser));
        when(jwtService.getCurrentCognitoUserDetails()).thenReturn(Optional.of(testAccessTokenDetails));
        when(jwtService.getUserGroups()).thenReturn(List.of("USER"));

        mockMvc.perform(post("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.enhancedProfile").doesNotExist());
    }

    @Test
    void getCurrentUserPost_NullRequestBody_StandardResponse() throws Exception {
        when(jwtService.getCurrentUser()).thenReturn(Optional.of(testUser));
        when(jwtService.getCurrentCognitoUserDetails()).thenReturn(Optional.of(testAccessTokenDetails));
        when(jwtService.getUserGroups()).thenReturn(List.of("USER"));

        mockMvc.perform(post("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.enhancedProfile").doesNotExist());
    }

    @Test
    void getCurrentUser_IdTokenParsingException_InternalServerError() throws Exception {
        when(jwtService.getCurrentUser()).thenReturn(Optional.of(testUser));
        when(jwtService.getCurrentCognitoUserDetails()).thenReturn(Optional.of(testAccessTokenDetails));
        when(idTokenService.parseIdToken(anyString())).thenThrow(new RuntimeException("Token parsing error"));

        mockMvc.perform(get("/api/users/me")
                        .param("idToken", "problematic-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error retrieving user information: Token parsing error"));
    }

    // ========== GET /api/users/profile Tests ==========

    @Nested
    @DisplayName("GET /api/users/profile Tests")
    class GetUserProfileTests {

        @Test
        @DisplayName("Should return user profile when user exists")
        void getUserProfile_UserExists_ReturnsProfile() throws Exception {
            when(jwtService.getCurrentUser()).thenReturn(Optional.of(testUser));

            mockMvc.perform(get("/api/users/profile"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"));
        }

        @Test
        @DisplayName("Should return 401 when user not found")
        void getUserProfile_UserNotFound_ReturnsUnauthorized() throws Exception {
            when(jwtService.getCurrentUser()).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/users/profile"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("User not found or invalid token"));
        }

        @Test
        @DisplayName("Should return 500 on service exception")
        void getUserProfile_ServiceException_ReturnsInternalServerError() throws Exception {
            when(jwtService.getCurrentUser()).thenThrow(new RuntimeException("Database error"));

            mockMvc.perform(get("/api/users/profile"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Error retrieving user profile: Database error"));
        }
    }

    // ========== GET /api/users/groups Tests ==========

    @Nested
    @DisplayName("GET /api/users/groups Tests")
    class GetUserGroupsTests {

        @Test
        @DisplayName("Should return user groups when token is valid")
        void getUserGroups_ValidToken_ReturnsGroups() throws Exception {
            when(jwtService.isTokenValid()).thenReturn(true);
            when(jwtService.getUserGroups()).thenReturn(Arrays.asList("ADMIN", "USER"));

            mockMvc.perform(get("/api/users/groups"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0]").value("ADMIN"))
                    .andExpect(jsonPath("$[1]").value("USER"));
        }

        @Test
        @DisplayName("Should return empty list when user has no groups")
        void getUserGroups_NoGroups_ReturnsEmptyList() throws Exception {
            when(jwtService.isTokenValid()).thenReturn(true);
            when(jwtService.getUserGroups()).thenReturn(Arrays.asList());

            mockMvc.perform(get("/api/users/groups"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 401 when token is invalid")
        void getUserGroups_InvalidToken_ReturnsUnauthorized() throws Exception {
            when(jwtService.isTokenValid()).thenReturn(false);

            mockMvc.perform(get("/api/users/groups"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Invalid token"));
        }

        @Test
        @DisplayName("Should return 500 on service exception")
        void getUserGroups_ServiceException_ReturnsInternalServerError() throws Exception {
            when(jwtService.isTokenValid()).thenThrow(new RuntimeException("Token parsing error"));

            mockMvc.perform(get("/api/users/groups"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Error retrieving user groups: Token parsing error"));
        }
    }

    // ========== GET /api/users/token-info Tests ==========

    @Nested
    @DisplayName("GET /api/users/token-info Tests")
    class GetTokenInfoTests {

        @Test
        @DisplayName("Should return token information when token is valid")
        void getTokenInfo_ValidToken_ReturnsTokenInfo() throws Exception {
            when(testAccessTokenDetails.getClaim("iss")).thenReturn("https://cognito-idp.region.amazonaws.com/pool");
            when(testAccessTokenDetails.getClaim("aud")).thenReturn("client-id");
            when(testAccessTokenDetails.getClaim("client_id")).thenReturn("test-client-id");

            when(jwtService.getCurrentCognitoUserDetails()).thenReturn(Optional.of(testAccessTokenDetails));
            when(jwtService.getUserGroups()).thenReturn(List.of("USER"));

            mockMvc.perform(get("/api/users/token-info"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subject").value("test-cognito-sub"))
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.groups[0]").value("USER"))
                    .andExpect(jsonPath("$.tokenUse").value("access"))
                    .andExpect(jsonPath("$.clientId").value("test-client-id"));
        }

        @Test
        @DisplayName("Should return 401 when token is invalid")
        void getTokenInfo_InvalidToken_ReturnsUnauthorized() throws Exception {
            when(jwtService.getCurrentCognitoUserDetails()).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/users/token-info"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Invalid token"));
        }

        @Test
        @DisplayName("Should return 500 on service exception")
        void getTokenInfo_ServiceException_ReturnsInternalServerError() throws Exception {
            when(jwtService.getCurrentCognitoUserDetails()).thenThrow(new RuntimeException("Token error"));

            mockMvc.perform(get("/api/users/token-info"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Error retrieving token information: Token error"));
        }
    }

    // ========== PUT /api/users/profile Tests ==========

    @Nested
    @DisplayName("PUT /api/users/profile Tests")
    class UpdateUserProfileTests {

        @Test
        @DisplayName("Should update profile with ID token data")
        void updateUserProfile_WithIdToken_UpdatesProfile() throws Exception {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest();
            request.setIdToken("valid-id-token");

            when(jwtService.getCurrentUser()).thenReturn(Optional.of(testUser));
            when(idTokenService.parseIdToken("valid-id-token")).thenReturn(Optional.of(testIdTokenDetails));
            when(idTokenService.extractUserProfile(testIdTokenDetails)).thenReturn(testIdProfile);
            when(userService.save(any(User.class))).thenReturn(testUser);

            mockMvc.perform(put("/api/users/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Profile updated successfully"))
                    .andExpect(jsonPath("$.user").exists());

            verify(userService, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should update profile with manual data")
        void updateUserProfile_WithManualData_UpdatesProfile() throws Exception {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest();
            request.setFirstName("NewFirst");
            request.setLastName("NewLast");

            when(jwtService.getCurrentUser()).thenReturn(Optional.of(testUser));
            when(userService.save(any(User.class))).thenReturn(testUser);

            mockMvc.perform(put("/api/users/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Profile updated successfully"));

            verify(userService, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should return 400 when ID token is invalid")
        void updateUserProfile_InvalidIdToken_ReturnsBadRequest() throws Exception {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest();
            request.setIdToken("invalid-token");

            when(jwtService.getCurrentUser()).thenReturn(Optional.of(testUser));
            when(idTokenService.parseIdToken("invalid-token")).thenReturn(Optional.empty());

            mockMvc.perform(put("/api/users/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Invalid ID token provided"));

            verify(userService, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should return 401 when user not found")
        void updateUserProfile_UserNotFound_ReturnsUnauthorized() throws Exception {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest();
            request.setFirstName("NewFirst");

            when(jwtService.getCurrentUser()).thenReturn(Optional.empty());

            mockMvc.perform(put("/api/users/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("User not found or invalid access token"));

            verify(userService, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should return 500 on service exception")
        void updateUserProfile_ServiceException_ReturnsInternalServerError() throws Exception {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest();
            request.setFirstName("NewFirst");

            when(jwtService.getCurrentUser()).thenReturn(Optional.of(testUser));
            when(userService.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

            mockMvc.perform(put("/api/users/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Error updating user profile: Database error"));
        }
    }

    // ========== GET /api/users?id= Tests ==========

    @Nested
    @DisplayName("GET /api/users?id= Tests")
    class GetUsersByIdsTests {

        @Test
        @DisplayName("Should return single user when ID exists")
        void getUsersByIds_SingleUser_ReturnsUser() throws Exception {
            when(userService.findById(1L)).thenReturn(Optional.of(testUser));

            mockMvc.perform(get("/api/users")
                            .param("id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.users", hasSize(1)))
                    .andExpect(jsonPath("$.users[0].id").value(1))
                    .andExpect(jsonPath("$.users[0].username").value("testuser"))
                    .andExpect(jsonPath("$.found").value(1))
                    .andExpect(jsonPath("$.requested").value(1));
        }

        @Test
        @DisplayName("Should return multiple users when multiple IDs provided")
        void getUsersByIds_MultipleUsers_ReturnsUsers() throws Exception {
            User user2 = new User();
            user2.setId(2L);
            user2.setUsername("user2");
            user2.setEmail("user2@example.com");

            when(userService.findById(1L)).thenReturn(Optional.of(testUser));
            when(userService.findById(2L)).thenReturn(Optional.of(user2));

            mockMvc.perform(get("/api/users")
                            .param("id", "1", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.users", hasSize(2)))
                    .andExpect(jsonPath("$.found").value(2))
                    .andExpect(jsonPath("$.requested").value(2));
        }

        @Test
        @DisplayName("Should filter out non-existent users")
        void getUsersByIds_SomeUsersNotFound_ReturnsOnlyFoundUsers() throws Exception {
            when(userService.findById(1L)).thenReturn(Optional.of(testUser));
            when(userService.findById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/users")
                            .param("id", "1", "999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.users", hasSize(1)))
                    .andExpect(jsonPath("$.users[0].id").value(1))
                    .andExpect(jsonPath("$.found").value(1))
                    .andExpect(jsonPath("$.requested").value(2));
        }

        @Test
        @DisplayName("Should return empty list when no users found")
        void getUsersByIds_NoUsersFound_ReturnsEmptyList() throws Exception {
            when(userService.findById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/users")
                            .param("id", "999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.users", hasSize(0)))
                    .andExpect(jsonPath("$.found").value(0))
                    .andExpect(jsonPath("$.requested").value(1));
        }

        @Test
        @DisplayName("Should return 500 on service exception")
        void getUsersByIds_ServiceException_ReturnsInternalServerError() throws Exception {
            when(userService.findById(any())).thenThrow(new RuntimeException("Database error"));

            mockMvc.perform(get("/api/users")
                            .param("id", "1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Error retrieving users"))
                    .andExpect(jsonPath("$.message").value("Database error"));
        }
    }
}