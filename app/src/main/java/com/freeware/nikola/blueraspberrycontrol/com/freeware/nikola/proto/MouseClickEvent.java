package com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by nikola on 1/3/16.
 */
public class MouseClickEvent extends OutboundPacket {

    public enum MouseButton {
        LEFT_BUTTON,
        RIGHT_BUTTON,
        MIDDLE_BUTTON,
        DOUBLE_LEFT_BUTTON
    }

    private double mX;
    private double mY;
    private MouseButton mButton;

    public MouseClickEvent(double x, double y, MouseButton button) {
        super(PacketType.MOUSE_EVENT);
        mX = x;
        mY = y;
        mButton = button;
    }

    @Override
    public void writeToStream(OutputStream outputStream) throws IOException{
        ByteBuffer buffer = ByteBuffer.allocate(1+16+1);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(getIntegerType());
        buffer.putDouble(mX);
        buffer.putDouble(mY);
        buffer.put((byte)(mButton.ordinal()+1));
        outputStream.write(buffer.array());
    }

    @Override
    public String toString() {
        String event = "MouseClickEvent["
                + "x="+mX+", y="+mY+", btn="+mButton.name()+"]";
        return event;
    }
}
