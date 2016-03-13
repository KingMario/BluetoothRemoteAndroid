package com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public abstract class KeyboardEvent extends OutboundPacket {

    enum KeyboardEventType {
        SINGLE_KEY,
        KEY_COMBO
    }

    private KeyboardEventType mKeyboardEventType;

    public KeyboardEvent(KeyboardEventType keyboardEventType) {
        super(PacketType.KEYBOARD_EVENT);
        mKeyboardEventType = keyboardEventType;
    }

    public byte getEventType() {
        return (byte)(mKeyboardEventType.ordinal()+1);
    }

    @Override
    public void writeToStream(OutputStream outputStream) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1+1);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(getIntegerType());
        buffer.put(getEventType());
        outputStream.write(buffer.array());
    }
}
