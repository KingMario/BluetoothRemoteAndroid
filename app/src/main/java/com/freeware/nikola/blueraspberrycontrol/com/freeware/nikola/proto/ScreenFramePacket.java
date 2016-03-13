package com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by nikola on 1/3/16.
 */
public class ScreenFramePacket extends InboundPacket {

    private Bitmap bitmap;

    public ScreenFramePacket() {
        super(PacketType.SCREE_FRAME);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public void readFromStream(InputStream inputStream) throws IOException {
        bitmap = BitmapFactory.decodeStream(inputStream);
        if(bitmap == null) {
            throw new IOException("Unable to read screen frame.");
        }
    }
}
