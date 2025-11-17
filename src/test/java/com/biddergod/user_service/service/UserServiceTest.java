package com.biddergod.user_service.service;

import com.biddergod.user_service.entity.User;
import com.biddergod.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
    }

    @Test
    @DisplayName("Should find user by ID when user exists")
    void findById_UserExists_ReturnsUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when user ID does not exist")
    void findById_UserNotExists_ReturnsEmpty() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should find user by username when user exists")
    void findByUsername_UserExists_ReturnsUser() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByUsername("testuser");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should return empty when username does not exist")
    void findByUsername_UserNotExists_ReturnsEmpty() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByUsername("nonexistent");

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("Should find user by email when user exists")
    void findByEmail_UserExists_ReturnsUser() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByEmail("test@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should return empty when email does not exist")
    void findByEmail_EmailNotExists_ReturnsEmpty() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail("nonexistent@example.com");

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("Should return all users")
    void findAll_ReturnsAllUsers() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");

        List<User> users = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(testUser, user2);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no users exist")
    void findAll_NoUsers_ReturnsEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<User> result = userService.findAll();

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return true when user exists by ID")
    void existsById_UserExists_ReturnsTrue() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = userService.existsById(1L);

        // Then
        assertThat(result).isTrue();
        verify(userRepository, times(1)).existsById(1L);
    }

    @Test
    @DisplayName("Should return false when user does not exist by ID")
    void existsById_UserNotExists_ReturnsFalse() {
        // Given
        when(userRepository.existsById(999L)).thenReturn(false);

        // When
        boolean result = userService.existsById(999L);

        // Then
        assertThat(result).isFalse();
        verify(userRepository, times(1)).existsById(999L);
    }

    @Test
    @DisplayName("Should return true when username exists")
    void existsByUsername_UsernameExists_ReturnsTrue() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When
        boolean result = userService.existsByUsername("testuser");

        // Then
        assertThat(result).isTrue();
        verify(userRepository, times(1)).existsByUsername("testuser");
    }

    @Test
    @DisplayName("Should return false when username does not exist")
    void existsByUsername_UsernameNotExists_ReturnsFalse() {
        // Given
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // When
        boolean result = userService.existsByUsername("nonexistent");

        // Then
        assertThat(result).isFalse();
        verify(userRepository, times(1)).existsByUsername("nonexistent");
    }

    @Test
    @DisplayName("Should return true when email exists")
    void existsByEmail_EmailExists_ReturnsTrue() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When
        boolean result = userService.existsByEmail("test@example.com");

        // Then
        assertThat(result).isTrue();
        verify(userRepository, times(1)).existsByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void existsByEmail_EmailNotExists_ReturnsFalse() {
        // Given
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // When
        boolean result = userService.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(result).isFalse();
        verify(userRepository, times(1)).existsByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("Should save new user successfully")
    void save_NewUser_ReturnsUser() {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("newuser");
        savedUser.setEmail("newuser@example.com");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.save(newUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("newuser@example.com");
        verify(userRepository, times(1)).save(newUser);
    }

    @Test
    @DisplayName("Should update existing user successfully")
    void save_UpdateUser_ReturnsUpdatedUser() {
        // Given
        testUser.setFirstName("UpdatedJohn");
        testUser.setLastName("UpdatedDoe");

        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        User result = userService.save(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("UpdatedJohn");
        assertThat(result.getLastName()).isEqualTo("UpdatedDoe");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Should delete user by ID successfully")
    void deleteById_UserExists_DeletesUser() {
        // Given
        doNothing().when(userRepository).deleteById(1L);

        // When
        userService.deleteById(1L);

        // Then
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should handle null username when finding user")
    void findByUsername_NullUsername_HandlesGracefully() {
        // Given
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByUsername(null);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findByUsername(null);
    }

    @Test
    @DisplayName("Should handle null email when finding user")
    void findByEmail_NullEmail_HandlesGracefully() {
        // Given
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail(null);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findByEmail(null);
    }
}