package com.example.turnserver.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for TURN server statistics
 */
public class StatisticsResponse {
    
    // User statistics
    private long totalUsers;
    private long activeUsers;
    private long usersCreatedToday;
    
    // Session statistics
    private long activeSessions;
    private long totalSessions;
    private long sessionsCreatedToday;
    private long totalBytesSent;
    private long totalBytesReceived;
    private long totalPacketsSent;
    private long totalPacketsReceived;
    
    // Allocation statistics
    private long activeAllocations;
    private long totalAllocations;
    private long allocationsCreatedToday;
    private long totalBytesRelayed;
    private long totalPacketsRelayed;
    
    // Server statistics
    private LocalDateTime serverStartTime;
    private long uptimeSeconds;
    private String serverVersion;
    private int configuredPortRange;
    private int availablePorts;
    
    // Performance metrics
    private double avgSessionDuration;
    private double avgAllocationLifetime;
    private long peakConcurrentSessions;
    private long peakConcurrentAllocations;
    
    // Constructors
    public StatisticsResponse() {}
    
    // Getters and setters
    public long getTotalUsers() {
        return totalUsers;
    }
    
    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }
    
    public long getActiveUsers() {
        return activeUsers;
    }
    
    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }
    
    public long getUsersCreatedToday() {
        return usersCreatedToday;
    }
    
    public void setUsersCreatedToday(long usersCreatedToday) {
        this.usersCreatedToday = usersCreatedToday;
    }
    
    public long getActiveSessions() {
        return activeSessions;
    }
    
    public void setActiveSessions(long activeSessions) {
        this.activeSessions = activeSessions;
    }
    
    public long getTotalSessions() {
        return totalSessions;
    }
    
    public void setTotalSessions(long totalSessions) {
        this.totalSessions = totalSessions;
    }
    
    public long getSessionsCreatedToday() {
        return sessionsCreatedToday;
    }
    
    public void setSessionsCreatedToday(long sessionsCreatedToday) {
        this.sessionsCreatedToday = sessionsCreatedToday;
    }
    
    public long getTotalBytesSent() {
        return totalBytesSent;
    }
    
    public void setTotalBytesSent(long totalBytesSent) {
        this.totalBytesSent = totalBytesSent;
    }
    
    public long getTotalBytesReceived() {
        return totalBytesReceived;
    }
    
    public void setTotalBytesReceived(long totalBytesReceived) {
        this.totalBytesReceived = totalBytesReceived;
    }
    
    public long getTotalPacketsSent() {
        return totalPacketsSent;
    }
    
    public void setTotalPacketsSent(long totalPacketsSent) {
        this.totalPacketsSent = totalPacketsSent;
    }
    
    public long getTotalPacketsReceived() {
        return totalPacketsReceived;
    }
    
    public void setTotalPacketsReceived(long totalPacketsReceived) {
        this.totalPacketsReceived = totalPacketsReceived;
    }
    
    public long getActiveAllocations() {
        return activeAllocations;
    }
    
    public void setActiveAllocations(long activeAllocations) {
        this.activeAllocations = activeAllocations;
    }
    
    public long getTotalAllocations() {
        return totalAllocations;
    }
    
    public void setTotalAllocations(long totalAllocations) {
        this.totalAllocations = totalAllocations;
    }
    
    public long getAllocationsCreatedToday() {
        return allocationsCreatedToday;
    }
    
    public void setAllocationsCreatedToday(long allocationsCreatedToday) {
        this.allocationsCreatedToday = allocationsCreatedToday;
    }
    
    public long getTotalBytesRelayed() {
        return totalBytesRelayed;
    }
    
    public void setTotalBytesRelayed(long totalBytesRelayed) {
        this.totalBytesRelayed = totalBytesRelayed;
    }
    
    public long getTotalPacketsRelayed() {
        return totalPacketsRelayed;
    }
    
    public void setTotalPacketsRelayed(long totalPacketsRelayed) {
        this.totalPacketsRelayed = totalPacketsRelayed;
    }
    
    public LocalDateTime getServerStartTime() {
        return serverStartTime;
    }
    
    public void setServerStartTime(LocalDateTime serverStartTime) {
        this.serverStartTime = serverStartTime;
    }
    
    public long getUptimeSeconds() {
        return uptimeSeconds;
    }
    
    public void setUptimeSeconds(long uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
    }
    
    public String getServerVersion() {
        return serverVersion;
    }
    
    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }
    
    public int getConfiguredPortRange() {
        return configuredPortRange;
    }
    
    public void setConfiguredPortRange(int configuredPortRange) {
        this.configuredPortRange = configuredPortRange;
    }
    
    public int getAvailablePorts() {
        return availablePorts;
    }
    
    public void setAvailablePorts(int availablePorts) {
        this.availablePorts = availablePorts;
    }
    
    public double getAvgSessionDuration() {
        return avgSessionDuration;
    }
    
    public void setAvgSessionDuration(double avgSessionDuration) {
        this.avgSessionDuration = avgSessionDuration;
    }
    
    public double getAvgAllocationLifetime() {
        return avgAllocationLifetime;
    }
    
    public void setAvgAllocationLifetime(double avgAllocationLifetime) {
        this.avgAllocationLifetime = avgAllocationLifetime;
    }
    
    public long getPeakConcurrentSessions() {
        return peakConcurrentSessions;
    }
    
    public void setPeakConcurrentSessions(long peakConcurrentSessions) {
        this.peakConcurrentSessions = peakConcurrentSessions;
    }
    
    public long getPeakConcurrentAllocations() {
        return peakConcurrentAllocations;
    }
    
    public void setPeakConcurrentAllocations(long peakConcurrentAllocations) {
        this.peakConcurrentAllocations = peakConcurrentAllocations;
    }
    
    @Override
    public String toString() {
        return "StatisticsResponse{" +
                "totalUsers=" + totalUsers +
                ", activeUsers=" + activeUsers +
                ", activeSessions=" + activeSessions +
                ", totalSessions=" + totalSessions +
                ", activeAllocations=" + activeAllocations +
                ", totalAllocations=" + totalAllocations +
                ", uptimeSeconds=" + uptimeSeconds +
                '}';
    }
}