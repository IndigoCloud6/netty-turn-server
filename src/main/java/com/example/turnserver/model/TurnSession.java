package com.example.turnserver.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * TURN session entity representing an active TURN client session
 */
@Entity
@Table(name = "turn_sessions", indexes = {
    @Index(name = "idx_session_id", columnList = "sessionId", unique = true),
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_client_address", columnList = "clientAddress")
})
public class TurnSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;
    
    @Column(nullable = false)
    private String username;
    
    @Column(name = "client_address", nullable = false)
    private String clientAddress;
    
    @Column(name = "client_port", nullable = false)
    private Integer clientPort;
    
    @Column(name = "realm")
    private String realm;
    
    @Column(name = "nonce")
    private String nonce;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_activity", nullable = false)
    private LocalDateTime lastActivity;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "bytes_sent", nullable = false)
    private Long bytesSent = 0L;
    
    @Column(name = "bytes_received", nullable = false)
    private Long bytesReceived = 0L;
    
    @Column(name = "packets_sent", nullable = false)
    private Long packetsSent = 0L;
    
    @Column(name = "packets_received", nullable = false)
    private Long packetsReceived = 0L;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActivity = LocalDateTime.now();
    }
    
    // Constructors
    public TurnSession() {}
    
    public TurnSession(String sessionId, String username, String clientAddress, Integer clientPort) {
        this.sessionId = sessionId;
        this.username = username;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getRealm() {
        return realm;
    }
    
    public void setRealm(String realm) {
        this.realm = realm;
    }
    
    public String getNonce() {
        return nonce;
    }
    
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastActivity() {
        return lastActivity;
    }
    
    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Long getBytesSent() {
        return bytesSent;
    }
    
    public void setBytesSent(Long bytesSent) {
        this.bytesSent = bytesSent;
    }
    
    public Long getBytesReceived() {
        return bytesReceived;
    }
    
    public void setBytesReceived(Long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }
    
    public Long getPacketsSent() {
        return packetsSent;
    }
    
    public void setPacketsSent(Long packetsSent) {
        this.packetsSent = packetsSent;
    }
    
    public Long getPacketsReceived() {
        return packetsReceived;
    }
    
    public void setPacketsReceived(Long packetsReceived) {
        this.packetsReceived = packetsReceived;
    }
    
    public void updateActivity() {
        this.lastActivity = LocalDateTime.now();
    }
    
    public void addBytesTransferred(long sent, long received) {
        this.bytesSent += sent;
        this.bytesReceived += received;
    }
    
    public void addPacketsTransferred(long sent, long received) {
        this.packetsSent += sent;
        this.packetsReceived += received;
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    @Override
    public String toString() {
        return "TurnSession{" +
                "id=" + id +
                ", sessionId='" + sessionId + '\'' +
                ", username='" + username + '\'' +
                ", clientAddress='" + clientAddress + '\'' +
                ", clientPort=" + clientPort +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                '}';
    }
}