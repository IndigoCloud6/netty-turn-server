package com.example.turnserver.dto;

import com.example.turnserver.model.User;

import java.time.LocalDateTime;

/**
 * Response DTO for user information
 */
public class UserResponse {
    
    private Long id;
    private String username;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
    
    // Constructors
    public UserResponse() {}
    
    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.enabled = user.getEnabled();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.lastLogin = user.getLastLogin();
    }
    
    public UserResponse(Long id, String username, Boolean enabled, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastLogin) {
        this.id = id;
        this.username = username;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLogin = lastLogin;
    }
    
    // Static factory method
    public static UserResponse fromUser(User user) {
        return new UserResponse(user);
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", enabled=" + enabled +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", lastLogin=" + lastLogin +
                '}';
    }
}