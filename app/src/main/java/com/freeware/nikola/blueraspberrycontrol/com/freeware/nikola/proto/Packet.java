package com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto;

/**
 * Created by nikola on 1/3/16.
 */
public abstract class Packet {

    public enum PacketType {
        SCREE_FRAME,
        KEYBOARD_EVENT,
        MOUSE_EVENT,
        REQUEST_SCREEN_REFRESH,
        CONNECTION_INIT,
        ACK

    }

    public static PacketType fromByte(byte b) {
        switch (b) {
            case 1: return PacketType.SCREE_FRAME;
            case 2: return PacketType.KEYBOARD_EVENT;
            case 3: return PacketType.MOUSE_EVENT;
            case 4: return PacketType.REQUEST_SCREEN_REFRESH;
            case 5: return PacketType.CONNECTION_INIT;
            case 6: return PacketType.ACK;
            default:throw new IllegalArgumentException("Not valid type");
        }
    }

    private PacketType type;

    public Packet(PacketType type) {
        this.type = type;
    }

    public PacketType getType() {
        return type;
    }

    public void setType(PacketType type) {
        this.type = type;
    }

    public byte getIntegerType() {
        return (byte)(type.ordinal()+1);
    }

}
