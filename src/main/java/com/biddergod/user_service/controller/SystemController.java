package com.biddergod.user_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SystemController {

    /**
     * Health check endpoint
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", "OK");
        healthStatus.put("service", "users-service");
        healthStatus.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(healthStatus);
    }

    /**
     * Service info endpoint
     * GET /api/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> serviceInfo = new HashMap<>();
        serviceInfo.put("serviceName", "users-service");
        serviceInfo.put("version", "1.0.0");
        serviceInfo.put("features", new String[]{"reputation-system", "feedback-tracking", "cognito-auth"});
        return ResponseEntity.ok(serviceInfo);
    }

    /**
     * Welcome message (keep for backward compatibility)
     * GET /
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, String>> welcome() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to Users Service!");
        response.put("documentation", "/api/info");
        return ResponseEntity.ok(response);
    }
}