package com.example.turnserver.service;

import com.example.turnserver.dto.StatisticsResponse;
import com.example.turnserver.repository.AllocationRepository;
import com.example.turnserver.repository.TurnSessionRepository;
import com.example.turnserver.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Service for collecting and providing TURN server statistics
 */
@Service
@Transactional(readOnly = true)
public class StatisticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);
    
    private final UserRepository userRepository;
    private final TurnSessionRepository sessionRepository;
    private final AllocationRepository allocationRepository;
    
    private final LocalDateTime serverStartTime;
    private final int minPort;
    private final int maxPort;
    
    @Autowired
    public StatisticsService(UserRepository userRepository,
                           TurnSessionRepository sessionRepository,
                           AllocationRepository allocationRepository,
                           @Value("${turn.server.min-port:49152}") int minPort,
                           @Value("${turn.server.max-port:65535}") int maxPort) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.allocationRepository = allocationRepository;
        this.serverStartTime = LocalDateTime.now();
        this.minPort = minPort;
        this.maxPort = maxPort;
    }
    
    /**
     * Get comprehensive server statistics
     */
    public StatisticsResponse getStatistics() {
        logger.debug("Collecting server statistics");
        
        StatisticsResponse stats = new StatisticsResponse();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        
        // User statistics
        stats.setTotalUsers(userRepository.count());
        stats.setActiveUsers(userRepository.countActiveUsers());
        stats.setUsersCreatedToday(userRepository.findByCreatedAtAfter(startOfDay).size());
        
        // Session statistics
        stats.setActiveSessions(sessionRepository.countActiveSessions(now));
        stats.setTotalSessions(sessionRepository.count());
        stats.setSessionsCreatedToday(sessionRepository.findByLastActivityAfter(startOfDay).size());
        
        // Calculate bytes/packets transferred (this would need proper implementation)
        // For now, using repository methods that would need to be implemented
        stats.setTotalBytesSent(calculateTotalBytesSent());
        stats.setTotalBytesReceived(calculateTotalBytesReceived());
        stats.setTotalPacketsSent(calculateTotalPacketsSent());
        stats.setTotalPacketsReceived(calculateTotalPacketsReceived());
        
        // Allocation statistics
        stats.setActiveAllocations(allocationRepository.countActiveAllocations(now));
        stats.setTotalAllocations(allocationRepository.count());
        stats.setAllocationsCreatedToday(allocationRepository.findActiveAllocations(startOfDay).size());
        
        Long totalBytesRelayed = allocationRepository.getTotalBytesRelayed();
        stats.setTotalBytesRelayed(totalBytesRelayed != null ? totalBytesRelayed : 0L);
        
        Long totalPacketsRelayed = allocationRepository.getTotalPacketsRelayed();
        stats.setTotalPacketsRelayed(totalPacketsRelayed != null ? totalPacketsRelayed : 0L);
        
        // Server statistics
        stats.setServerStartTime(serverStartTime);
        stats.setUptimeSeconds(Duration.between(serverStartTime, now).getSeconds());
        stats.setServerVersion("1.0.0");
        stats.setConfiguredPortRange(maxPort - minPort + 1);
        stats.setAvailablePorts(calculateAvailablePorts(now));
        
        // Performance metrics (simplified calculations)
        stats.setAvgSessionDuration(calculateAverageSessionDuration());
        stats.setAvgAllocationLifetime(calculateAverageAllocationLifetime());
        stats.setPeakConcurrentSessions(stats.getActiveSessions()); // Simplified
        stats.setPeakConcurrentAllocations(stats.getActiveAllocations()); // Simplified
        
        logger.debug("Collected statistics: {} active sessions, {} active allocations", 
                     stats.getActiveSessions(), stats.getActiveAllocations());
        
        return stats;
    }
    
    /**
     * Get user statistics only
     */
    public StatisticsResponse getUserStatistics() {
        StatisticsResponse stats = new StatisticsResponse();
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        
        stats.setTotalUsers(userRepository.count());
        stats.setActiveUsers(userRepository.countActiveUsers());
        stats.setUsersCreatedToday(userRepository.findByCreatedAtAfter(startOfDay).size());
        
        return stats;
    }
    
    /**
     * Get session statistics only
     */
    public StatisticsResponse getSessionStatistics() {
        StatisticsResponse stats = new StatisticsResponse();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        
        stats.setActiveSessions(sessionRepository.countActiveSessions(now));
        stats.setTotalSessions(sessionRepository.count());
        stats.setSessionsCreatedToday(sessionRepository.findByLastActivityAfter(startOfDay).size());
        stats.setTotalBytesSent(calculateTotalBytesSent());
        stats.setTotalBytesReceived(calculateTotalBytesReceived());
        stats.setTotalPacketsSent(calculateTotalPacketsSent());
        stats.setTotalPacketsReceived(calculateTotalPacketsReceived());
        
        return stats;
    }
    
    /**
     * Get allocation statistics only
     */
    public StatisticsResponse getAllocationStatistics() {
        StatisticsResponse stats = new StatisticsResponse();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        
        stats.setActiveAllocations(allocationRepository.countActiveAllocations(now));
        stats.setTotalAllocations(allocationRepository.count());
        stats.setAllocationsCreatedToday(allocationRepository.findActiveAllocations(startOfDay).size());
        
        Long totalBytesRelayed = allocationRepository.getTotalBytesRelayed();
        stats.setTotalBytesRelayed(totalBytesRelayed != null ? totalBytesRelayed : 0L);
        
        Long totalPacketsRelayed = allocationRepository.getTotalPacketsRelayed();
        stats.setTotalPacketsRelayed(totalPacketsRelayed != null ? totalPacketsRelayed : 0L);
        
        return stats;
    }
    
    /**
     * Get server uptime in seconds
     */
    public long getUptimeSeconds() {
        return Duration.between(serverStartTime, LocalDateTime.now()).getSeconds();
    }
    
    /**
     * Get server start time
     */
    public LocalDateTime getServerStartTime() {
        return serverStartTime;
    }
    
    private long calculateTotalBytesSent() {
        // This would be implemented to sum bytes sent across all sessions
        // For now, returning 0 as placeholder
        return 0L;
    }
    
    private long calculateTotalBytesReceived() {
        // This would be implemented to sum bytes received across all sessions
        // For now, returning 0 as placeholder
        return 0L;
    }
    
    private long calculateTotalPacketsSent() {
        // This would be implemented to sum packets sent across all sessions
        // For now, returning 0 as placeholder
        return 0L;
    }
    
    private long calculateTotalPacketsReceived() {
        // This would be implemented to sum packets received across all sessions
        // For now, returning 0 as placeholder
        return 0L;
    }
    
    private int calculateAvailablePorts(LocalDateTime now) {
        try {
            java.util.List<Integer> usedPorts = allocationRepository.findUsedRelayPorts(now);
            int totalPorts = maxPort - minPort + 1;
            return totalPorts - usedPorts.size();
        } catch (Exception e) {
            logger.warn("Error calculating available ports", e);
            return maxPort - minPort + 1;
        }
    }
    
    private double calculateAverageSessionDuration() {
        // This would be implemented to calculate actual average session duration
        // For now, returning 0 as placeholder
        return 0.0;
    }
    
    private double calculateAverageAllocationLifetime() {
        // This would be implemented to calculate actual average allocation lifetime
        // For now, returning 0 as placeholder
        return 0.0;
    }
}