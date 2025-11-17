package com.biddergod.user_service.dto;

import com.biddergod.user_service.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserDetailsResponse Tests")
class UserDetailsResponseTest {

    private User testUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @BeforeEach
    void setUp() {
        createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        updatedAt = LocalDateTime.of(2024, 1, 15, 14, 30);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setCreatedAt(createdAt);
        testUser.setUpdatedAt(updatedAt);
    }

    @Test
    @DisplayName("Should create UserDetailsResponse from User entity")
    void constructor_FromUser_CopiesAllFields() {
        // When
        UserDetailsResponse response = new UserDetailsResponse(testUser);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
        assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("Should create empty UserDetailsResponse with default constructor")
    void defaultConstructor_CreatesEmptyObject() {
        // When
        UserDetailsResponse response = new UserDetailsResponse();

        // Then
        assertThat(response.getId()).isNull();
        assertThat(response.getUsername()).isNull();
        assertThat(response.getEmail()).isNull();
        assertThat(response.getFirstName()).isNull();
        assertThat(response.getLastName()).isNull();
        assertThat(response.getCreatedAt()).isNull();
        assertThat(response.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should set and get all fields correctly")
    void settersAndGetters_AllFields_Work() {
        // Given
        UserDetailsResponse response = new UserDetailsResponse();
        LocalDateTime now = LocalDateTime.now();

        // When
        response.setId(42L);
        response.setUsername("newuser");
        response.setEmail("new@example.com");
        response.setFirstName("Jane");
        response.setLastName("Smith");
        response.setCreatedAt(now);
        response.setUpdatedAt(now);

        // Then
        assertThat(response.getId()).isEqualTo(42L);
        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getEmail()).isEqualTo("new@example.com");
        assertThat(response.getFirstName()).isEqualTo("Jane");
        assertThat(response.getLastName()).isEqualTo("Smith");
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should return full name when both first and last names exist")
    void getFullName_BothNamesExist_ReturnsFullName() {
        // Given
        UserDetailsResponse response = new UserDetailsResponse(testUser);

        // When
        String fullName = response.getFullName();

        // Then
        assertThat(fullName).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should return first name when last name is null")
    void getFullName_OnlyFirstName_ReturnsFirstName() {
        // Given
        testUser.setLastName(null);
        UserDetailsResponse response = new UserDetailsResponse(testUser);

        // When
        String fullName = response.getFullName();

        // Then
        assertThat(fullName).isEqualTo("John");
    }

    @Test
    @DisplayName("Should return last name when first name is null")
    void getFullName_OnlyLastName_ReturnsLastName() {
        // Given
        testUser.setFirstName(null);
        UserDetailsResponse response = new UserDetailsResponse(testUser);

        // When
        String fullName = response.getFullName();

        // Then
        assertThat(fullName).isEqualTo("Doe");
    }

    @Test
    @DisplayName("Should return username when both names are null")
    void getFullName_NoNames_ReturnsUsername() {
        // Given
        testUser.setFirstName(null);
        testUser.setLastName(null);
        UserDetailsResponse response = new UserDetailsResponse(testUser);

        // When
        String fullName = response.getFullName();

        // Then
        assertThat(fullName).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should handle User with null values")
    void constructor_UserWithNulls_HandlesGracefully() {
        // Given
        User minimalUser = new User();
        minimalUser.setId(5L);
        minimalUser.setUsername("minimaluser");

        // When
        UserDetailsResponse response = new UserDetailsResponse(minimalUser);

        // Then
        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getUsername()).isEqualTo("minimaluser");
        assertThat(response.getEmail()).isNull();
        assertThat(response.getFirstName()).isNull();
        assertThat(response.getLastName()).isNull();
        assertThat(response.getCreatedAt()).isNull();
        assertThat(response.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should handle null values in setters")
    void setters_NullValues_AcceptsNulls() {
        // Given
        UserDetailsResponse response = new UserDetailsResponse(testUser);

        // When
        response.setId(null);
        response.setUsername(null);
        response.setEmail(null);
        response.setFirstName(null);
        response.setLastName(null);
        response.setCreatedAt(null);
        response.setUpdatedAt(null);

        // Then
        assertThat(response.getId()).isNull();
        assertThat(response.getUsername()).isNull();
        assertThat(response.getEmail()).isNull();
        assertThat(response.getFirstName()).isNull();
        assertThat(response.getLastName()).isNull();
        assertThat(response.getCreatedAt()).isNull();
        assertThat(response.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should preserve exact timestamp values")
    void timestamps_PreserveExactValues() {
        // Given
        LocalDateTime specificTime = LocalDateTime.of(2024, 6, 15, 9, 30, 45, 123456789);
        UserDetailsResponse response = new UserDetailsResponse();

        // When
        response.setCreatedAt(specificTime);
        response.setUpdatedAt(specificTime);

        // Then
        assertThat(response.getCreatedAt()).isEqualTo(specificTime);
        assertThat(response.getUpdatedAt()).isEqualTo(specificTime);
    }
}
