package com.example.turnserver.handler;

import com.example.turnserver.protocol.MessageType;
import com.example.turnserver.protocol.StunMessage;
import com.example.turnserver.protocol.StunUtils;
import com.example.turnserver.service.TurnServerService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * Main Netty handler for TURN/STUN server traffic
 */
@Component
public class TurnServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    
    private static final Logger logger = LoggerFactory.getLogger(TurnServerHandler.class);
    
    private final TurnServerService turnServerService;
    private final StunMessageHandler stunMessageHandler;
    private final TurnMessageHandler turnMessageHandler;
    
    @Autowired
    public TurnServerHandler(TurnServerService turnServerService) {
        this.turnServerService = turnServerService;
        this.stunMessageHandler = new StunMessageHandler(turnServerService);
        this.turnMessageHandler = new TurnMessageHandler(turnServerService);
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        InetSocketAddress sender = packet.sender();
        ByteBuf content = packet.content();
        
        logger.debug("Received packet from {}:{}, size: {} bytes", 
                     sender.getHostString(), sender.getPort(), content.readableBytes());
        
        try {
            // Parse STUN/TURN message
            StunMessage request = StunUtils.parseMessage(content.copy());
            
            logger.debug("Parsed message: type={}, transaction={}", 
                         request.getMessageType(), 
                         java.util.Arrays.toString(request.getTransactionId()));
            
            // Process message and generate response
            StunMessage response = processMessage(request, sender);
            
            if (response != null) {
                // Encode and send response
                ByteBuf responseBuffer = StunUtils.encodeMessage(response);
                DatagramPacket responsePacket = new DatagramPacket(responseBuffer, sender);
                ctx.writeAndFlush(responsePacket);
                
                logger.debug("Sent response to {}:{}, type={}", 
                             sender.getHostString(), sender.getPort(), response.getMessageType());
            }
            
        } catch (Exception e) {
            logger.error("Error processing packet from " + sender, e);
            // Send error response if possible
            sendErrorResponse(ctx, sender, e);
        }
    }
    
    /**
     * Process incoming STUN/TURN message
     */
    private StunMessage processMessage(StunMessage request, InetSocketAddress sender) {
        MessageType messageType = request.getMessageType();
        String clientAddress = sender.getHostString();
        int clientPort = sender.getPort();
        
        try {
            switch (messageType) {
                case BINDING_REQUEST:
                    return stunMessageHandler.handleBindingRequest(request, clientAddress, clientPort);
                    
                case ALLOCATE_REQUEST:
                    return turnMessageHandler.handleAllocateRequest(request, clientAddress, clientPort);
                    
                case REFRESH_REQUEST:
                    return turnMessageHandler.handleRefreshRequest(request, clientAddress, clientPort);
                    
                case CREATE_PERMISSION_REQUEST:
                    return turnMessageHandler.handleCreatePermissionRequest(request, clientAddress, clientPort);
                    
                case CHANNEL_BIND_REQUEST:
                    return turnMessageHandler.handleChannelBindRequest(request, clientAddress, clientPort);
                    
                case SEND_INDICATION:
                case DATA_INDICATION:
                    // Handle data relay (no response needed for indications)
                    turnMessageHandler.handleDataRelay(request, clientAddress, clientPort);
                    return null;
                    
                default:
                    logger.warn("Unsupported message type: {}", messageType);
                    return createErrorResponse(request, 400, "Bad Request");
            }
            
        } catch (Exception e) {
            logger.error("Error processing {} from {}:{}", messageType, clientAddress, clientPort, e);
            return createErrorResponse(request, 500, "Server Error");
        }
    }
    
    /**
     * Create error response message
     */
    private StunMessage createErrorResponse(StunMessage request, int errorCode, String reason) {
        MessageType errorResponseType = getErrorResponseType(request.getMessageType());
        StunMessage errorResponse = new StunMessage(errorResponseType, request.getTransactionId());
        
        // Add ERROR-CODE attribute
        errorResponse.addAttribute(StunUtils.createErrorCodeAttribute(errorCode, reason));
        
        // Add SOFTWARE attribute
        errorResponse.addAttribute(StunUtils.createSoftwareAttribute("Netty TURN Server 1.0"));
        
        return errorResponse;
    }
    
    /**
     * Send error response to client
     */
    private void sendErrorResponse(ChannelHandlerContext ctx, InetSocketAddress sender, Exception e) {
        try {
            // Create a generic error response
            byte[] transactionId = StunUtils.generateTransactionId();
            StunMessage errorResponse = new StunMessage(MessageType.BINDING_ERROR_RESPONSE, transactionId);
            
            errorResponse.addAttribute(StunUtils.createErrorCodeAttribute(500, "Server Error"));
            errorResponse.addAttribute(StunUtils.createSoftwareAttribute("Netty TURN Server 1.0"));
            
            ByteBuf responseBuffer = StunUtils.encodeMessage(errorResponse);
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, sender);
            ctx.writeAndFlush(responsePacket);
            
        } catch (Exception ex) {
            logger.error("Failed to send error response", ex);
        }
    }
    
    /**
     * Get error response type for a given request type
     */
    private MessageType getErrorResponseType(MessageType requestType) {
        switch (requestType) {
            case BINDING_REQUEST:
                return MessageType.BINDING_ERROR_RESPONSE;
            case ALLOCATE_REQUEST:
                return MessageType.ALLOCATE_ERROR_RESPONSE;
            case REFRESH_REQUEST:
                return MessageType.REFRESH_ERROR_RESPONSE;
            case CREATE_PERMISSION_REQUEST:
                return MessageType.CREATE_PERMISSION_ERROR_RESPONSE;
            case CHANNEL_BIND_REQUEST:
                return MessageType.CHANNEL_BIND_ERROR_RESPONSE;
            default:
                return MessageType.BINDING_ERROR_RESPONSE;
        }
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("TURN server channel active: {}", ctx.channel().localAddress());
        super.channelActive(ctx);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("TURN server channel inactive: {}", ctx.channel().localAddress());
        super.channelInactive(ctx);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception in TURN server handler", cause);
        // Don't close the channel for UDP
    }
}