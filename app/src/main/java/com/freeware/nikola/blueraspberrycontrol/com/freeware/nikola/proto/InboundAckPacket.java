package com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by nikola on 2/27/16.
 */
public class InboundAckPacket extends InboundPacket {

    public InboundAckPacket() {
        super(PacketType.ACK);
    }

    @Override
    public void readFromStream(InputStream inputStream) throws IOException {

    }
}
