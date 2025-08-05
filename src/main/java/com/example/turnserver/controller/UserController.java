package com.example.turnserver.controller;

import com.example.turnserver.dto.UserCreateRequest;
import com.example.turnserver.dto.UserResponse;
import com.example.turnserver.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for user management
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Create a new user
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        logger.info("Creating new user: {}", request.getUsername());
        UserResponse user = userService.createUser(request);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
    
    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        logger.debug("Getting user by ID: {}", id);
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Get user by username
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        logger.debug("Getting user by username: {}", username);
        UserResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Get all users with pagination
     */
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        
        logger.debug("Getting users - page: {}, size: {}, sort: {}, direction: {}", 
                     page, size, sort, direction);
        
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get all active users
     */
    @GetMapping("/active")
    public ResponseEntity<List<UserResponse>> getActiveUsers() {
        logger.debug("Getting active users");
        List<UserResponse> users = userService.getActiveUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * Update user
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, 
                                                  @Valid @RequestBody UserCreateRequest request) {
        logger.info("Updating user with ID: {}", id);
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Delete user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        logger.info("Deleting user with ID: {}", id);
        userService.deleteUser(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User deleted successfully");
        response.put("id", id);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Enable user
     */
    @PostMapping("/{id}/enable")
    public ResponseEntity<UserResponse> enableUser(@PathVariable Long id) {
        logger.info("Enabling user with ID: {}", id);
        UserResponse user = userService.setUserEnabled(id, true);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Disable user
     */
    @PostMapping("/{id}/disable")
    public ResponseEntity<UserResponse> disableUser(@PathVariable Long id) {
        logger.info("Disabling user with ID: {}", id);
        UserResponse user = userService.setUserEnabled(id, false);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Search users by username pattern
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String q) {
        logger.debug("Searching users with pattern: {}", q);
        List<UserResponse> users = userService.searchUsers(q);
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get user statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        logger.debug("Getting user statistics");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.getTotalUserCount());
        stats.put("activeUsers", userService.getActiveUserCount());
        stats.put("usersCreatedToday", userService.getUsersCreatedToday());
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Validate username availability
     */
    @GetMapping("/validate-username/{username}")
    public ResponseEntity<Map<String, Object>> validateUsername(@PathVariable String username) {
        logger.debug("Validating username availability: {}", username);
        
        boolean available = !userService.isUserActiveByUsername(username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("available", available);
        
        return ResponseEntity.ok(response);
    }
}