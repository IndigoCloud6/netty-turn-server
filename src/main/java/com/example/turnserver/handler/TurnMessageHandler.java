package com.example.turnserver.handler;

import com.example.turnserver.protocol.StunMessage;
import com.example.turnserver.protocol.StunUtils;
import com.example.turnserver.service.TurnServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for TURN-specific messages
 */
public class TurnMessageHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(TurnMessageHandler.class);
    
    private final TurnServerService turnServerService;
    
    public TurnMessageHandler(TurnServerService turnServerService) {
        this.turnServerService = turnServerService;
    }
    
    /**
     * Handle TURN Allocate request
     */
    public StunMessage handleAllocateRequest(StunMessage request, String clientAddress, int clientPort) {
        logger.debug("Handling TURN Allocate request from {}:{}", clientAddress, clientPort);
        
        return turnServerService.processAllocateRequest(request, clientAddress, clientPort);
    }
    
    /**
     * Handle TURN Refresh request
     */
    public StunMessage handleRefreshRequest(StunMessage request, String clientAddress, int clientPort) {
        logger.debug("Handling TURN Refresh request from {}:{}", clientAddress, clientPort);
        
        return turnServerService.processRefreshRequest(request, clientAddress, clientPort);
    }
    
    /**
     * Handle TURN CreatePermission request (not fully implemented)
     */
    public StunMessage handleCreatePermissionRequest(StunMessage request, String clientAddress, int clientPort) {
        logger.debug("Handling TURN CreatePermission request from {}:{}", clientAddress, clientPort);
        
        // For now, return a simple success response
        // In a full implementation, this would handle peer permissions
        StunMessage response = new StunMessage(
            com.example.turnserver.protocol.MessageType.CREATE_PERMISSION_RESPONSE, 
            request.getTransactionId()
        );
        
        response.addAttribute(StunUtils.createSoftwareAttribute("Netty TURN Server 1.0"));
        
        logger.info("CreatePermission request processed (placeholder implementation)");
        return response;
    }
    
    /**
     * Handle TURN ChannelBind request (not fully implemented)
     */
    public StunMessage handleChannelBindRequest(StunMessage request, String clientAddress, int clientPort) {
        logger.debug("Handling TURN ChannelBind request from {}:{}", clientAddress, clientPort);
        
        // For now, return a simple success response
        // In a full implementation, this would handle channel binding
        StunMessage response = new StunMessage(
            com.example.turnserver.protocol.MessageType.CHANNEL_BIND_RESPONSE, 
            request.getTransactionId()
        );
        
        response.addAttribute(StunUtils.createSoftwareAttribute("Netty TURN Server 1.0"));
        
        logger.info("ChannelBind request processed (placeholder implementation)");
        return response;
    }
    
    /**
     * Handle data relay (Send/Data indications)
     */
    public void handleDataRelay(StunMessage request, String clientAddress, int clientPort) {
        logger.debug("Handling data relay from {}:{}", clientAddress, clientPort);
        
        // This would handle actual data relaying in a full implementation
        // For now, just log the request
        logger.info("Data relay request received from {}:{} (placeholder implementation)", 
                   clientAddress, clientPort);
    }
}