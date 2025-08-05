package com.example.turnserver.controller;

import com.example.turnserver.service.TurnServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for TURN server operations and control
 */
@RestController
@RequestMapping("/api/turn")
@CrossOrigin(origins = "*")
public class TurnController {
    
    private static final Logger logger = LoggerFactory.getLogger(TurnController.class);
    
    private final TurnServerService turnServerService;
    
    @Autowired
    public TurnController(TurnServerService turnServerService) {
        this.turnServerService = turnServerService;
    }
    
    /**
     * Get TURN server status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getServerStatus() {
        logger.debug("Getting TURN server status");
        
        Map<String, Object> status = new HashMap<>();
        status.put("status", "running");
        status.put("timestamp", LocalDateTime.now());
        status.put("activeSessions", turnServerService.getActiveSessionCount());
        status.put("activeAllocations", turnServerService.getActiveAllocationCount());
        status.put("protocol", "TURN/STUN");
        status.put("version", "1.0.0");
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Get active sessions count
     */
    @GetMapping("/sessions/count")
    public ResponseEntity<Map<String, Object>> getActiveSessionsCount() {
        logger.debug("Getting active sessions count");
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", turnServerService.getActiveSessionCount());
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get active allocations count
     */
    @GetMapping("/allocations/count")
    public ResponseEntity<Map<String, Object>> getActiveAllocationsCount() {
        logger.debug("Getting active allocations count");
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", turnServerService.getActiveAllocationCount());
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Force cleanup of expired resources
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> forceCleanup() {
        logger.info("Forcing cleanup of expired resources");
        
        // Trigger cleanup manually
        turnServerService.cleanupExpiredResources();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cleanup initiated successfully");
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get server configuration info (read-only)
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getServerConfig() {
        logger.debug("Getting TURN server configuration");
        
        Map<String, Object> config = new HashMap<>();
        config.put("protocol", "TURN/STUN");
        config.put("authMethod", "long-term-credential");
        config.put("transportProtocols", new String[]{"UDP"});
        config.put("features", new String[]{
            "STUN Binding",
            "TURN Allocate",
            "TURN Refresh",
            "Authentication",
            "Message Integrity"
        });
        
        return ResponseEntity.ok(config);
    }
    
    /**
     * Get server capabilities
     */
    @GetMapping("/capabilities")
    public ResponseEntity<Map<String, Object>> getServerCapabilities() {
        logger.debug("Getting TURN server capabilities");
        
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("stunBinding", true);
        capabilities.put("turnAllocate", true);
        capabilities.put("turnRefresh", true);
        capabilities.put("turnChannelBind", false); // Not implemented yet
        capabilities.put("turnPermissions", false); // Not implemented yet
        capabilities.put("udpTransport", true);
        capabilities.put("tcpTransport", false); // Not implemented yet
        capabilities.put("tlsTransport", false); // Not implemented yet
        capabilities.put("authentication", true);
        capabilities.put("messageIntegrity", true);
        capabilities.put("fingerprint", false); // Not implemented yet
        
        return ResponseEntity.ok(capabilities);
    }
    
    /**
     * Test connectivity endpoint
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        logger.debug("Ping request received");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "pong");
        response.put("timestamp", LocalDateTime.now());
        response.put("server", "Netty TURN Server");
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get server information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getServerInfo() {
        logger.debug("Getting TURN server information");
        
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Netty TURN Server");
        info.put("version", "1.0.0");
        info.put("description", "A high-performance TURN/STUN server implementation based on Netty and Spring Boot");
        info.put("author", "IndigoCloud6");
        info.put("license", "MIT");
        info.put("protocols", new String[]{"STUN", "TURN"});
        info.put("transports", new String[]{"UDP"});
        info.put("features", new String[]{
            "User Management",
            "Session Management", 
            "Allocation Management",
            "Statistics & Monitoring",
            "REST API",
            "Authentication & Authorization"
        });
        
        return ResponseEntity.ok(info);
    }
}