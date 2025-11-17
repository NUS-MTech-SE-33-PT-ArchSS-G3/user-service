package com.biddergod.user_service.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User Entity Validation Tests")
class UserEntityTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should validate user with all required fields")
    void testValidUser() {
        User user = new User();
        user.setUsername("validuser");
        user.setEmail("valid@example.com");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            violations.forEach(v -> System.out.println(v.getMessage() + ": " + v.getPropertyPath()));
        }
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when username is blank")
    void testBlankUsername() {
        User user = new User();
        user.setUsername("");
        user.setEmail("test@example.com");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("must not be blank"));
    }

    @Test
    @DisplayName("Should fail validation when username is null")
    void testNullUsername() {
        User user = new User();
        user.setUsername(null);
        user.setEmail("test@example.com");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Should fail validation when email is blank")
    void testBlankEmail() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Should fail validation when email is null")
    void testNullEmail() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Should fail validation when email is invalid format")
    void testInvalidEmailFormat() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("invalid-email");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("must be a well-formed email address"));
    }

    @Test
    @DisplayName("Should fail validation when username exceeds max size")
    void testUsernameTooLong() {
        User user = new User();
        user.setUsername("a".repeat(51)); // Exceeds max size of 50
        user.setEmail("test@example.com");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("size must be between"));
    }

    @Test
    @DisplayName("Should fail validation when email exceeds max size")
    void testEmailTooLong() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("a".repeat(91) + "@example.com"); // Exceeds max size of 100

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Should fail validation when firstName exceeds max size")
    void testFirstNameTooLong() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("a".repeat(101)); // Exceeds max size of 100

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("size must be between"));
    }

    @Test
    @DisplayName("Should fail validation when lastName exceeds max size")
    void testLastNameTooLong() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setLastName("a".repeat(101)); // Exceeds max size of 100

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Should allow null firstName and lastName")
    void testNullOptionalFields() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName(null);
        user.setLastName(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should validate user with maximum allowed field sizes")
    void testMaximumFieldSizes() {
        User user = new User();
        user.setUsername("a".repeat(50)); // Max size
        user.setEmail("a".repeat(20) + "@example.com"); // Valid email under 100 chars
        user.setFirstName("a".repeat(100)); // Max size
        user.setLastName("a".repeat(100)); // Max size

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            violations.forEach(v -> System.out.println(v.getMessage() + ": " + v.getPropertyPath()));
        }
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should accept various valid email formats")
    void testValidEmailFormats() {
        String[] validEmails = {
                "simple@example.com",
                "user+tag@example.com",
                "user.name@example.co.uk",
                "user_name@example.com",
                "123@example.com",
                "a@b.c"
        };

        for (String email : validEmails) {
            User user = new User();
            user.setUsername("testuser");
            user.setEmail(email);

            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).isEmpty();
        }
    }

    @Test
    @DisplayName("Should reject invalid email formats")
    void testInvalidEmailFormats() {
        String[] invalidEmails = {
                "invalid",
                "@example.com",
                "user@",
                "user @example.com"
        };

        for (String email : invalidEmails) {
            User user = new User();
            user.setUsername("testuser");
            user.setEmail(email);

            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).as("Expected violations for email: " + email).isNotEmpty();
        }
    }

    @Test
    @DisplayName("Should test User constructor with username and email")
    void testUserConstructor() {
        User user = new User("testuser", "test@example.com");

        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getId()).isNull();
        assertThat(user.getFirstName()).isNull();
        assertThat(user.getLastName()).isNull();
    }

    @Test
    @DisplayName("Should test default constructor")
    void testDefaultConstructor() {
        User user = new User();

        assertThat(user).isNotNull();
        assertThat(user.getId()).isNull();
        assertThat(user.getUsername()).isNull();
        assertThat(user.getEmail()).isNull();
    }

    @Test
    @DisplayName("Should test all getters and setters")
    void testGettersAndSetters() {
        User user = new User();

        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
    }
}
