package com.example.turnserver.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Request DTO for creating a new user
 */
public class UserCreateRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;
    
    private Boolean enabled = true;
    
    // Constructors
    public UserCreateRequest() {}
    
    public UserCreateRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public UserCreateRequest(String username, String password, Boolean enabled) {
        this.username = username;
        this.password = password;
        this.enabled = enabled;
    }
    
    // Getters and setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public String toString() {
        return "UserCreateRequest{" +
                "username='" + username + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}