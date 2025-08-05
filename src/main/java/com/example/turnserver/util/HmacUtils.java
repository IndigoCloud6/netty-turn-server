package com.example.turnserver.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * HMAC utility functions for TURN server authentication
 */
public class HmacUtils {
    
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final String MD5_ALGORITHM = "MD5";
    
    private static final SecureRandom RANDOM = new SecureRandom();
    
    /**
     * Calculate HMAC-SHA1
     */
    public static byte[] calculateHmacSha1(byte[] data, byte[] key) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, HMAC_SHA1_ALGORITHM);
            mac.init(secretKeySpec);
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error calculating HMAC-SHA1", e);
        }
    }
    
    /**
     * Calculate HMAC-SHA1 with string inputs
     */
    public static byte[] calculateHmacSha1(String data, String key) {
        return calculateHmacSha1(data.getBytes(StandardCharsets.UTF_8), 
                                 key.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Calculate HMAC-SHA256
     */
    public static byte[] calculateHmacSha256(byte[] data, byte[] key) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, HMAC_SHA256_ALGORITHM);
            mac.init(secretKeySpec);
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error calculating HMAC-SHA256", e);
        }
    }
    
    /**
     * Calculate HMAC-SHA256 with string inputs
     */
    public static byte[] calculateHmacSha256(String data, String key) {
        return calculateHmacSha256(data.getBytes(StandardCharsets.UTF_8), 
                                   key.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Calculate MD5 hash for TURN long-term credentials
     * MD5(username:realm:password)
     */
    public static byte[] calculateMd5Hash(String username, String realm, String password) {
        try {
            String input = username + ":" + realm + ":" + password;
            MessageDigest md = MessageDigest.getInstance(MD5_ALGORITHM);
            return md.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
    
    /**
     * Generate TURN long-term credential key
     * This is typically MD5(username:realm:password)
     */
    public static byte[] generateLongTermKey(String username, String realm, String password) {
        return calculateMd5Hash(username, realm, password);
    }
    
    /**
     * Verify HMAC-SHA1 signature
     */
    public static boolean verifyHmacSha1(byte[] data, byte[] key, byte[] expectedHmac) {
        byte[] calculatedHmac = calculateHmacSha1(data, key);
        return constantTimeEquals(calculatedHmac, expectedHmac);
    }
    
    /**
     * Verify HMAC-SHA256 signature
     */
    public static boolean verifyHmacSha256(byte[] data, byte[] key, byte[] expectedHmac) {
        byte[] calculatedHmac = calculateHmacSha256(data, key);
        return constantTimeEquals(calculatedHmac, expectedHmac);
    }
    
    /**
     * Generate a random nonce for TURN authentication
     */
    public static String generateNonce() {
        byte[] nonceBytes = new byte[16];
        RANDOM.nextBytes(nonceBytes);
        return CryptoUtils.bytesToHex(nonceBytes);
    }
    
    /**
     * Generate a random nonce with custom length
     */
    public static String generateNonce(int length) {
        byte[] nonceBytes = new byte[length];
        RANDOM.nextBytes(nonceBytes);
        return CryptoUtils.bytesToHex(nonceBytes);
    }
    
    /**
     * Constant time comparison to prevent timing attacks
     */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        
        return result == 0;
    }
    
    /**
     * Create STUN MESSAGE-INTEGRITY attribute value
     * This is HMAC-SHA1 of the STUN message up to but not including the MESSAGE-INTEGRITY attribute
     */
    public static byte[] createStunMessageIntegrity(byte[] stunMessage, byte[] key) {
        return calculateHmacSha1(stunMessage, key);
    }
    
    /**
     * Verify STUN MESSAGE-INTEGRITY attribute
     */
    public static boolean verifyStunMessageIntegrity(byte[] stunMessage, byte[] key, byte[] messageIntegrity) {
        byte[] calculatedIntegrity = createStunMessageIntegrity(stunMessage, key);
        return constantTimeEquals(calculatedIntegrity, messageIntegrity);
    }
    
    /**
     * Generate short-term credential password for TURN
     * This is typically a random string or derived from shared secret
     */
    public static String generateShortTermPassword() {
        byte[] passwordBytes = new byte[20];
        RANDOM.nextBytes(passwordBytes);
        return CryptoUtils.bytesToBase64(passwordBytes);
    }
    
    /**
     * Generate username for short-term credentials
     * Format: timestamp:username
     */
    public static String generateShortTermUsername(String baseUsername) {
        long timestamp = System.currentTimeMillis() / 1000; // Unix timestamp
        return timestamp + ":" + baseUsername;
    }
    
    /**
     * Parse timestamp from short-term username
     */
    public static long parseTimestampFromUsername(String username) {
        if (username == null || !username.contains(":")) {
            return -1;
        }
        
        try {
            String timestampStr = username.substring(0, username.indexOf(':'));
            return Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /**
     * Check if short-term credential is expired
     */
    public static boolean isShortTermCredentialExpired(String username, long maxAgeSeconds) {
        long timestamp = parseTimestampFromUsername(username);
        if (timestamp == -1) {
            return true;
        }
        
        long currentTime = System.currentTimeMillis() / 1000;
        return (currentTime - timestamp) > maxAgeSeconds;
    }
}