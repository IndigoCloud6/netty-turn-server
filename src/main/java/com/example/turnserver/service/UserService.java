package com.example.turnserver.service;

import com.example.turnserver.dto.UserCreateRequest;
import com.example.turnserver.dto.UserResponse;
import com.example.turnserver.exception.TurnException;
import com.example.turnserver.model.User;
import com.example.turnserver.repository.UserRepository;
import com.example.turnserver.util.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing TURN server users
 */
@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Create a new user
     */
    public UserResponse createUser(UserCreateRequest request) {
        logger.info("Creating user: {}", request.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw TurnException.badRequest("Username already exists: " + request.getUsername());
        }
        
        // Create user entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(CryptoUtils.hashPassword(request.getPassword()));
        user.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        
        // Save user
        User savedUser = userRepository.save(user);
        
        logger.info("Successfully created user with ID: {}", savedUser.getId());
        return UserResponse.fromUser(savedUser);
    }
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> TurnException.badRequest("User not found with ID: " + id));
        return UserResponse.fromUser(user);
    }
    
    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> TurnException.badRequest("User not found with username: " + username));
        return UserResponse.fromUser(user);
    }
    
    /**
     * Get all users with pagination
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserResponse::fromUser);
    }
    
    /**
     * Get all active users
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getActiveUsers() {
        return userRepository.findByEnabledTrue()
                .stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }
    
    /**
     * Update user
     */
    public UserResponse updateUser(Long id, UserCreateRequest request) {
        logger.info("Updating user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> TurnException.badRequest("User not found with ID: " + id));
        
        // Check if new username conflicts with existing users
        if (!user.getUsername().equals(request.getUsername()) && 
            userRepository.existsByUsername(request.getUsername())) {
            throw TurnException.badRequest("Username already exists: " + request.getUsername());
        }
        
        // Update user fields
        user.setUsername(request.getUsername());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(CryptoUtils.hashPassword(request.getPassword()));
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        
        User savedUser = userRepository.save(user);
        
        logger.info("Successfully updated user with ID: {}", savedUser.getId());
        return UserResponse.fromUser(savedUser);
    }
    
    /**
     * Delete user by ID
     */
    public void deleteUser(Long id) {
        logger.info("Deleting user with ID: {}", id);
        
        if (!userRepository.existsById(id)) {
            throw TurnException.badRequest("User not found with ID: " + id);
        }
        
        userRepository.deleteById(id);
        logger.info("Successfully deleted user with ID: {}", id);
    }
    
    /**
     * Enable/disable user
     */
    public UserResponse setUserEnabled(Long id, boolean enabled) {
        logger.info("Setting user {} enabled status to: {}", id, enabled);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> TurnException.badRequest("User not found with ID: " + id));
        
        user.setEnabled(enabled);
        User savedUser = userRepository.save(user);
        
        logger.info("Successfully updated enabled status for user with ID: {}", savedUser.getId());
        return UserResponse.fromUser(savedUser);
    }
    
    /**
     * Update user's last login time
     */
    public void updateLastLogin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            logger.debug("Updated last login for user: {}", username);
        }
    }
    
    /**
     * Search users by username pattern
     */
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String pattern) {
        return userRepository.findByUsernameContainingIgnoreCase(pattern)
                .stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }
    
    /**
     * Get user count statistics
     */
    @Transactional(readOnly = true)
    public long getTotalUserCount() {
        return userRepository.count();
    }
    
    @Transactional(readOnly = true)
    public long getActiveUserCount() {
        return userRepository.countActiveUsers();
    }
    
    @Transactional(readOnly = true)
    public long getUsersCreatedToday() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        return userRepository.findByCreatedAtAfter(startOfDay).size();
    }
    
    /**
     * Verify user exists and is enabled (for authentication)
     */
    @Transactional(readOnly = true)
    public boolean isUserActiveByUsername(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        return userOpt.isPresent() && userOpt.get().getEnabled();
    }
    
    /**
     * Get user entity by username (for internal use)
     */
    @Transactional(readOnly = true)
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}