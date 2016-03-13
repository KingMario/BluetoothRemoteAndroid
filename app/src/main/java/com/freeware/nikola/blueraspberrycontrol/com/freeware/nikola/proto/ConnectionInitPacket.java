package com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by nikola on 2/27/16.
 */
public class ConnectionInitPacket extends OutboundPacket {

    public ConnectionInitPacket() {
        super(PacketType.CONNECTION_INIT);
    }

    @Override
    public void writeToStream(OutputStream outputStream) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(getIntegerType());
        outputStream.write(buffer.array());
    }
}
