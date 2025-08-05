package com.example.turnserver.service;

import com.example.turnserver.exception.TurnException;
import com.example.turnserver.model.Allocation;
import com.example.turnserver.model.TurnSession;
import com.example.turnserver.protocol.*;
import com.example.turnserver.repository.AllocationRepository;
import com.example.turnserver.repository.TurnSessionRepository;
import com.example.turnserver.util.CryptoUtils;
import com.example.turnserver.util.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core TURN server service handling TURN protocol operations
 */
@Service
@Transactional
public class TurnServerService {
    
    private static final Logger logger = LoggerFactory.getLogger(TurnServerService.class);
    
    private static final int DEFAULT_ALLOCATION_LIFETIME = 600; // 10 minutes
    private static final int MAX_ALLOCATION_LIFETIME = 3600; // 1 hour
    private static final int UDP_PROTOCOL = 17;
    
    private final TurnSessionRepository sessionRepository;
    private final AllocationRepository allocationRepository;
    private final AuthService authService;
    
    private final String externalIp;
    private final int minPort;
    private final int maxPort;
    private final String realm;
    
    // Active sessions and allocations cache
    private final Set<String> activeSessions = ConcurrentHashMap.newKeySet();
    private final Set<String> activeAllocations = ConcurrentHashMap.newKeySet();
    
    @Autowired
    public TurnServerService(TurnSessionRepository sessionRepository,
                           AllocationRepository allocationRepository,
                           AuthService authService,
                           @Value("${turn.server.external-ip:127.0.0.1}") String externalIp,
                           @Value("${turn.server.min-port:49152}") int minPort,
                           @Value("${turn.server.max-port:65535}") int maxPort,
                           @Value("${turn.server.realm:turn.example.com}") String realm) {
        this.sessionRepository = sessionRepository;
        this.allocationRepository = allocationRepository;
        this.authService = authService;
        this.externalIp = externalIp;
        this.minPort = minPort;
        this.maxPort = maxPort;
        this.realm = realm;
    }
    
    /**
     * Process STUN Binding request
     */
    public StunMessage processBindingRequest(StunMessage request, String clientAddress, int clientPort) {
        logger.debug("Processing STUN Binding request from {}:{}", clientAddress, clientPort);
        
        StunMessage response = new StunMessage(MessageType.BINDING_RESPONSE, request.getTransactionId());
        
        // Add XOR-MAPPED-ADDRESS attribute
        java.net.InetSocketAddress clientSocketAddress = new java.net.InetSocketAddress(clientAddress, clientPort);
        StunAttribute xorMappedAddr = StunUtils.createXorMappedAddressAttribute(clientSocketAddress, request.getTransactionId());
        response.addAttribute(xorMappedAddr);
        
        // Add SOFTWARE attribute
        response.addAttribute(StunUtils.createSoftwareAttribute("Netty TURN Server 1.0"));
        
        logger.debug("Created STUN Binding response");
        return response;
    }
    
