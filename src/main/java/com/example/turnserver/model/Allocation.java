package com.example.turnserver.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * TURN allocation entity representing an allocated relay address
 */
@Entity
@Table(name = "allocations", indexes = {
    @Index(name = "idx_allocation_id", columnList = "allocation_id", unique = true),
    @Index(name = "idx_session_id", columnList = "session_id"),
    @Index(name = "idx_relay_address", columnList = "relay_address")
})
public class Allocation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "allocation_id", unique = true, nullable = false)
    private String allocationId;
    
    @Column(name = "session_id", nullable = false)
    private String sessionId;
    
    @Column(nullable = false)
    private String username;
    
    @Column(name = "relay_address", nullable = false)
    private String relayAddress;
    
    @Column(name = "relay_port", nullable = false)
    private Integer relayPort;
    
    @Column(name = "client_address", nullable = false)
    private String clientAddress;
    
    @Column(name = "client_port", nullable = false)
    private Integer clientPort;
    
    @Column(name = "transport_protocol", nullable = false)
    private String transportProtocol; // UDP, TCP
    
    @Column(name = "lifetime_seconds", nullable = false)
    private Integer lifetimeSeconds;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "last_refresh")
    private LocalDateTime lastRefresh;
    
    @Column(name = "bytes_relayed", nullable = false)
    private Long bytesRelayed = 0L;
    
    @Column(name = "packets_relayed", nullable = false)
    private Long packetsRelayed = 0L;
    
    @Column(name = "permissions_count", nullable = false)
    private Integer permissionsCount = 0;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastRefresh = LocalDateTime.now();
    }
    
    // Constructors
    public Allocation() {}
    
    public Allocation(String allocationId, String sessionId, String username,
                     String relayAddress, Integer relayPort,
                     String clientAddress, Integer clientPort,
                     String transportProtocol, Integer lifetimeSeconds) {
        this.allocationId = allocationId;
        this.sessionId = sessionId;
        this.username = username;
        this.relayAddress = relayAddress;
        this.relayPort = relayPort;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.transportProtocol = transportProtocol;
        this.lifetimeSeconds = lifetimeSeconds;
        this.expiresAt = LocalDateTime.now().plusSeconds(lifetimeSeconds);
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getAllocationId() {
        return allocationId;
    }
    
    public void setAllocationId(String allocationId) {
        this.allocationId = allocationId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getRelayAddress() {
        return relayAddress;
    }
    
    public void setRelayAddress(String relayAddress) {
        this.relayAddress = relayAddress;
    }
    
    public Integer getRelayPort() {
        return relayPort;
    }
    
    public void setRelayPort(Integer relayPort) {
        this.relayPort = relayPort;
    }
    
    public String getClientAddress() {
        return clientAddress;
    }
    
    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }
    
    public Integer getClientPort() {
        return clientPort;
    }
    
    public void setClientPort(Integer clientPort) {
        this.clientPort = clientPort;
    }
    
    public String getTransportProtocol() {
        return transportProtocol;
    }
    
    public void setTransportProtocol(String transportProtocol) {
        this.transportProtocol = transportProtocol;
    }
    
    public Integer getLifetimeSeconds() {
        return lifetimeSeconds;
    }
    
    public void setLifetimeSeconds(Integer lifetimeSeconds) {
        this.lifetimeSeconds = lifetimeSeconds;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public LocalDateTime getLastRefresh() {
        return lastRefresh;
    }
    
    public void setLastRefresh(LocalDateTime lastRefresh) {
        this.lastRefresh = lastRefresh;
    }
    
    public Long getBytesRelayed() {
        return bytesRelayed;
    }
    
    public void setBytesRelayed(Long bytesRelayed) {
        this.bytesRelayed = bytesRelayed;
    }
    
    public Long getPacketsRelayed() {
        return packetsRelayed;
    }
    
    public void setPacketsRelayed(Long packetsRelayed) {
        this.packetsRelayed = packetsRelayed;
    }
    
    public Integer getPermissionsCount() {
        return permissionsCount;
    }
    
    public void setPermissionsCount(Integer permissionsCount) {
        this.permissionsCount = permissionsCount;
    }
    
    public void refresh(Integer newLifetimeSeconds) {
        this.lifetimeSeconds = newLifetimeSeconds;
        this.lastRefresh = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusSeconds(newLifetimeSeconds);
    }
    
    public void addRelayedData(long bytes, long packets) {
        this.bytesRelayed += bytes;
        this.packetsRelayed += packets;
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public long getSecondsUntilExpiry() {
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
    }
    
    @Override
    public String toString() {
        return "Allocation{" +
                "id=" + id +
                ", allocationId='" + allocationId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", username='" + username + '\'' +
                ", relayAddress='" + relayAddress + '\'' +
                ", relayPort=" + relayPort +
                ", transportProtocol='" + transportProtocol + '\'' +
                ", expiresAt=" + expiresAt +
                '}';
    }
}