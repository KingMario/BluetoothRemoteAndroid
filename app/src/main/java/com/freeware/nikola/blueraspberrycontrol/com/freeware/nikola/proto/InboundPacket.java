package com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by nikola on 1/3/16.
 */
public abstract class InboundPacket extends Packet {

    public InboundPacket(PacketType type) {
        super(type);
    }

    public abstract void readFromStream(InputStream inputStream)
            throws IOException;
}
