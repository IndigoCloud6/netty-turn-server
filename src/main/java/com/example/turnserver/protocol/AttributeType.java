package com.example.turnserver.protocol;

/**
 * STUN/TURN attribute types as defined in RFC 5389 and RFC 5766
 */
public enum AttributeType {
    // Comprehension-required range (0x0000-0x7FFF)
    MAPPED_ADDRESS(0x0001),
    USERNAME(0x0006),
    MESSAGE_INTEGRITY(0x0008),
    ERROR_CODE(0x0009),
    UNKNOWN_ATTRIBUTES(0x000A),
    REALM(0x0014),
    NONCE(0x0015),
    XOR_MAPPED_ADDRESS(0x0020),
    
    // TURN specific attributes
    CHANNEL_NUMBER(0x000C),
    LIFETIME(0x000D),
    XOR_PEER_ADDRESS(0x0012),
    DATA(0x0013),
    XOR_RELAYED_ADDRESS(0x0016),
    EVEN_PORT(0x0018),
    REQUESTED_TRANSPORT(0x0019),
    DONT_FRAGMENT(0x001A),
    RESERVATION_TOKEN(0x0022),
    
    // Comprehension-optional range (0x8000-0xFFFF)
    SOFTWARE(0x8022),
    ALTERNATE_SERVER(0x8023),
    FINGERPRINT(0x8028),
    
    // Additional TURN attributes
    RESPONSE_ORIGIN(0x802B),
    OTHER_ADDRESS(0x802C);
    
    private final int value;
    
    AttributeType(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    public static AttributeType fromValue(int value) {
        for (AttributeType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null; // Unknown attribute
    }
    
    public boolean isComprehensionRequired() {
        return value <= 0x7FFF;
    }
    
    public boolean isComprehensionOptional() {
        return value >= 0x8000;
    }
}