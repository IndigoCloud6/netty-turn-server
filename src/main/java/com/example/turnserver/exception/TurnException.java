package com.example.turnserver.exception;

/**
 * Base exception for TURN server related errors
 */
public class TurnException extends RuntimeException {
    
    private final int errorCode;
    
    public TurnException(String message) {
        super(message);
        this.errorCode = 500; // Default error code
    }
    
    public TurnException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public TurnException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = 500;
    }
    
    public TurnException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
    
    // STUN/TURN error codes as defined in RFC 5389 and RFC 5766
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int UNKNOWN_ATTRIBUTE = 420;
    public static final int ALLOCATION_MISMATCH = 437;
    public static final int STALE_NONCE = 438;
    public static final int ADDRESS_FAMILY_NOT_SUPPORTED = 440;
    public static final int WRONG_CREDENTIALS = 441;
    public static final int UNSUPPORTED_TRANSPORT_PROTOCOL = 442;
    public static final int ALLOCATION_QUOTA_REACHED = 486;
    public static final int ROLE_CONFLICT = 487;
    public static final int SERVER_ERROR = 500;
    public static final int INSUFFICIENT_CAPACITY = 508;
    
    public static TurnException badRequest(String message) {
        return new TurnException(message, BAD_REQUEST);
    }
    
    public static TurnException unauthorized(String message) {
        return new TurnException(message, UNAUTHORIZED);
    }
    
    public static TurnException forbidden(String message) {
        return new TurnException(message, FORBIDDEN);
    }
    
    public static TurnException unknownAttribute(String message) {
        return new TurnException(message, UNKNOWN_ATTRIBUTE);
    }
    
    public static TurnException allocationMismatch(String message) {
        return new TurnException(message, ALLOCATION_MISMATCH);
    }
    
    public static TurnException staleNonce(String message) {
        return new TurnException(message, STALE_NONCE);
    }
    
    public static TurnException addressFamilyNotSupported(String message) {
        return new TurnException(message, ADDRESS_FAMILY_NOT_SUPPORTED);
    }
    
    public static TurnException wrongCredentials(String message) {
        return new TurnException(message, WRONG_CREDENTIALS);
    }
    
    public static TurnException unsupportedTransportProtocol(String message) {
        return new TurnException(message, UNSUPPORTED_TRANSPORT_PROTOCOL);
    }
    
    public static TurnException allocationQuotaReached(String message) {
        return new TurnException(message, ALLOCATION_QUOTA_REACHED);
    }
    
    public static TurnException serverError(String message) {
        return new TurnException(message, SERVER_ERROR);
    }
    
    public static TurnException insufficientCapacity(String message) {
        return new TurnException(message, INSUFFICIENT_CAPACITY);
    }
}