    /**
     * Process TURN Allocate request
     */
    public StunMessage processAllocateRequest(StunMessage request, String clientAddress, int clientPort) {
        logger.debug("Processing TURN Allocate request from {}:{}", clientAddress, clientPort);
        
        // Authenticate the request
        if (!authService.authenticateMessage(request, clientAddress)) {
            logger.warn("Authentication failed for allocate request from {}", clientAddress);
            return authService.createAuthenticationChallenge(request);
        }
        
        // Extract username
        StunAttribute usernameAttr = request.getAttribute(AttributeType.USERNAME);
        if (usernameAttr == null) {
            throw TurnException.badRequest("Missing USERNAME attribute");
        }
        String username = usernameAttr.getValueAsString();
        
        // Check REQUESTED-TRANSPORT attribute
        StunAttribute transportAttr = request.getAttribute(AttributeType.REQUESTED_TRANSPORT);
        if (transportAttr == null) {
            throw TurnException.badRequest("Missing REQUESTED-TRANSPORT attribute");
        }
        
        byte protocol = transportAttr.getValueAsByte();
        if (protocol != UDP_PROTOCOL) {
            throw TurnException.unsupportedTransportProtocol("Only UDP transport is supported");
        }
        
        // Get or create session
        TurnSession session = getOrCreateSession(username, clientAddress, clientPort);
        
        // Check for existing allocation
        List<Allocation> existingAllocations = allocationRepository.findBySessionId(session.getSessionId());
        if (!existingAllocations.isEmpty()) {
            throw TurnException.allocationMismatch("Allocation already exists for this session");
        }
        
        // Allocate relay address and port
        int relayPort = allocatePort();
        if (relayPort == -1) {
            throw TurnException.insufficientCapacity("No available ports for allocation");
        }
        
        // Get lifetime
        int lifetime = DEFAULT_ALLOCATION_LIFETIME;
        StunAttribute lifetimeAttr = request.getAttribute(AttributeType.LIFETIME);
        if (lifetimeAttr != null) {
            int requestedLifetime = lifetimeAttr.getValueAsInt();
            lifetime = Math.min(requestedLifetime, MAX_ALLOCATION_LIFETIME);
        }
        
        // Create allocation
        Allocation allocation = new Allocation(
            CryptoUtils.generateAllocationId(),
            session.getSessionId(),
            username,
            externalIp,
            relayPort,
            clientAddress,
            clientPort,
            "UDP",
            lifetime
        );
        
        allocationRepository.save(allocation);
        activeAllocations.add(allocation.getAllocationId());
        
        // Create response
        StunMessage response = new StunMessage(MessageType.ALLOCATE_RESPONSE, request.getTransactionId());
        
        // Add XOR-RELAYED-ADDRESS attribute
        java.net.InetSocketAddress relayAddress = new java.net.InetSocketAddress(externalIp, relayPort);
        response.addAttribute(StunUtils.createXorRelayedAddressAttribute(relayAddress, request.getTransactionId()));
        
        // Add LIFETIME attribute
        response.addAttribute(StunUtils.createLifetimeAttribute(lifetime));
        
        // Add MESSAGE-INTEGRITY attribute
        response.addAttribute(authService.createMessageIntegrityAttribute(response, username));
        
        logger.info("Created allocation {} for user {} on port {}", allocation.getAllocationId(), username, relayPort);
        return response;
    }
    
    /**
     * Process TURN Refresh request
     */
    public StunMessage processRefreshRequest(StunMessage request, String clientAddress, int clientPort) {
        logger.debug("Processing TURN Refresh request from {}:{}", clientAddress, clientPort);
        
        // Authenticate the request
        if (!authService.authenticateMessage(request, clientAddress)) {
            logger.warn("Authentication failed for refresh request from {}", clientAddress);
            return authService.createAuthenticationChallenge(request);
        }
        
        // Extract username
        StunAttribute usernameAttr = request.getAttribute(AttributeType.USERNAME);
        if (usernameAttr == null) {
            throw TurnException.badRequest("Missing USERNAME attribute");
        }
        String username = usernameAttr.getValueAsString();
        
        // Find session
        Optional<TurnSession> sessionOpt = sessionRepository.findByUsername(username)
            .stream()
            .filter(s -> s.getClientAddress().equals(clientAddress) && s.getClientPort().equals(clientPort))
            .findFirst();
        
        if (!sessionOpt.isPresent()) {
            throw TurnException.allocationMismatch("No session found for this client");
        }
        
        TurnSession session = sessionOpt.get();
        
        // Find allocation
        List<Allocation> allocations = allocationRepository.findBySessionId(session.getSessionId());
        if (allocations.isEmpty()) {
            throw TurnException.allocationMismatch("No allocation found for this session");
        }
        
        Allocation allocation = allocations.get(0);
        
        // Get requested lifetime
        int lifetime = 0; // Default to 0 (delete allocation)
        StunAttribute lifetimeAttr = request.getAttribute(AttributeType.LIFETIME);
        if (lifetimeAttr != null) {
            lifetime = Math.min(lifetimeAttr.getValueAsInt(), MAX_ALLOCATION_LIFETIME);
        }
        
        // Refresh or delete allocation
        if (lifetime == 0) {
            // Delete allocation
            allocationRepository.delete(allocation);
            activeAllocations.remove(allocation.getAllocationId());
            logger.info("Deleted allocation {} for user {}", allocation.getAllocationId(), username);
        } else {
            // Refresh allocation
            allocation.refresh(lifetime);
            allocationRepository.save(allocation);
            logger.info("Refreshed allocation {} for user {} with lifetime {}", 
                       allocation.getAllocationId(), username, lifetime);
        }
        
        // Create response
        StunMessage response = new StunMessage(MessageType.REFRESH_RESPONSE, request.getTransactionId());
        response.addAttribute(StunUtils.createLifetimeAttribute(lifetime));
        response.addAttribute(authService.createMessageIntegrityAttribute(response, username));
        
        return response;
    }
    
