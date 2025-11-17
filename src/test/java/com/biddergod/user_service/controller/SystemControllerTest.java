package com.biddergod.user_service.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SystemController Tests")
class SystemControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private SystemController systemController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(systemController).build();
    }

    @Test
    @DisplayName("Should return OK status for health check")
    void health_ReturnsOkStatus() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.service").value("users-service"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Health check should return timestamp")
    void health_ReturnsTimestamp() {
        // When
        ResponseEntity<Map<String, Object>> response = systemController.health();

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("OK");
        assertThat(response.getBody().get("service")).isEqualTo("users-service");
        assertThat(response.getBody().get("timestamp")).isNotNull();
        assertThat(response.getBody().get("timestamp")).isInstanceOf(Long.class);
    }

    @Test
    @DisplayName("Should return service info")
    void info_ReturnsServiceInfo() throws Exception {
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceName").value("users-service"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.features").isArray())
                .andExpect(jsonPath("$.features[0]").value("reputation-system"))
                .andExpect(jsonPath("$.features[1]").value("feedback-tracking"))
                .andExpect(jsonPath("$.features[2]").value("cognito-auth"));
    }

    @Test
    @DisplayName("Info endpoint should return all required fields")
    void info_ReturnsAllFields() {
        // When
        ResponseEntity<Map<String, Object>> response = systemController.info();

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("serviceName")).isEqualTo("users-service");
        assertThat(response.getBody().get("version")).isEqualTo("1.0.0");
        assertThat(response.getBody().get("features")).isNotNull();
        
        String[] features = (String[]) response.getBody().get("features");
        assertThat(features).hasSize(3);
        assertThat(features).contains("reputation-system", "feedback-tracking", "cognito-auth");
    }

    @Test
    @DisplayName("Should return welcome message")
    void welcome_ReturnsWelcomeMessage() throws Exception {
        mockMvc.perform(get("/api/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Welcome to Users Service!"))
                .andExpect(jsonPath("$.documentation").value("/api/info"));
    }

    @Test
    @DisplayName("Welcome endpoint should return message and documentation link")
    void welcome_ReturnsMessageAndDocLink() {
        // When
        ResponseEntity<Map<String, String>> response = systemController.welcome();

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Welcome to Users Service!");
        assertThat(response.getBody().get("documentation")).isEqualTo("/api/info");
    }

    @Test
    @DisplayName("Health check timestamp should be recent")
    void health_TimestampIsRecent() {
        // When
        long beforeCall = System.currentTimeMillis();
        ResponseEntity<Map<String, Object>> response = systemController.health();
        long afterCall = System.currentTimeMillis();

        // Then
        Long timestamp = (Long) response.getBody().get("timestamp");
        assertThat(timestamp).isGreaterThanOrEqualTo(beforeCall);
        assertThat(timestamp).isLessThanOrEqualTo(afterCall);
    }
}
