package com.example.turnserver.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cryptographic utility functions for TURN server
 */
public class CryptoUtils {
    
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
    /**
     * Convert byte array to hexadecimal string
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
    
    /**
     * Convert hexadecimal string to byte array
     */
    public static byte[] hexToBytes(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }
        
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
    
    /**
     * Convert byte array to Base64 string
     */
    public static String bytesToBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
    
    /**
     * Convert Base64 string to byte array
     */
    public static byte[] base64ToBytes(String base64) {
        return Base64.getDecoder().decode(base64);
    }
    
    /**
     * Generate random bytes
     */
    public static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
    
    /**
     * Generate random string of specified length
     */
    public static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        
        return sb.toString();
    }
    
    /**
     * Generate random alphanumeric string
     */
    public static String generateRandomAlphanumeric(int length) {
        return generateRandomString(length);
    }
    
    /**
     * Generate UUID-like string
     */
    public static String generateUuid() {
        byte[] randomBytes = generateRandomBytes(16);
        
        // Set version (4) and variant bits
        randomBytes[6] = (byte) ((randomBytes[6] & 0x0F) | 0x40);
        randomBytes[8] = (byte) ((randomBytes[8] & 0x3F) | 0x80);
        
        String hex = bytesToHex(randomBytes);
        return hex.substring(0, 8) + "-" +
               hex.substring(8, 12) + "-" +
               hex.substring(12, 16) + "-" +
               hex.substring(16, 20) + "-" +
               hex.substring(20, 32);
    }
    
    /**
     * Generate AES key
     */
    public static SecretKey generateAESKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGen.init(256);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("AES algorithm not available", e);
        }
    }
    
    /**
     * Create AES key from byte array
     */
    public static SecretKey createAESKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, AES_ALGORITHM);
    }
    
    /**
     * Encrypt data using AES
     */
    public static byte[] encryptAES(byte[] data, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }
    
    /**
     * Decrypt data using AES
     */
    public static byte[] decryptAES(byte[] encryptedData, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }
    
    /**
     * Encrypt string using AES
     */
    public static String encryptString(String plaintext, SecretKey key) {
        byte[] encrypted = encryptAES(plaintext.getBytes(StandardCharsets.UTF_8), key);
        return bytesToBase64(encrypted);
    }
    
    /**
     * Decrypt string using AES
     */
    public static String decryptString(String encryptedBase64, SecretKey key) {
        byte[] encrypted = base64ToBytes(encryptedBase64);
        byte[] decrypted = decryptAES(encrypted, key);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
    
    /**
     * Hash password using a simple method (for demo purposes)
     * In production, use bcrypt, scrypt, or Argon2
     */
    public static String hashPassword(String password) {
        // This is a simple implementation for demo
        // In production, use proper password hashing
        byte[] salt = generateRandomBytes(16);
        String saltStr = bytesToBase64(salt);
        String hash = bytesToBase64(HmacUtils.calculateHmacSha256(password, saltStr));
        return saltStr + ":" + hash;
    }
    
    /**
     * Verify password hash
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        try {
            String[] parts = hashedPassword.split(":");
            if (parts.length != 2) {
                return false;
            }
            
            String salt = parts[0];
            String expectedHash = parts[1];
            String actualHash = bytesToBase64(HmacUtils.calculateHmacSha256(password, salt));
            
            return constantTimeEquals(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Constant time string comparison
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        
        return result == 0;
    }
    
    /**
     * Generate session ID
     */
    public static String generateSessionId() {
        return generateUuid();
    }
    
    /**
     * Generate allocation ID
     */
    public static String generateAllocationId() {
        return generateUuid();
    }
    
    /**
     * Generate secure token
     */
    public static String generateSecureToken(int length) {
        byte[] tokenBytes = generateRandomBytes(length);
        return bytesToBase64(tokenBytes);
    }
}