    /**
     * Get or create a TURN session
     */
    private TurnSession getOrCreateSession(String username, String clientAddress, int clientPort) {
        // Look for existing session
        Optional<TurnSession> existingSession = sessionRepository.findByUsername(username)
            .stream()
            .filter(s -> s.getClientAddress().equals(clientAddress) && s.getClientPort().equals(clientPort))
            .filter(s -> !s.isExpired())
            .findFirst();
        
        if (existingSession.isPresent()) {
            TurnSession session = existingSession.get();
            session.updateActivity();
            sessionRepository.save(session);
            return session;
        }
        
        // Create new session
        TurnSession session = new TurnSession(
            CryptoUtils.generateSessionId(),
            username,
            clientAddress,
            clientPort
        );
        session.setRealm(realm);
        session.setExpiresAt(LocalDateTime.now().plusMinutes(30)); // 30 minutes session lifetime
        
        sessionRepository.save(session);
        activeSessions.add(session.getSessionId());
        
        logger.info("Created new session {} for user {}", session.getSessionId(), username);
        return session;
    }
    
    /**
     * Allocate an available port
     */
    private int allocatePort() {
        // Get used ports
        List<Integer> usedPorts = allocationRepository.findUsedRelayPorts(LocalDateTime.now());
        
        // Find available port
        for (int port = minPort; port <= maxPort; port++) {
            if (!usedPorts.contains(port)) {
                return port;
            }
        }
        
        logger.warn("No available ports in range {}-{}", minPort, maxPort);
        return -1;
    }
    
    /**
     * Clean up expired sessions and allocations
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    @Async
    public void cleanupExpiredResources() {
        LocalDateTime now = LocalDateTime.now();
        
        // Clean up expired sessions
        int expiredSessions = sessionRepository.deleteExpiredSessions(now);
        if (expiredSessions > 0) {
            logger.info("Cleaned up {} expired sessions", expiredSessions);
        }
        
        // Clean up expired allocations
        int expiredAllocations = allocationRepository.deleteExpiredAllocations(now);
        if (expiredAllocations > 0) {
            logger.info("Cleaned up {} expired allocations", expiredAllocations);
        }
        
        // Update cache
        refreshCaches();
    }
    
    /**
     * Refresh internal caches
     */
    private void refreshCaches() {
        LocalDateTime now = LocalDateTime.now();
        
        // Update active sessions cache
        activeSessions.clear();
        sessionRepository.findActiveSessions(now)
            .forEach(session -> activeSessions.add(session.getSessionId()));
        
        // Update active allocations cache
        activeAllocations.clear();
        allocationRepository.findActiveAllocations(now)
            .forEach(allocation -> activeAllocations.add(allocation.getAllocationId()));
    }
    
    /**
     * Get active session count
     */
    public long getActiveSessionCount() {
        return activeSessions.size();
    }
    
    /**
     * Get active allocation count
     */
    public long getActiveAllocationCount() {
        return activeAllocations.size();
    }
}