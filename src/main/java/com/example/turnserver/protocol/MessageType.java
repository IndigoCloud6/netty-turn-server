package com.example.turnserver.protocol;

/**
 * STUN/TURN message types as defined in RFC 5389 and RFC 5766
 */
public enum MessageType {
    // STUN Binding methods
    BINDING_REQUEST(0x0001),
    BINDING_RESPONSE(0x0101),
    BINDING_ERROR_RESPONSE(0x0111),
    
    // TURN Allocate methods
    ALLOCATE_REQUEST(0x0003),
    ALLOCATE_RESPONSE(0x0103),
    ALLOCATE_ERROR_RESPONSE(0x0113),
    
    // TURN Refresh methods
    REFRESH_REQUEST(0x0004),
    REFRESH_RESPONSE(0x0104),
    REFRESH_ERROR_RESPONSE(0x0114),
    
    // TURN Send methods
    SEND_INDICATION(0x0016),
    
    // TURN Data methods
    DATA_INDICATION(0x0017),
    
    // TURN CreatePermission methods
    CREATE_PERMISSION_REQUEST(0x0008),
    CREATE_PERMISSION_RESPONSE(0x0108),
    CREATE_PERMISSION_ERROR_RESPONSE(0x0118),
    
    // TURN ChannelBind methods
    CHANNEL_BIND_REQUEST(0x0009),
    CHANNEL_BIND_RESPONSE(0x0109),
    CHANNEL_BIND_ERROR_RESPONSE(0x0119);
    
    private final int value;
    
    MessageType(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    public static MessageType fromValue(int value) {
        for (MessageType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown message type: " + value);
    }
    
    public boolean isRequest() {
        return (value & 0x0110) == 0x0000;
    }
    
    public boolean isResponse() {
        return (value & 0x0110) == 0x0100;
    }
    
    public boolean isErrorResponse() {
        return (value & 0x0110) == 0x0110;
    }
    
    public boolean isIndication() {
        return (value & 0x0110) == 0x0010;
    }
}