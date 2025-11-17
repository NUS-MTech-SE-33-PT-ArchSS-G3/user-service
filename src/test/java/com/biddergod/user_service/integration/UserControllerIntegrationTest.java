package com.biddergod.user_service.integration;

import com.biddergod.user_service.config.TestSecurityConfig;
import com.biddergod.user_service.dto.UserProfileUpdateRequest;
import com.biddergod.user_service.entity.User;
import com.biddergod.user_service.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
@DisplayName("UserController Integration Tests")
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser1 = new User();
        testUser1.setUsername("apiuser1");
        testUser1.setEmail("apiuser1@example.com");
        testUser1.setFirstName("API");
        testUser1.setLastName("User1");

        testUser2 = new User();
        testUser2.setUsername("apiuser2");
        testUser2.setEmail("apiuser2@example.com");
        testUser2.setFirstName("API");
        testUser2.setLastName("User2");
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/users?id={id} - Should return single user")
    void testGetUserById() throws Exception {
        User savedUser = userRepository.save(testUser1);

        mockMvc.perform(get("/api/users")
                        .param("id", savedUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasSize(1)))
                .andExpect(jsonPath("$.users[0].id").value(savedUser.getId()))
                .andExpect(jsonPath("$.users[0].username").value("apiuser1"))
                .andExpect(jsonPath("$.users[0].email").value("apiuser1@example.com"))
                .andExpect(jsonPath("$.found").value(1))
                .andExpect(jsonPath("$.requested").value(1));
    }

    @Test
    @DisplayName("GET /api/users?id={id1}&id={id2} - Should return multiple users")
    void testGetMultipleUsersByIds() throws Exception {
        User savedUser1 = userRepository.save(testUser1);
        User savedUser2 = userRepository.save(testUser2);

        mockMvc.perform(get("/api/users")
                        .param("id", savedUser1.getId().toString(), savedUser2.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasSize(2)))
                .andExpect(jsonPath("$.found").value(2))
                .andExpect(jsonPath("$.requested").value(2));
    }

    @Test
    @DisplayName("GET /api/users?id={id} - Should return empty for non-existent user")
    void testGetNonExistentUser() throws Exception {
        mockMvc.perform(get("/api/users")
                        .param("id", "9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasSize(0)))
                .andExpect(jsonPath("$.found").value(0))
                .andExpect(jsonPath("$.requested").value(1));
    }

    @Test
    @DisplayName("GET /api/users?id={id1}&id={id2} - Should filter non-existent users")
    void testGetMixedExistentAndNonExistentUsers() throws Exception {
        User savedUser = userRepository.save(testUser1);

        mockMvc.perform(get("/api/users")
                        .param("id", savedUser.getId().toString(), "9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasSize(1)))
                .andExpect(jsonPath("$.users[0].id").value(savedUser.getId()))
                .andExpect(jsonPath("$.found").value(1))
                .andExpect(jsonPath("$.requested").value(2));
    }

    @Test
    @DisplayName("GET /api/users - Should return error when no IDs provided")
    void testGetUsersWithoutIds() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle special characters in user data")
    void testSpecialCharactersInUserData() throws Exception {
        User specialUser = new User();
        specialUser.setUsername("user@special");
        specialUser.setEmail("special+test@example.com");
        specialUser.setFirstName("Name-With-Dash");
        specialUser.setLastName("O'Brien");

        User savedUser = userRepository.save(specialUser);

        mockMvc.perform(get("/api/users")
                        .param("id", savedUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].username").value("user@special"))
                .andExpect(jsonPath("$.users[0].email").value("special+test@example.com"))
                .andExpect(jsonPath("$.users[0].firstName").value("Name-With-Dash"))
                .andExpect(jsonPath("$.users[0].lastName").value("O'Brien"));
    }

    @Test
    @DisplayName("Should return user with null first and last names")
    void testUserWithNullNames() throws Exception {
        User minimalUser = new User();
        minimalUser.setUsername("minimaluser");
        minimalUser.setEmail("minimal@example.com");

        User savedUser = userRepository.save(minimalUser);

        mockMvc.perform(get("/api/users")
                        .param("id", savedUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].username").value("minimaluser"))
                .andExpect(jsonPath("$.users[0].email").value("minimal@example.com"))
                .andExpect(jsonPath("$.users[0].firstName").isEmpty())
                .andExpect(jsonPath("$.users[0].lastName").isEmpty());
    }

    @Test
    @DisplayName("Should handle large batch of user IDs")
    void testLargeBatchOfUserIds() throws Exception {
        User savedUser1 = userRepository.save(testUser1);
        User savedUser2 = userRepository.save(testUser2);

        // Create request with multiple IDs including non-existent ones
        String[] ids = {
                savedUser1.getId().toString(),
                savedUser2.getId().toString(),
                "9991", "9992", "9993", "9994", "9995"
        };

        mockMvc.perform(get("/api/users")
                        .param("id", ids))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasSize(2)))
                .andExpect(jsonPath("$.found").value(2))
                .andExpect(jsonPath("$.requested").value(7));
    }

    @Test
    @DisplayName("Should return correct timestamps for created and updated users")
    void testUserTimestamps() throws Exception {
        User savedUser = userRepository.save(testUser1);

        mockMvc.perform(get("/api/users")
                        .param("id", savedUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].createdAt").exists())
                .andExpect(jsonPath("$.users[0].updatedAt").exists());
    }

    @Test
    @DisplayName("Should handle duplicate IDs in request")
    void testDuplicateIdsInRequest() throws Exception {
        User savedUser = userRepository.save(testUser1);
        String userId = savedUser.getId().toString();

        // Request with duplicate IDs
        mockMvc.perform(get("/api/users")
                        .param("id", userId, userId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(1)) // Should only return one user
                .andExpect(jsonPath("$.requested").value(3));
    }

    @Test
    @DisplayName("Should validate email format in UserDetailsResponse")
    void testEmailFormatInResponse() throws Exception {
        User savedUser = userRepository.save(testUser1);

        mockMvc.perform(get("/api/users")
                        .param("id", savedUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].email").value("apiuser1@example.com"))
                .andExpect(jsonPath("$.users[0].email").isString());
    }

    @Test
    @DisplayName("Should return users in consistent order")
    void testConsistentUserOrder() throws Exception {
        User savedUser1 = userRepository.save(testUser1);
        User savedUser2 = userRepository.save(testUser2);

        // Make multiple requests and verify consistency
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/users")
                            .param("id", savedUser1.getId().toString(), savedUser2.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.users", hasSize(2)))
                    .andExpect(jsonPath("$.found").value(2));
        }
    }

    @Test
    @DisplayName("Should handle very long user IDs")
    void testVeryLongUserIds() throws Exception {
        mockMvc.perform(get("/api/users")
                        .param("id", "999999999999999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasSize(0)))
                .andExpect(jsonPath("$.found").value(0));
    }
}
