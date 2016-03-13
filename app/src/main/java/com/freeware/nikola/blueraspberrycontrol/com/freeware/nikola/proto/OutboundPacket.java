package com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by nikola on 1/3/16.
 */
public abstract class OutboundPacket extends Packet {

    public OutboundPacket(PacketType type) {
        super(type);
    }

    public abstract void writeToStream(OutputStream outputStream)
            throws IOException;

}
