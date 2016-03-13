package com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by nikola on 3/12/16.
 */
public class SingleKeyboardEvent extends KeyboardEvent {

    private int mKeyCode;

    public SingleKeyboardEvent(int keyCode) {
        super(KeyboardEventType.SINGLE_KEY);
        mKeyCode = keyCode;
    }

    @Override
    public void writeToStream(OutputStream outputStream) throws IOException {
        super.writeToStream(outputStream);
        outputStream.write(mKeyCode);
    }
}
