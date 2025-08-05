package com.example.turnserver.protocol;

import java.util.Arrays;

/**
 * Represents a STUN/TURN attribute
 */
public class StunAttribute {
    private final AttributeType type;
    private final byte[] value;
    
    public StunAttribute(AttributeType type, byte[] value) {
        this.type = type;
        this.value = value != null ? Arrays.copyOf(value, value.length) : new byte[0];
    }
    
    public AttributeType getType() {
        return type;
    }
    
    public byte[] getValue() {
        return Arrays.copyOf(value, value.length);
    }
    
    public int getLength() {
        return value.length;
    }
    
    public int getPaddedLength() {
        // STUN attributes must be padded to 4-byte boundaries
        return (value.length + 3) & ~3;
    }
    
    public String getValueAsString() {
        return new String(value);
    }
    
    public int getValueAsInt() {
        if (value.length < 4) {
            throw new IllegalStateException("Attribute value too short for int");
        }
        return ((value[0] & 0xFF) << 24) |
               ((value[1] & 0xFF) << 16) |
               ((value[2] & 0xFF) << 8) |
               (value[3] & 0xFF);
    }
    
    public short getValueAsShort() {
        if (value.length < 2) {
            throw new IllegalStateException("Attribute value too short for short");
        }
        return (short) (((value[0] & 0xFF) << 8) | (value[1] & 0xFF));
    }
    
    public byte getValueAsByte() {
        if (value.length < 1) {
            throw new IllegalStateException("Attribute value too short for byte");
        }
        return value[0];
    }
    
    @Override
    public String toString() {
        return "StunAttribute{" +
                "type=" + type +
                ", length=" + value.length +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StunAttribute that = (StunAttribute) o;
        return type == that.type && Arrays.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }
}