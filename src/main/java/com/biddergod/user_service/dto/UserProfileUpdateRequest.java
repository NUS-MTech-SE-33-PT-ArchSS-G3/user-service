package com.biddergod.user_service.dto;

import jakarta.validation.constraints.Size;

public class UserProfileUpdateRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    // Optional ID token for enhanced user profile information
    private String idToken;

    // Constructors
    public UserProfileUpdateRequest() {}

    public UserProfileUpdateRequest(String firstName, String lastName, String idToken) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.idToken = idToken;
    }

    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }
}