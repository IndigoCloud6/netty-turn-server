package com.example.turnserver.exception;

/**
 * Exception for authentication related errors
 */
public class AuthenticationException extends TurnException {
    
    public AuthenticationException(String message) {
        super(message, UNAUTHORIZED);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause, UNAUTHORIZED);
    }
    
    public AuthenticationException(String message, int errorCode) {
        super(message, errorCode);
    }
    
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Invalid credentials", WRONG_CREDENTIALS);
    }
    
    public static AuthenticationException missingCredentials() {
        return new AuthenticationException("Missing credentials", UNAUTHORIZED);
    }
    
    public static AuthenticationException expiredCredentials() {
        return new AuthenticationException("Expired credentials", STALE_NONCE);
    }
    
    public static AuthenticationException malformedCredentials() {
        return new AuthenticationException("Malformed credentials", BAD_REQUEST);
    }
    
    public static AuthenticationException userNotFound() {
        return new AuthenticationException("User not found", WRONG_CREDENTIALS);
    }
    
    public static AuthenticationException userDisabled() {
        return new AuthenticationException("User account is disabled", FORBIDDEN);
    }
    
    public static AuthenticationException invalidNonce() {
        return new AuthenticationException("Invalid nonce", STALE_NONCE);
    }
    
    public static AuthenticationException missingMessageIntegrity() {
        return new AuthenticationException("Missing MESSAGE-INTEGRITY attribute", UNAUTHORIZED);
    }
    
    public static AuthenticationException invalidMessageIntegrity() {
        return new AuthenticationException("Invalid MESSAGE-INTEGRITY", WRONG_CREDENTIALS);
    }
}