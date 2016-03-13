package com.freeware.nikola.blueraspberrycontrol;

import android.bluetooth.BluetoothSocket;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by nikola on 2/1/16.
 */
public class VideoProxy extends Thread {

    private static final String TAG = "VideoProxy";
    public static String SOCKET_ADDRESS = "bluetooth2localsocketproxy";

    private LocalServerSocket mLocalServerSocket;
    private LocalSocket mLocalSocket;
    private OutputStream mLocalOutputStream;

    private BluetoothSocket mRemoteServerConnection;
    private InputStream mBluetoothSocketInputStream;
    private static final byte BUFFER[] = new byte[4096];

    public VideoProxy(BluetoothSocket remoteServerConnection) throws IOException {
        mRemoteServerConnection = remoteServerConnection;
        mLocalServerSocket = new LocalServerSocket(SOCKET_ADDRESS);
    }

    private boolean softStopped() {
        return false;
    }

    @Override
    public void run() {
        try {
            mLocalSocket = mLocalServerSocket.accept();
            mLocalOutputStream = mLocalSocket.getOutputStream();
            Log.d(TAG, "run: accepted local conn");
            mRemoteServerConnection.connect();
            mBluetoothSocketInputStream = mRemoteServerConnection.getInputStream();
            Log.d(TAG, "run: connected to remote server");

            while(!softStopped()) {
                int read = 0;
                while((read = mBluetoothSocketInputStream.read(BUFFER)) > 0) {
                    if(softStopped()) {
                        closeConn();
                        break;
                    }
                    mLocalOutputStream.write(BUFFER, 0, read);
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "run: ", e);
        } finally {
            closeConn();
        }
    }

    private void closeConn() {
        try {
            mRemoteServerConnection.close();
        } catch (IOException e) {
            Log.d(TAG, "closeConn: ", e);
        }
        try {
            mLocalSocket.close();
        } catch (IOException e) {
            Log.d(TAG, "closeConn: ", e);
        }
    }
}
