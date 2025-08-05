package com.example.turnserver.repository;

import com.example.turnserver.model.TurnSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TurnSession entity
 */
@Repository
public interface TurnSessionRepository extends JpaRepository<TurnSession, Long> {
    
    /**
     * Find session by session ID
     */
    Optional<TurnSession> findBySessionId(String sessionId);
    
    /**
     * Find all sessions for a user
     */
    List<TurnSession> findByUsername(String username);
    
    /**
     * Find sessions by client address
     */
    List<TurnSession> findByClientAddress(String clientAddress);
    
    /**
     * Find active sessions (not expired)
     */
    @Query("SELECT s FROM TurnSession s WHERE s.expiresAt > :now")
    List<TurnSession> findActiveSessions(LocalDateTime now);
    
    /**
     * Find expired sessions
     */
    @Query("SELECT s FROM TurnSession s WHERE s.expiresAt <= :now")
    List<TurnSession> findExpiredSessions(LocalDateTime now);
    
    /**
     * Find sessions with recent activity
     */
    List<TurnSession> findByLastActivityAfter(LocalDateTime dateTime);
    
    /**
     * Count active sessions
     */
    @Query("SELECT COUNT(s) FROM TurnSession s WHERE s.expiresAt > :now")
    long countActiveSessions(LocalDateTime now);
    
    /**
     * Count sessions by username
     */
    long countByUsername(String username);
    
    /**
     * Delete expired sessions
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM TurnSession s WHERE s.expiresAt <= :now")
    int deleteExpiredSessions(LocalDateTime now);
    
    /**
     * Update last activity for a session
     */
    @Modifying
    @Transactional
    @Query("UPDATE TurnSession s SET s.lastActivity = :now WHERE s.sessionId = :sessionId")
    int updateLastActivity(String sessionId, LocalDateTime now);
    
    /**
     * Get session statistics
     */
    @Query("SELECT " +
           "COUNT(s) as totalSessions, " +
           "SUM(s.bytesSent) as totalBytesSent, " +
           "SUM(s.bytesReceived) as totalBytesReceived, " +
           "SUM(s.packetsSent) as totalPacketsSent, " +
           "SUM(s.packetsReceived) as totalPacketsReceived " +
           "FROM TurnSession s")
    Object getSessionStatistics();
}