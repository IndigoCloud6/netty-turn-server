package com.example.turnserver.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Network utility functions for TURN server
 */
public class NetworkUtils {
    
    private static final int MIN_EPHEMERAL_PORT = 49152;
    private static final int MAX_EPHEMERAL_PORT = 65535;
    
    /**
     * Get all available network interfaces
     */
    public static List<NetworkInterface> getAvailableNetworkInterfaces() {
        List<NetworkInterface> interfaces = new ArrayList<>();
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (ni.isUp() && !ni.isLoopback() && !ni.isVirtual()) {
                    interfaces.add(ni);
                }
            }
        } catch (SocketException e) {
            // Return empty list on error
        }
        return interfaces;
    }
    
    /**
     * Get the local machine's IP addresses
     */
    public static List<String> getLocalIPAddresses() {
        List<String> addresses = new ArrayList<>();
        try {
            for (NetworkInterface ni : getAvailableNetworkInterfaces()) {
                for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                    if (!addr.isLoopbackAddress() && !addr.isLinkLocalAddress()) {
                        addresses.add(addr.getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
            // Fallback to localhost
            try {
                addresses.add(InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException ex) {
                addresses.add("127.0.0.1");
            }
        }
        return addresses;
    }
    
    /**
     * Get the primary IP address of the local machine
     */
    public static String getPrimaryIPAddress() {
        List<String> addresses = getLocalIPAddresses();
        if (!addresses.isEmpty()) {
            return addresses.get(0);
        }
        return "127.0.0.1";
    }
    
    /**
     * Check if an IP address is valid IPv4
     */
    public static boolean isValidIPv4(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        
        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Check if an IP address is valid IPv6
     */
    public static boolean isValidIPv6(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        try {
            InetAddress addr = InetAddress.getByName(ip);
            return addr.getAddress().length == 16;
        } catch (UnknownHostException e) {
            return false;
        }
    }
    
    /**
     * Check if a port number is valid
     */
    public static boolean isValidPort(int port) {
        return port >= 1 && port <= 65535;
    }
    
    /**
     * Check if a port is in the ephemeral range
     */
    public static boolean isEphemeralPort(int port) {
        return port >= MIN_EPHEMERAL_PORT && port <= MAX_EPHEMERAL_PORT;
    }
    
    /**
     * Generate a random port in the ephemeral range
     */
    public static int generateRandomPort() {
        return ThreadLocalRandom.current().nextInt(MIN_EPHEMERAL_PORT, MAX_EPHEMERAL_PORT + 1);
    }
    
    /**
     * Generate a random port in a specific range
     */
    public static int generateRandomPort(int minPort, int maxPort) {
        if (minPort > maxPort || !isValidPort(minPort) || !isValidPort(maxPort)) {
            throw new IllegalArgumentException("Invalid port range: " + minPort + "-" + maxPort);
        }
        return ThreadLocalRandom.current().nextInt(minPort, maxPort + 1);
    }
    
    /**
     * Check if an IP address is private (RFC 1918)
     */
    public static boolean isPrivateIP(String ip) {
        if (!isValidIPv4(ip)) {
            return false;
        }
        
        String[] parts = ip.split("\\.");
        int first = Integer.parseInt(parts[0]);
        int second = Integer.parseInt(parts[1]);
        
        // 10.0.0.0/8
        if (first == 10) {
            return true;
        }
        
        // 172.16.0.0/12
        if (first == 172 && second >= 16 && second <= 31) {
            return true;
        }
        
        // 192.168.0.0/16
        if (first == 192 && second == 168) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if an IP address is loopback
     */
    public static boolean isLoopbackIP(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            return addr.isLoopbackAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }
    
    /**
     * Format an IP address and port as a string
     */
    public static String formatAddress(String ip, int port) {
        if (isValidIPv6(ip)) {
            return "[" + ip + "]:" + port;
        } else {
            return ip + ":" + port;
        }
    }
    
    /**
     * Parse an address string into IP and port
     */
    public static String[] parseAddress(String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }
        
        // IPv6 format: [ip]:port
        if (address.startsWith("[")) {
            int closeBracket = address.indexOf(']');
            if (closeBracket == -1 || closeBracket >= address.length() - 1) {
                return null;
            }
            String ip = address.substring(1, closeBracket);
            String portStr = address.substring(closeBracket + 2); // Skip ]:
            return new String[]{ip, portStr};
        }
        
        // IPv4 format: ip:port
        int lastColon = address.lastIndexOf(':');
        if (lastColon == -1) {
            return null;
        }
        
        String ip = address.substring(0, lastColon);
        String portStr = address.substring(lastColon + 1);
        return new String[]{ip, portStr};
    }
}