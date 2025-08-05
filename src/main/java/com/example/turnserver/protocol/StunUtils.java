package com.example.turnserver.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Utility class for STUN/TURN message parsing and creation
 */
public class StunUtils {
    
    private static final SecureRandom RANDOM = new SecureRandom();
    
    /**
     * Parse a STUN message from a ByteBuf
     */
    public static StunMessage parseMessage(ByteBuf buffer) {
        if (buffer.readableBytes() < StunMessage.HEADER_LENGTH) {
            throw new IllegalArgumentException("Buffer too small for STUN header");
        }
        
        // Read message type
        int messageTypeValue = buffer.readUnsignedShort();
        MessageType messageType = MessageType.fromValue(messageTypeValue);
        
        // Read message length
        int messageLength = buffer.readUnsignedShort();
        
        // Read magic cookie
        int magicCookie = buffer.readInt();
        if (magicCookie != StunMessage.MAGIC_COOKIE) {
            throw new IllegalArgumentException("Invalid magic cookie: " + Integer.toHexString(magicCookie));
        }
        
        // Read transaction ID
        byte[] transactionId = new byte[12];
        buffer.readBytes(transactionId);
        
        StunMessage message = new StunMessage(messageType, transactionId);
        
        // Parse attributes
        int remainingLength = messageLength;
        while (remainingLength > 0 && buffer.readableBytes() >= 4) {
            int attrType = buffer.readUnsignedShort();
            int attrLength = buffer.readUnsignedShort();
            
            AttributeType attributeType = AttributeType.fromValue(attrType);
            
            if (buffer.readableBytes() < attrLength) {
                throw new IllegalArgumentException("Not enough bytes for attribute value");
            }
            
            byte[] value = new byte[attrLength];
            buffer.readBytes(value);
            
            if (attributeType != null) {
                message.addAttribute(new StunAttribute(attributeType, value));
            }
            
            // Skip padding
            int padding = (4 - (attrLength % 4)) % 4;
            buffer.skipBytes(Math.min(padding, buffer.readableBytes()));
            
            remainingLength -= 4 + attrLength + padding;
        }
        
        return message;
    }
    
    /**
     * Encode a STUN message to a ByteBuf
     */
    public static ByteBuf encodeMessage(StunMessage message) {
        int messageLength = message.calculateLength();
        ByteBuf buffer = Unpooled.buffer(StunMessage.HEADER_LENGTH + messageLength);
        
        // Write header
        buffer.writeShort(message.getMessageType().getValue());
        buffer.writeShort(messageLength);
        buffer.writeInt(StunMessage.MAGIC_COOKIE);
        buffer.writeBytes(message.getTransactionId());
        
        // Write attributes
        for (StunAttribute attr : message.getAttributes()) {
            buffer.writeShort(attr.getType().getValue());
            buffer.writeShort(attr.getLength());
            buffer.writeBytes(attr.getValue());
            
            // Add padding
            int padding = (4 - (attr.getLength() % 4)) % 4;
            for (int i = 0; i < padding; i++) {
                buffer.writeByte(0);
            }
        }
        
        return buffer;
    }
    
    /**
     * Generate a random transaction ID
     */
    public static byte[] generateTransactionId() {
        byte[] transactionId = new byte[12];
        RANDOM.nextBytes(transactionId);
        return transactionId;
    }
    
