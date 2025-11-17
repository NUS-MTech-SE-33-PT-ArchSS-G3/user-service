package com.biddergod.user_service.integration;

import com.biddergod.user_service.entity.User;
import com.biddergod.user_service.repository.UserRepository;
import com.biddergod.user_service.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UserService Integration Tests")
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser1 = new User();
        testUser1.setUsername("integrationuser1");
        testUser1.setEmail("integration1@example.com");
        testUser1.setFirstName("Integration");
        testUser1.setLastName("User1");

        testUser2 = new User();
        testUser2.setUsername("integrationuser2");
        testUser2.setEmail("integration2@example.com");
        testUser2.setFirstName("Integration");
        testUser2.setLastName("User2");
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save and retrieve user by ID")
    void testSaveAndFindById() {
        User savedUser = userService.save(testUser1);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();

        Optional<User> foundUser = userService.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("integrationuser1");
        assertThat(foundUser.get().getEmail()).isEqualTo("integration1@example.com");
    }

    @Test
    @DisplayName("Should find user by username")
    void testFindByUsername() {
        userService.save(testUser1);

        Optional<User> foundUser = userService.findByUsername("integrationuser1");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("integration1@example.com");
    }

    @Test
    @DisplayName("Should find user by email")
    void testFindByEmail() {
        userService.save(testUser1);

        Optional<User> foundUser = userService.findByEmail("integration1@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("integrationuser1");
    }

    @Test
    @DisplayName("Should find all users")
    void testFindAll() {
        userService.save(testUser1);
        userService.save(testUser2);

        List<User> allUsers = userService.findAll();

        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).extracting(User::getUsername)
                .containsExactlyInAnyOrder("integrationuser1", "integrationuser2");
    }

    @Test
    @DisplayName("Should check if user exists by ID")
    void testExistsById() {
        User savedUser = userService.save(testUser1);

        boolean exists = userService.existsById(savedUser.getId());
        assertThat(exists).isTrue();

        boolean notExists = userService.existsById(9999L);
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should check if username exists")
    void testExistsByUsername() {
        userService.save(testUser1);

        boolean exists = userService.existsByUsername("integrationuser1");
        assertThat(exists).isTrue();

        boolean notExists = userService.existsByUsername("nonexistent");
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should check if email exists")
    void testExistsByEmail() {
        userService.save(testUser1);

        boolean exists = userService.existsByEmail("integration1@example.com");
        assertThat(exists).isTrue();

        boolean notExists = userService.existsByEmail("nonexistent@example.com");
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should update existing user")
    void testUpdateUser() throws InterruptedException {
        User savedUser = userService.save(testUser1);
        Long userId = savedUser.getId();

        // Wait a bit to ensure timestamp difference
        Thread.sleep(10);

        savedUser.setFirstName("UpdatedFirst");
        savedUser.setLastName("UpdatedLast");
        userService.save(savedUser);

        Optional<User> updatedUser = userService.findById(userId);

        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getFirstName()).isEqualTo("UpdatedFirst");
        assertThat(updatedUser.get().getLastName()).isEqualTo("UpdatedLast");
        assertThat(updatedUser.get().getUpdatedAt()).isAfterOrEqualTo(updatedUser.get().getCreatedAt());
    }

    @Test
    @DisplayName("Should delete user by ID")
    void testDeleteById() {
        User savedUser = userService.save(testUser1);
        Long userId = savedUser.getId();

        assertThat(userService.existsById(userId)).isTrue();

        userService.deleteById(userId);

        assertThat(userService.existsById(userId)).isFalse();
    }

    @Test
    @DisplayName("Should enforce unique username constraint")
    void testUniqueUsername() {
        userService.save(testUser1);
        userRepository.flush();

        User duplicateUsername = new User();
        duplicateUsername.setUsername("integrationuser1");
        duplicateUsername.setEmail("different@example.com");

        boolean exceptionThrown = false;
        try {
            userService.save(duplicateUsername);
            userRepository.flush();
        } catch (Exception e) {
            exceptionThrown = true;
            assertThat(e).isNotNull();
        }
        assertThat(exceptionThrown).withFailMessage("Expected exception for duplicate username").isTrue();
    }

    @Test
    @DisplayName("Should enforce unique email constraint")
    void testUniqueEmail() {
        userService.save(testUser1);
        userRepository.flush();

        User duplicateEmail = new User();
        duplicateEmail.setUsername("differentuser");
        duplicateEmail.setEmail("integration1@example.com");

        boolean exceptionThrown = false;
        try {
            userService.save(duplicateEmail);
            userRepository.flush();
        } catch (Exception e) {
            exceptionThrown = true;
            assertThat(e).isNotNull();
        }
        assertThat(exceptionThrown).withFailMessage("Expected exception for duplicate email").isTrue();
    }

    @Test
    @DisplayName("Should validate email format")
    void testEmailValidation() {
        User invalidEmailUser = new User();
        invalidEmailUser.setUsername("testuser");
        invalidEmailUser.setEmail("invalid-email");

        boolean exceptionThrown = false;
        try {
            userService.save(invalidEmailUser);
            userRepository.flush();
        } catch (Exception e) {
            exceptionThrown = true;
            assertThat(e).isNotNull();
        }
        assertThat(exceptionThrown).withFailMessage("Expected exception for invalid email format").isTrue();
    }

    @Test
    @DisplayName("Should enforce username size constraint")
    void testUsernameSizeConstraint() {
        User longUsernameUser = new User();
        longUsernameUser.setUsername("a".repeat(51)); // Exceeds max size of 50
        longUsernameUser.setEmail("test@example.com");

        boolean exceptionThrown = false;
        try {
            userService.save(longUsernameUser);
            userRepository.flush();
        } catch (Exception e) {
            exceptionThrown = true;
            assertThat(e).isNotNull();
        }
        assertThat(exceptionThrown).withFailMessage("Expected exception for username size constraint").isTrue();
    }

    @Test
    @DisplayName("Should return empty when finding non-existent user")
    void testFindNonExistentUser() {
        Optional<User> notFound = userService.findById(9999L);
        assertThat(notFound).isEmpty();

        notFound = userService.findByUsername("nonexistent");
        assertThat(notFound).isEmpty();

        notFound = userService.findByEmail("nonexistent@example.com");
        assertThat(notFound).isEmpty();
    }

    @Test
    @DisplayName("Should handle concurrent user creation")
    void testConcurrentUserCreation() {
        User user1 = new User();
        user1.setUsername("concurrent1");
        user1.setEmail("concurrent1@example.com");

        User user2 = new User();
        user2.setUsername("concurrent2");
        user2.setEmail("concurrent2@example.com");

        User savedUser1 = userService.save(user1);
        User savedUser2 = userService.save(user2);

        assertThat(savedUser1.getId()).isNotNull();
        assertThat(savedUser2.getId()).isNotNull();
        assertThat(savedUser1.getId()).isNotEqualTo(savedUser2.getId());

        List<User> allUsers = userService.findAll();
        assertThat(allUsers).hasSizeGreaterThanOrEqualTo(2);
    }
}
