package com.example.turnserver.protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Represents a STUN/TURN message as defined in RFC 5389 and RFC 5766
 */
public class StunMessage {
    // STUN magic cookie as defined in RFC 5389
    public static final int MAGIC_COOKIE = 0x2112A442;
    
    // STUN header length
    public static final int HEADER_LENGTH = 20;
    
    private final MessageType messageType;
    private final byte[] transactionId;
    private final List<StunAttribute> attributes;
    
    public StunMessage(MessageType messageType, byte[] transactionId) {
        this.messageType = Objects.requireNonNull(messageType, "Message type cannot be null");
        if (transactionId == null || transactionId.length != 12) {
            throw new IllegalArgumentException("Transaction ID must be 12 bytes");
        }
        this.transactionId = Arrays.copyOf(transactionId, 12);
        this.attributes = new ArrayList<>();
    }
    
    public MessageType getMessageType() {
        return messageType;
    }
    
    public byte[] getTransactionId() {
        return Arrays.copyOf(transactionId, transactionId.length);
    }
    
    public List<StunAttribute> getAttributes() {
        return new ArrayList<>(attributes);
    }
    
    public void addAttribute(StunAttribute attribute) {
        attributes.add(Objects.requireNonNull(attribute, "Attribute cannot be null"));
    }
    
    public StunAttribute getAttribute(AttributeType type) {
        for (StunAttribute attr : attributes) {
            if (attr.getType() == type) {
                return attr;
            }
        }
        return null;
    }
    
    public boolean hasAttribute(AttributeType type) {
        return getAttribute(type) != null;
    }
    
    public void removeAttribute(AttributeType type) {
        attributes.removeIf(attr -> attr.getType() == type);
    }
    
    public int calculateLength() {
        int length = 0;
        for (StunAttribute attr : attributes) {
            length += 4; // Type (2 bytes) + Length (2 bytes)
            length += attr.getPaddedLength(); // Value with padding
        }
        return length;
    }
    
    public boolean isRequest() {
        return messageType.isRequest();
    }
    
    public boolean isResponse() {
        return messageType.isResponse();
    }
    
    public boolean isErrorResponse() {
        return messageType.isErrorResponse();
    }
    
    public boolean isIndication() {
        return messageType.isIndication();
    }
    
    public boolean isStunBindingMessage() {
        return messageType == MessageType.BINDING_REQUEST ||
               messageType == MessageType.BINDING_RESPONSE ||
               messageType == MessageType.BINDING_ERROR_RESPONSE;
    }
    
    public boolean isTurnMessage() {
        return !isStunBindingMessage();
    }
    
    @Override
    public String toString() {
        return "StunMessage{" +
                "messageType=" + messageType +
                ", transactionId=" + Arrays.toString(transactionId) +
                ", attributeCount=" + attributes.size() +
                ", length=" + calculateLength() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StunMessage that = (StunMessage) o;
        return messageType == that.messageType &&
               Arrays.equals(transactionId, that.transactionId) &&
               Objects.equals(attributes, that.attributes);
    }
    
    @Override
    public int hashCode() {
        int result = Objects.hash(messageType, attributes);
        result = 31 * result + Arrays.hashCode(transactionId);
        return result;
    }
}