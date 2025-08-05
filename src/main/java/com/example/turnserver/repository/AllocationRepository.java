package com.example.turnserver.repository;

import com.example.turnserver.model.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Allocation entity
 */
@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    
    /**
     * Find allocation by allocation ID
     */
    Optional<Allocation> findByAllocationId(String allocationId);
    
    /**
     * Find allocations by session ID
     */
    List<Allocation> findBySessionId(String sessionId);
    
    /**
     * Find allocations by username
     */
    List<Allocation> findByUsername(String username);
    
    /**
     * Find allocation by relay address and port
     */
    Optional<Allocation> findByRelayAddressAndRelayPort(String relayAddress, Integer relayPort);
    
    /**
     * Find active allocations (not expired)
     */
    @Query("SELECT a FROM Allocation a WHERE a.expiresAt > :now")
    List<Allocation> findActiveAllocations(LocalDateTime now);
    
    /**
     * Find expired allocations
     */
    @Query("SELECT a FROM Allocation a WHERE a.expiresAt <= :now")
    List<Allocation> findExpiredAllocations(LocalDateTime now);
    
    /**
     * Find allocations by transport protocol
     */
    List<Allocation> findByTransportProtocol(String transportProtocol);
    
    /**
     * Count active allocations
     */
    @Query("SELECT COUNT(a) FROM Allocation a WHERE a.expiresAt > :now")
    long countActiveAllocations(LocalDateTime now);
    
    /**
     * Count allocations by username
     */
    long countByUsername(String username);
    
    /**
     * Find allocations expiring soon
     */
    @Query("SELECT a FROM Allocation a WHERE a.expiresAt BETWEEN :now AND :threshold")
    List<Allocation> findAllocationsExpiringSoon(LocalDateTime now, LocalDateTime threshold);
    
    /**
     * Delete expired allocations
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Allocation a WHERE a.expiresAt <= :now")
    int deleteExpiredAllocations(LocalDateTime now);
    
    /**
     * Update allocation expiry time
     */
    @Modifying
    @Transactional
    @Query("UPDATE Allocation a SET a.expiresAt = :newExpiryTime, a.lastRefresh = :now, a.lifetimeSeconds = :lifetimeSeconds WHERE a.allocationId = :allocationId")
    int refreshAllocation(String allocationId, LocalDateTime newExpiryTime, LocalDateTime now, Integer lifetimeSeconds);
    
    /**
     * Get total bytes relayed across all allocations
     */
    @Query("SELECT SUM(a.bytesRelayed) FROM Allocation a")
    Long getTotalBytesRelayed();
    
    /**
     * Get total packets relayed across all allocations
     */
    @Query("SELECT SUM(a.packetsRelayed) FROM Allocation a")
    Long getTotalPacketsRelayed();
    
    /**
     * Get allocation statistics
     */
    @Query("SELECT " +
           "COUNT(a) as totalAllocations, " +
           "SUM(a.bytesRelayed) as totalBytesRelayed, " +
           "SUM(a.packetsRelayed) as totalPacketsRelayed, " +
           "AVG(a.lifetimeSeconds) as averageLifetime " +
           "FROM Allocation a")
    Object getAllocationStatistics();
    
    /**
     * Find allocations using specific relay ports
     */
    @Query("SELECT a.relayPort FROM Allocation a WHERE a.expiresAt > :now")
    List<Integer> findUsedRelayPorts(LocalDateTime now);
}