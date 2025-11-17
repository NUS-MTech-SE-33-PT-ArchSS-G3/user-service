package com.biddergod.user_service.service;

import com.biddergod.user_service.entity.User;
import com.biddergod.user_service.repository.UserRepository;
import com.biddergod.user_service.security.CognitoUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CognitoUserService Tests")
class CognitoUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CognitoUserService cognitoUserService;

    @Mock
    private CognitoUserDetails cognitoDetails;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("cognito-sub-123");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
    }

    @Test
    @DisplayName("Should find existing user by cognito sub")
    void findOrCreateUser_ExistingUserByCognitoSub_ReturnsUser() {
        // Given
        when(cognitoDetails.getCognitoSub()).thenReturn("cognito-sub-123");
        when(cognitoDetails.getEmail()).thenReturn("test@example.com");
        when(cognitoDetails.getCognitoUsername()).thenReturn("testuser");
        when(userRepository.findByUsername("cognito-sub-123")).thenReturn(Optional.of(testUser));

        // When
        User result = cognitoUserService.findOrCreateUser(cognitoDetails);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("cognito-sub-123");
        verify(userRepository, times(1)).findByUsername("cognito-sub-123");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update email when existing user has cognito.local email")
    void findOrCreateUser_ExistingUserWithCognitoEmail_UpdatesEmail() {
        // Given
        testUser.setEmail("cognito-sub-123@cognito.local");
        when(cognitoDetails.getCognitoSub()).thenReturn("cognito-sub-123");
        when(cognitoDetails.getEmail()).thenReturn("newemail@example.com");
        when(cognitoDetails.getCognitoUsername()).thenReturn("testuser");
        when(userRepository.findByUsername("cognito-sub-123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = cognitoUserService.findOrCreateUser(cognitoDetails);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("newemail@example.com");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Should find existing user by email if not found by cognito sub")
    void findOrCreateUser_ExistingUserByEmail_ReturnsUserAndUpdatesUsername() {
        // Given
        when(cognitoDetails.getCognitoSub()).thenReturn("cognito-sub-456");
        when(cognitoDetails.getEmail()).thenReturn("test@example.com");
        when(cognitoDetails.getCognitoUsername()).thenReturn("testuser");
        when(userRepository.findByUsername("cognito-sub-456")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = cognitoUserService.findOrCreateUser(cognitoDetails);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("cognito-sub-456");
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Should create new user when not found by cognito sub or email")
    void findOrCreateUser_NewUser_CreatesUser() {
        // Given
        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("cognito-sub-789");
        newUser.setEmail("newuser@example.com");

        when(cognitoDetails.getCognitoSub()).thenReturn("cognito-sub-789");
        when(cognitoDetails.getEmail()).thenReturn("newuser@example.com");
        when(cognitoDetails.getCognitoUsername()).thenReturn("newuser");
        when(userRepository.findByUsername("cognito-sub-789")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // When
        User result = cognitoUserService.findOrCreateUser(cognitoDetails);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should create new user with cognito.local email when email is null")
    void findOrCreateUser_NewUserNoEmail_CreatesUserWithCognitoLocalEmail() {
        // Given
        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("cognito-sub-999");
        newUser.setEmail("cognito-sub-999@cognito.local");

        when(cognitoDetails.getCognitoSub()).thenReturn("cognito-sub-999");
        when(cognitoDetails.getEmail()).thenReturn(null);
        when(cognitoDetails.getCognitoUsername()).thenReturn("testuser");
        when(userRepository.findByUsername("cognito-sub-999")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // When
        User result = cognitoUserService.findOrCreateUser(cognitoDetails);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Should set first name from cognito username when creating new user")
    void findOrCreateUser_NewUserWithCognitoUsername_SetsFirstName() {
        // Given
        User newUser = new User();
        newUser.setId(3L);
        newUser.setUsername("cognito-sub-888");
        newUser.setEmail("user@example.com");
        newUser.setFirstName("displayname");

        when(cognitoDetails.getCognitoSub()).thenReturn("cognito-sub-888");
        when(cognitoDetails.getEmail()).thenReturn("user@example.com");
        when(cognitoDetails.getCognitoUsername()).thenReturn("displayname");
        when(userRepository.findByUsername("cognito-sub-888")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // When
        User result = cognitoUserService.findOrCreateUser(cognitoDetails);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should update user email from token when current email is cognito.local")
    void updateUserFromToken_CognitoLocalEmail_UpdatesEmail() {
        // Given
        testUser.setEmail("cognito-sub-123@cognito.local");
        when(cognitoDetails.getEmail()).thenReturn("real@example.com");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = cognitoUserService.updateUserFromToken(testUser, cognitoDetails);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Should update user email from token when current email is null")
    void updateUserFromToken_NullEmail_UpdatesEmail() {
        // Given
        testUser.setEmail(null);
        when(cognitoDetails.getEmail()).thenReturn("real@example.com");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = cognitoUserService.updateUserFromToken(testUser, cognitoDetails);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Should not update email when user already has a real email")
    void updateUserFromToken_RealEmail_DoesNotUpdate() {
        // Given
        testUser.setEmail("existing@example.com");
        when(cognitoDetails.getEmail()).thenReturn("new@example.com");

        // When
        User result = cognitoUserService.updateUserFromToken(testUser, cognitoDetails);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("existing@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not update when token email is null")
    void updateUserFromToken_TokenEmailNull_DoesNotUpdate() {
        // Given
        when(cognitoDetails.getEmail()).thenReturn(null);

        // When
        User result = cognitoUserService.updateUserFromToken(testUser, cognitoDetails);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get user info with all details")
    void getUserInfo_ReturnsCompleteUserInfo() {
        // Given
        when(cognitoDetails.getCognitoSub()).thenReturn("cognito-sub-123");
        when(cognitoDetails.getEmail()).thenReturn("test@example.com");
        when(cognitoDetails.getCognitoUsername()).thenReturn("testuser");
        when(cognitoDetails.getClaim("token_use")).thenReturn("access");
        when(cognitoDetails.getClaim("client_id")).thenReturn("test-client-id");
        when(cognitoDetails.getClaim("scope")).thenReturn("aws.cognito.signin.user.admin");

        // When
        CognitoUserService.UserInfo userInfo = cognitoUserService.getUserInfo(testUser, cognitoDetails);

        // Then
        assertThat(userInfo).isNotNull();
        assertThat(userInfo.getUser()).isEqualTo(testUser);
        assertThat(userInfo.getCognitoSub()).isEqualTo("cognito-sub-123");
        assertThat(userInfo.getTokenEmail()).isEqualTo("test@example.com");
        assertThat(userInfo.getCognitoUsername()).isEqualTo("testuser");
        assertThat(userInfo.getTokenUse()).isEqualTo("access");
        assertThat(userInfo.getClientId()).isEqualTo("test-client-id");
        assertThat(userInfo.getScope()).isEqualTo("aws.cognito.signin.user.admin");
    }

    @Test
    @DisplayName("Should handle empty email in find by email strategy")
    void findOrCreateUser_EmptyEmail_AttemptsEmailLookup() {
        // Given
        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("cognito-sub-555");
        newUser.setEmail("cognito-sub-555@cognito.local");

        when(cognitoDetails.getCognitoSub()).thenReturn("cognito-sub-555");
        when(cognitoDetails.getEmail()).thenReturn("");
        when(cognitoDetails.getCognitoUsername()).thenReturn("testuser");
        when(userRepository.findByUsername("cognito-sub-555")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // When
        User result = cognitoUserService.findOrCreateUser(cognitoDetails);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).findByEmail("");
        verify(userRepository, times(1)).save(any(User.class));
    }
}
