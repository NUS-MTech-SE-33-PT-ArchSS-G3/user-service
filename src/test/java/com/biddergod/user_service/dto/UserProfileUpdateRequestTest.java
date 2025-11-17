package com.biddergod.user_service.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserProfileUpdateRequest Tests")
class UserProfileUpdateRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create request with default constructor")
    void defaultConstructor_CreatesEmptyObject() {
        // When
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();

        // Then
        assertThat(request.getFirstName()).isNull();
        assertThat(request.getLastName()).isNull();
        assertThat(request.getIdToken()).isNull();
    }

    @Test
    @DisplayName("Should create request with all parameters")
    void parameterizedConstructor_SetsAllFields() {
        // When
        UserProfileUpdateRequest request = new UserProfileUpdateRequest(
            "John",
            "Doe",
            "id-token-123"
        );

        // Then
        assertThat(request.getFirstName()).isEqualTo("John");
        assertThat(request.getLastName()).isEqualTo("Doe");
        assertThat(request.getIdToken()).isEqualTo("id-token-123");
    }

    @Test
    @DisplayName("Should set and get first name")
    void setFirstName_AndGet_Works() {
        // Given
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();

        // When
        request.setFirstName("Jane");

        // Then
        assertThat(request.getFirstName()).isEqualTo("Jane");
    }

    @Test
    @DisplayName("Should set and get last name")
    void setLastName_AndGet_Works() {
        // Given
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();

        // When
        request.setLastName("Smith");

        // Then
        assertThat(request.getLastName()).isEqualTo("Smith");
    }

    @Test
    @DisplayName("Should set and get ID token")
    void setIdToken_AndGet_Works() {
        // Given
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();

        // When
        request.setIdToken("token-xyz");

        // Then
        assertThat(request.getIdToken()).isEqualTo("token-xyz");
    }

    @Test
    @DisplayName("Should validate first name with 100 characters")
    void validation_FirstName100Chars_Valid() {
        // Given
        String name = "A".repeat(100);
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFirstName(name);

        // When
        Set<ConstraintViolation<UserProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should validate last name with 100 characters")
    void validation_LastName100Chars_Valid() {
        // Given
        String name = "B".repeat(100);
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setLastName(name);

        // When
        Set<ConstraintViolation<UserProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when first name exceeds 100 characters")
    void validation_FirstNameTooLong_Invalid() {
        // Given
        String name = "A".repeat(101);
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFirstName(name);

        // When
        Set<ConstraintViolation<UserProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("100");
    }

    @Test
    @DisplayName("Should fail validation when last name exceeds 100 characters")
    void validation_LastNameTooLong_Invalid() {
        // Given
        String name = "B".repeat(101);
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setLastName(name);

        // When
        Set<ConstraintViolation<UserProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("100");
    }

    @Test
    @DisplayName("Should accept null values for all fields")
    void nullValues_AllFields_Valid() {
        // Given
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFirstName(null);
        request.setLastName(null);
        request.setIdToken(null);

        // When
        Set<ConstraintViolation<UserProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.getFirstName()).isNull();
        assertThat(request.getLastName()).isNull();
        assertThat(request.getIdToken()).isNull();
    }

    @Test
    @DisplayName("Should accept empty strings for all fields")
    void emptyStrings_AllFields_Valid() {
        // Given
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFirstName("");
        request.setLastName("");
        request.setIdToken("");

        // When
        Set<ConstraintViolation<UserProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.getFirstName()).isEmpty();
        assertThat(request.getLastName()).isEmpty();
        assertThat(request.getIdToken()).isEmpty();
    }

    @Test
    @DisplayName("Should handle all fields populated")
    void allFieldsPopulated_Valid() {
        // Given
        UserProfileUpdateRequest request = new UserProfileUpdateRequest(
            "Alexander",
            "Hamilton",
            "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0"
        );

        // When
        Set<ConstraintViolation<UserProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.getFirstName()).isEqualTo("Alexander");
        assertThat(request.getLastName()).isEqualTo("Hamilton");
        assertThat(request.getIdToken()).isNotNull();
    }

    @Test
    @DisplayName("Should handle special characters in names")
    void specialCharactersInNames_Valid() {
        // Given
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFirstName("María-José");
        request.setLastName("O'Brien-Smith");

        // When
        Set<ConstraintViolation<UserProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.getFirstName()).isEqualTo("María-José");
        assertThat(request.getLastName()).isEqualTo("O'Brien-Smith");
    }

    @Test
    @DisplayName("Should validate only first name provided")
    void onlyFirstName_Valid() {
        // Given
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFirstName("SingleName");

        // When
        Set<ConstraintViolation<UserProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.getFirstName()).isEqualTo("SingleName");
        assertThat(request.getLastName()).isNull();
        assertThat(request.getIdToken()).isNull();
    }

    @Test
    @DisplayName("Should validate only ID token provided")
    void onlyIdToken_Valid() {
        // Given
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setIdToken("token-only");

        // When
        Set<ConstraintViolation<UserProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.getFirstName()).isNull();
        assertThat(request.getLastName()).isNull();
        assertThat(request.getIdToken()).isEqualTo("token-only");
    }
}
