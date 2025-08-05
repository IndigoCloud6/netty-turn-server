package com.example.turnserver.service;

import com.example.turnserver.exception.AuthenticationException;
import com.example.turnserver.model.User;
import com.example.turnserver.protocol.AttributeType;
import com.example.turnserver.protocol.StunAttribute;
import com.example.turnserver.protocol.StunMessage;
import com.example.turnserver.util.CryptoUtils;
import com.example.turnserver.util.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for handling TURN server authentication
 */
@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final UserService userService;
    private final String realm;
    private final String secret;
    
    // Cache for nonces to prevent replay attacks
    private final Map<String, Long> nonceCache = new ConcurrentHashMap<>();
    private static final long NONCE_VALIDITY_SECONDS = 300; // 5 minutes
    private static final long NONCE_CLEANUP_INTERVAL = 60000; // 1 minute
    
    @Autowired
    public AuthService(UserService userService,
                       @Value("${turn.server.realm:turn.example.com}") String realm,
                       @Value("${turn.server.secret:myTurnSecret123}") String secret) {
        this.userService = userService;
        this.realm = realm;
        this.secret = secret;
        
        // Start nonce cleanup task
        startNonceCleanupTask();
    }
    
    /**
     * Authenticate a STUN/TURN message
     */
    public boolean authenticateMessage(StunMessage message, String clientAddress) {
        logger.debug("Authenticating message for client: {}", clientAddress);
        
        try {
            // Check for USERNAME attribute
            StunAttribute usernameAttr = message.getAttribute(AttributeType.USERNAME);
            if (usernameAttr == null) {
                logger.debug("No USERNAME attribute found");
                return false;
            }
            
            String username = usernameAttr.getValueAsString();
            
            // Check for MESSAGE-INTEGRITY attribute
            StunAttribute messageIntegrityAttr = message.getAttribute(AttributeType.MESSAGE_INTEGRITY);
            if (messageIntegrityAttr == null) {
                logger.debug("No MESSAGE-INTEGRITY attribute found");
                return false;
            }
            
            // Verify user exists and is active
            Optional<User> userOpt = userService.findUserByUsername(username);
            if (!userOpt.isPresent() || !userOpt.get().getEnabled()) {
                logger.warn("User not found or disabled: {}", username);
                return false;
            }
            
            User user = userOpt.get();
            
            // Generate key for long-term credentials
            byte[] key = HmacUtils.generateLongTermKey(username, realm, extractPasswordFromHash(user.getPassword()));
            
            // Create message without MESSAGE-INTEGRITY for verification
            byte[] messageBytes = createMessageBytesForIntegrityCheck(message);
            
            // Verify MESSAGE-INTEGRITY
            boolean isValid = HmacUtils.verifyStunMessageIntegrity(messageBytes, key, messageIntegrityAttr.getValue());
            
            if (isValid) {
                logger.debug("Authentication successful for user: {}", username);
                userService.updateLastLogin(username);
                return true;
            } else {
                logger.warn("MESSAGE-INTEGRITY verification failed for user: {}", username);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error during authentication", e);
            return false;
        }
    }
    
    /**
     * Generate authentication challenge for unauthorized requests
     */
    public StunMessage createAuthenticationChallenge(StunMessage originalMessage) {
        logger.debug("Creating authentication challenge");
        
        // Generate nonce
        String nonce = HmacUtils.generateNonce();
        storeNonce(nonce);
        
        // Create error response
        StunMessage errorResponse = new StunMessage(
            getErrorResponseType(originalMessage.getMessageType()),
            originalMessage.getTransactionId()
        );
        
        // Add REALM attribute
        errorResponse.addAttribute(new StunAttribute(AttributeType.REALM, realm.getBytes()));
        
        // Add NONCE attribute
        errorResponse.addAttribute(new StunAttribute(AttributeType.NONCE, nonce.getBytes()));
        
        // Add ERROR-CODE attribute (401 Unauthorized)
        errorResponse.addAttribute(createErrorCodeAttribute(401, "Unauthorized"));
        
        return errorResponse;
    }
    
    /**
     * Validate nonce from client request
     */
    public boolean validateNonce(String nonce) {
        if (nonce == null || nonce.isEmpty()) {
            return false;
        }
        
        Long timestamp = nonceCache.get(nonce);
        if (timestamp == null) {
            logger.debug("Nonce not found in cache: {}", nonce);
            return false;
        }
        
        long currentTime = System.currentTimeMillis() / 1000;
        boolean isValid = (currentTime - timestamp) <= NONCE_VALIDITY_SECONDS;
        
        if (!isValid) {
            logger.debug("Nonce expired: {}", nonce);
            nonceCache.remove(nonce);
        }
        
        return isValid;
    }
    
    /**
     * Generate short-term credentials
     */
    public String[] generateShortTermCredentials(String baseUsername) {
        String username = HmacUtils.generateShortTermUsername(baseUsername);
        String password = HmacUtils.generateShortTermPassword();
        return new String[]{username, password};
    }
    
    /**
     * Validate short-term credentials
     */
    public boolean validateShortTermCredentials(String username, String password) {
        // Check if username format is correct (timestamp:username)
        if (!username.contains(":")) {
            return false;
        }
        
        // Check if credentials are not expired
        return !HmacUtils.isShortTermCredentialExpired(username, NONCE_VALIDITY_SECONDS);
    }
    
    /**
     * Check if a user is authorized for TURN operations
     */
    public boolean isUserAuthorized(String username) {
        return userService.isUserActiveByUsername(username);
    }
    
    /**
     * Get realm for authentication
     */
    public String getRealm() {
        return realm;
    }
    
    /**
     * Create MESSAGE-INTEGRITY attribute for response
     */
    public StunAttribute createMessageIntegrityAttribute(StunMessage message, String username) {
        try {
            Optional<User> userOpt = userService.findUserByUsername(username);
            if (!userOpt.isPresent()) {
                throw AuthenticationException.userNotFound();
            }
            
            User user = userOpt.get();
            byte[] key = HmacUtils.generateLongTermKey(username, realm, extractPasswordFromHash(user.getPassword()));
            byte[] messageBytes = createMessageBytesForIntegrityCheck(message);
            byte[] integrity = HmacUtils.createStunMessageIntegrity(messageBytes, key);
            
            return new StunAttribute(AttributeType.MESSAGE_INTEGRITY, integrity);
            
        } catch (Exception e) {
            logger.error("Error creating MESSAGE-INTEGRITY attribute", e);
            throw AuthenticationException.invalidCredentials();
        }
    }
    
    private void storeNonce(String nonce) {
        long timestamp = System.currentTimeMillis() / 1000;
        nonceCache.put(nonce, timestamp);
    }
    
    private com.example.turnserver.protocol.MessageType getErrorResponseType(com.example.turnserver.protocol.MessageType requestType) {
        // Map request types to their corresponding error response types
        switch (requestType) {
            case BINDING_REQUEST:
                return com.example.turnserver.protocol.MessageType.BINDING_ERROR_RESPONSE;
            case ALLOCATE_REQUEST:
                return com.example.turnserver.protocol.MessageType.ALLOCATE_ERROR_RESPONSE;
            case REFRESH_REQUEST:
                return com.example.turnserver.protocol.MessageType.REFRESH_ERROR_RESPONSE;
            case CREATE_PERMISSION_REQUEST:
                return com.example.turnserver.protocol.MessageType.CREATE_PERMISSION_ERROR_RESPONSE;
            case CHANNEL_BIND_REQUEST:
                return com.example.turnserver.protocol.MessageType.CHANNEL_BIND_ERROR_RESPONSE;
            default:
                return com.example.turnserver.protocol.MessageType.BINDING_ERROR_RESPONSE;
        }
    }
    
    private StunAttribute createErrorCodeAttribute(int errorCode, String reason) {
        byte[] reasonBytes = reason.getBytes();
        byte[] value = new byte[4 + reasonBytes.length];
        
        // Error code is encoded as: 0 0 class number
        value[2] = (byte) (errorCode / 100);
        value[3] = (byte) (errorCode % 100);
        
        System.arraycopy(reasonBytes, 0, value, 4, reasonBytes.length);
        
        return new StunAttribute(AttributeType.ERROR_CODE, value);
    }
    
    private byte[] createMessageBytesForIntegrityCheck(StunMessage message) {
        // This is a simplified implementation
        // In a real implementation, you would serialize the STUN message
        // up to but not including the MESSAGE-INTEGRITY attribute
        String messageStr = message.getMessageType().getValue() + "_" + message.calculateLength();
        return messageStr.getBytes();
    }
    
    private String extractPasswordFromHash(String hashedPassword) {
        // This is a simplified extraction for demo purposes
        // In a real implementation, you would need to handle the stored password format properly
        return hashedPassword.split(":").length > 1 ? hashedPassword.split(":")[1] : hashedPassword;
    }
    
    private void startNonceCleanupTask() {
        Thread cleanupThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(NONCE_CLEANUP_INTERVAL);
                    cleanupExpiredNonces();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.setName("NonceCleanupThread");
        cleanupThread.start();
    }
    
    private void cleanupExpiredNonces() {
        long currentTime = System.currentTimeMillis() / 1000;
        nonceCache.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue()) > NONCE_VALIDITY_SECONDS);
    }
}