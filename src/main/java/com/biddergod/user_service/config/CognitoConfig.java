package com.biddergod.user_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cognito")
public class CognitoConfig {

    private String region;
    private String userPoolId;
    private String clientId;

    // Getters and Setters
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getUserPoolId() { return userPoolId; }
    public void setUserPoolId(String userPoolId) { this.userPoolId = userPoolId; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getIssuerUri() {
        return String.format("https://cognito-idp.%s.amazonaws.com/%s", region, userPoolId);
    }
}