package com.example.turnserver.handler;

import com.example.turnserver.protocol.StunMessage;
import com.example.turnserver.service.TurnServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for STUN-specific messages
 */
public class StunMessageHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(StunMessageHandler.class);
    
    private final TurnServerService turnServerService;
    
    public StunMessageHandler(TurnServerService turnServerService) {
        this.turnServerService = turnServerService;
    }
    
    /**
     * Handle STUN Binding request
     */
    public StunMessage handleBindingRequest(StunMessage request, String clientAddress, int clientPort) {
        logger.debug("Handling STUN Binding request from {}:{}", clientAddress, clientPort);
        
        return turnServerService.processBindingRequest(request, clientAddress, clientPort);
    }
}