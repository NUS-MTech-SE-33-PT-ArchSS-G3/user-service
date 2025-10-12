package com.biddergod.user_service.controller;

import com.biddergod.user_service.dto.UserProfileUpdateRequest;
import com.biddergod.user_service.entity.User;
import com.biddergod.user_service.security.CognitoUserDetails;
import com.biddergod.user_service.service.IdTokenService;
import com.biddergod.user_service.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JwtService jwtService;

    @Mock
    private IdTokenService idTokenService;

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
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.totalReviews").value(10))
                .andExpect(jsonPath("$.reputationScore").value(850))
                .andExpect(jsonPath("$.trustLevel").value("GOOD"))
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
}