    /**
     * Create a USERNAME attribute
     */
    public static StunAttribute createUsernameAttribute(String username) {
        return new StunAttribute(AttributeType.USERNAME, username.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Create a REALM attribute
     */
    public static StunAttribute createRealmAttribute(String realm) {
        return new StunAttribute(AttributeType.REALM, realm.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Create a NONCE attribute
     */
    public static StunAttribute createNonceAttribute(String nonce) {
        return new StunAttribute(AttributeType.NONCE, nonce.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Create a SOFTWARE attribute
     */
    public static StunAttribute createSoftwareAttribute(String software) {
        return new StunAttribute(AttributeType.SOFTWARE, software.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Create an ERROR-CODE attribute
     */
    public static StunAttribute createErrorCodeAttribute(int errorCode, String reason) {
        byte[] reasonBytes = reason.getBytes(StandardCharsets.UTF_8);
        byte[] value = new byte[4 + reasonBytes.length];
        
        // Error code is encoded as: 0 0 class number
        value[2] = (byte) (errorCode / 100);
        value[3] = (byte) (errorCode % 100);
        
        System.arraycopy(reasonBytes, 0, value, 4, reasonBytes.length);
        
        return new StunAttribute(AttributeType.ERROR_CODE, value);
    }
    
    /**
     * Create a LIFETIME attribute
     */
    public static StunAttribute createLifetimeAttribute(int seconds) {
        byte[] value = new byte[4];
        value[0] = (byte) ((seconds >> 24) & 0xFF);
        value[1] = (byte) ((seconds >> 16) & 0xFF);
        value[2] = (byte) ((seconds >> 8) & 0xFF);
        value[3] = (byte) (seconds & 0xFF);
        return new StunAttribute(AttributeType.LIFETIME, value);
    }
    
    /**
     * Create a REQUESTED-TRANSPORT attribute
     */
    public static StunAttribute createRequestedTransportAttribute(byte protocol) {
        byte[] value = new byte[4];
        value[0] = protocol; // 17 for UDP
        // bytes 1-3 are reserved and set to 0
        return new StunAttribute(AttributeType.REQUESTED_TRANSPORT, value);
    }
    
    /**
     * Create an XOR-MAPPED-ADDRESS attribute
     */
    public static StunAttribute createXorMappedAddressAttribute(InetSocketAddress address, byte[] transactionId) {
        return createXorAddressAttribute(AttributeType.XOR_MAPPED_ADDRESS, address, transactionId);
    }
    
    /**
     * Create an XOR-RELAYED-ADDRESS attribute
     */
    public static StunAttribute createXorRelayedAddressAttribute(InetSocketAddress address, byte[] transactionId) {
        return createXorAddressAttribute(AttributeType.XOR_RELAYED_ADDRESS, address, transactionId);
    }
    
    /**
     * Create an XOR-PEER-ADDRESS attribute
     */
    public static StunAttribute createXorPeerAddressAttribute(InetSocketAddress address, byte[] transactionId) {
        return createXorAddressAttribute(AttributeType.XOR_PEER_ADDRESS, address, transactionId);
    }
    
    private static StunAttribute createXorAddressAttribute(AttributeType type, InetSocketAddress address, byte[] transactionId) {
        byte[] addrBytes = address.getAddress().getAddress();
        boolean isIPv6 = addrBytes.length == 16;
        
        byte[] value = new byte[isIPv6 ? 20 : 8];
        
        // Family
        value[1] = (byte) (isIPv6 ? 0x02 : 0x01);
        
        // XOR port with magic cookie high 16 bits
        int xorPort = address.getPort() ^ (StunMessage.MAGIC_COOKIE >> 16);
        value[2] = (byte) ((xorPort >> 8) & 0xFF);
        value[3] = (byte) (xorPort & 0xFF);
        
        // XOR address
        if (isIPv6) {
            // For IPv6, XOR with magic cookie + transaction ID
            byte[] xorMask = new byte[16];
            System.arraycopy(intToBytes(StunMessage.MAGIC_COOKIE), 0, xorMask, 0, 4);
            System.arraycopy(transactionId, 0, xorMask, 4, 12);
            
            for (int i = 0; i < 16; i++) {
                value[4 + i] = (byte) (addrBytes[i] ^ xorMask[i]);
            }
        } else {
            // For IPv4, XOR with magic cookie
            for (int i = 0; i < 4; i++) {
                value[4 + i] = (byte) (addrBytes[i] ^ ((StunMessage.MAGIC_COOKIE >> (24 - i * 8)) & 0xFF));
            }
        }
        
        return new StunAttribute(type, value);
    }
    
    /**
     * Parse an XOR address attribute
     */
    public static InetSocketAddress parseXorAddressAttribute(StunAttribute attribute, byte[] transactionId) {
        byte[] value = attribute.getValue();
        if (value.length < 8) {
            throw new IllegalArgumentException("Invalid XOR address attribute length");
        }
        
        int family = value[1] & 0xFF;
        boolean isIPv6 = (family == 0x02);
        
        // XOR port
        int xorPort = ((value[2] & 0xFF) << 8) | (value[3] & 0xFF);
        int port = xorPort ^ (StunMessage.MAGIC_COOKIE >> 16);
        
        // XOR address
        byte[] addrBytes;
        if (isIPv6) {
            if (value.length < 20) {
                throw new IllegalArgumentException("Invalid IPv6 XOR address attribute length");
            }
            addrBytes = new byte[16];
            byte[] xorMask = new byte[16];
            System.arraycopy(intToBytes(StunMessage.MAGIC_COOKIE), 0, xorMask, 0, 4);
            System.arraycopy(transactionId, 0, xorMask, 4, 12);
            
            for (int i = 0; i < 16; i++) {
                addrBytes[i] = (byte) (value[4 + i] ^ xorMask[i]);
            }
        } else {
            addrBytes = new byte[4];
            for (int i = 0; i < 4; i++) {
                addrBytes[i] = (byte) (value[4 + i] ^ ((StunMessage.MAGIC_COOKIE >> (24 - i * 8)) & 0xFF));
            }
        }
        
        try {
            InetAddress inetAddress = InetAddress.getByAddress(addrBytes);
            return new InetSocketAddress(inetAddress, port);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IP address", e);
        }
    }
    
    private static byte[] intToBytes(int value) {
        return new byte[] {
            (byte) ((value >> 24) & 0xFF),
            (byte) ((value >> 16) & 0xFF),
            (byte) ((value >> 8) & 0xFF),
            (byte) (value & 0xFF)
        };
    }
}