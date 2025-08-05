package com.example.turnserver.controller;

import com.example.turnserver.dto.StatisticsResponse;
import com.example.turnserver.service.StatisticsService;
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
 * REST controller for TURN server statistics
 */
@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {
    
    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);
    
    private final StatisticsService statisticsService;
    private final TurnServerService turnServerService;
    
    @Autowired
    public StatisticsController(StatisticsService statisticsService, TurnServerService turnServerService) {
        this.statisticsService = statisticsService;
        this.turnServerService = turnServerService;
    }
    
    /**
     * Get comprehensive server statistics
     */
    @GetMapping
    public ResponseEntity<StatisticsResponse> getStatistics() {
        logger.debug("Getting comprehensive server statistics");
        StatisticsResponse stats = statisticsService.getStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get user statistics only
     */
    @GetMapping("/users")
    public ResponseEntity<StatisticsResponse> getUserStatistics() {
        logger.debug("Getting user statistics");
        StatisticsResponse stats = statisticsService.getUserStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get session statistics only
     */
    @GetMapping("/sessions")
    public ResponseEntity<StatisticsResponse> getSessionStatistics() {
        logger.debug("Getting session statistics");
        StatisticsResponse stats = statisticsService.getSessionStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get allocation statistics only
     */
    @GetMapping("/allocations")
    public ResponseEntity<StatisticsResponse> getAllocationStatistics() {
        logger.debug("Getting allocation statistics");
        StatisticsResponse stats = statisticsService.getAllocationStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get server summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getServerSummary() {
        logger.debug("Getting server summary");
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("serverStartTime", statisticsService.getServerStartTime());
        summary.put("uptimeSeconds", statisticsService.getUptimeSeconds());
        summary.put("activeSessions", turnServerService.getActiveSessionCount());
        summary.put("activeAllocations", turnServerService.getActiveAllocationCount());
        summary.put("version", "1.0.0");
        summary.put("status", "running");
        
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Get health check information
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthCheck() {
        logger.debug("Getting health check information");
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("uptime", statisticsService.getUptimeSeconds());
        health.put("activeSessions", turnServerService.getActiveSessionCount());
        health.put("activeAllocations", turnServerService.getActiveAllocationCount());
        
        // Basic health indicators
        Map<String, Object> details = new HashMap<>();
        details.put("database", "UP"); // Would check database connectivity in real implementation
        details.put("memory", getMemoryInfo());
        details.put("disk", "UP"); // Would check disk space in real implementation
        
        health.put("details", details);
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Get performance metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        logger.debug("Getting performance metrics");
        
        Map<String, Object> metrics = new HashMap<>();
        
        // JVM metrics
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> jvm = new HashMap<>();
        jvm.put("maxMemory", runtime.maxMemory());
        jvm.put("totalMemory", runtime.totalMemory());
        jvm.put("freeMemory", runtime.freeMemory());
        jvm.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        jvm.put("availableProcessors", runtime.availableProcessors());
        
        metrics.put("jvm", jvm);
        
        // Server metrics
        Map<String, Object> server = new HashMap<>();
        server.put("uptime", statisticsService.getUptimeSeconds());
        server.put("activeSessions", turnServerService.getActiveSessionCount());
        server.put("activeAllocations", turnServerService.getActiveAllocationCount());
        
        metrics.put("server", server);
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Get real-time activity
     */
    @GetMapping("/activity")
    public ResponseEntity<Map<String, Object>> getActivity() {
        logger.debug("Getting real-time activity");
        
        Map<String, Object> activity = new HashMap<>();
        activity.put("timestamp", LocalDateTime.now());
        activity.put("activeSessions", turnServerService.getActiveSessionCount());
        activity.put("activeAllocations", turnServerService.getActiveAllocationCount());
        
        // Would include more real-time metrics in a full implementation
        activity.put("requestsPerSecond", 0); // Placeholder
        activity.put("bytesPerSecond", 0); // Placeholder
        activity.put("packetsPerSecond", 0); // Placeholder
        
        return ResponseEntity.ok(activity);
    }
    
    private Map<String, Object> getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        memory.put("max", maxMemory);
        memory.put("total", totalMemory);
        memory.put("used", usedMemory);
        memory.put("free", freeMemory);
        memory.put("usagePercent", (double) usedMemory / maxMemory * 100);
        
        return memory;
    }
}