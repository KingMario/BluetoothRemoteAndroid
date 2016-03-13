package com.freeware.nikola.blueraspberrycontrol;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.async.Consumer;
import com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.async.Interruptible;
import com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto.ConnectionInitPacket;
import com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto.InboundAckPacket;
import com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto.InboundPacket;
import com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto.OutboundPacket;
import com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto.Packet;
import com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto.ScreenFramePacket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class DataTransferManager implements BlueberryRemote.OnOutboundPacketListener {

    private static final String TAG = "DataTransferManager";

    private BluetoothSocket mBluetoothSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    /**
     *  Called when a packet arrives from the input stream
     */
    public interface OnInboundPacketListener {
        void onInboundPacket(InboundPacket inboundPacket);
    }

    /**
     *  Called when some error ocurrs during read/write operation
     */
    public interface OnConnectionErrorListener {
        void onConnectionError(String message);
    }

    private OnInboundPacketListener mOnInboundPacketListener;
    private OnConnectionErrorListener onConnectionErrorListener;




    /**
     *  Read for inbound packets. On packet arrival the packet is
     *  sent to the listener to be handled. On connection error:
     *  1. the input stream is closed.
     *  2. on connection error handler (if any) is executed.
     */
    Interruptible mInboundHandler = new Interruptible() {

        @Override
        public void run() {
            while (!interrupted()) {
                try {
                    InboundPacket inboundPacket = readPacket();
                    if(mOnInboundPacketListener != null) {
                        mOnInboundPacketListener.onInboundPacket(inboundPacket);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "run: ", e);
                    try {
                        mInputStream.close();
                    } catch (IOException ioe) {
                        Log.e(TAG, "run: ", ioe);
                    }
                    if(onConnectionErrorListener != null) {
                        onConnectionErrorListener.onConnectionError("cannot read packet");
                    }
                    return;
                }
            }
        }

        private InboundPacket readPacket() throws IOException {
            byte typeByte[] = new byte[1];
            mInputStream.read(typeByte);
            InboundPacket packet = null;
            switch (Packet.fromByte(typeByte[0])) {
                case SCREE_FRAME:
                    packet = new ScreenFramePacket();
                    break;
                case ACK:
                    packet = new InboundAckPacket();
                    break;
                default:
                    throw new IllegalStateException("cannot create packet from "+Packet.fromByte(typeByte[0]));
            }
            packet.readFromStream(mInputStream);
            return packet;
        }

    };

    /**
     *  Writes outbound packets to the output stream.
     *  on OutboundPacket the PacketMethod writeToStream
     *  is used. On connection error:
     *  1. the output stream is closed.
     *  2. on connection error handler (if any) is executed.
     */
    Consumer<OutboundPacket> mOutboundHandler = new Consumer<OutboundPacket>() {
        @Override
        public void processItem(OutboundPacket item) {
            try {
                Log.d(TAG, "processItem: "+item);
                item.writeToStream(mOutputStream);
            } catch (IOException e) {
                Log.e(TAG, "processItem: ", e);
                try {
                    mOutputStream.close();
                } catch (IOException ioe) {
                    Log.e(TAG, "processItem: ", ioe);
                }
                if(onConnectionErrorListener != null) {
                    onConnectionErrorListener.onConnectionError("cannot write packet");
                }
                return;
            }
        }
    };

    private Thread mInboundHandlerThread;
    private Thread mOutboundHandlerThread;

    /**
     *  Close the connections and stops the threads
     *  associated with the DataTransferManager
     * @throws IOException  when the connection cannot be closed for some reason
     */

    private void closeConnection() throws IOException  {
        Log.d(TAG, "closeConnection: Entering closeConnection");
        try {
            if (mInboundHandlerThread != null && mInboundHandlerThread.isAlive()) {
                mInboundHandler.doInterrupt();
                Log.d(TAG, "closeConnection: joining mInboundHandlerThread");
                mInboundHandlerThread.join();
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "closeConnection: mInboundHandlerThread", e);
        }
        try {
            if (mOutboundHandlerThread != null && mOutboundHandlerThread.isAlive()) {
                mOutboundHandler.doInterrupt();
                Log.d(TAG, "closeConnection: joining mOutboundHandlerThread");
                mOutboundHandlerThread.join();
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "closeConnection: OutboundHandlerThread", e);
        }
        Log.d(TAG, "closeConnection: closing mInputStream");
        mInputStream.close();
        Log.d(TAG, "closeConnection: closing mOutputStream");
        mOutputStream.close();
        Log.d(TAG, "closeConnection: connection closed");
    }

    public void setOnConnectionErrorListener(OnConnectionErrorListener onConnectionErrorListener) {
        this.onConnectionErrorListener = onConnectionErrorListener;
    }

    public DataTransferManager(BluetoothSocket socket) {
        mBluetoothSocket = socket;
    }

    @Override
    public void onOutboundPacket(OutboundPacket outboundPacket) {
        Log.d(TAG, "onOutboundPacket: "+outboundPacket);
        mOutboundHandler.add(outboundPacket);
    }

    public void setOnInboundPacketListener(OnInboundPacketListener onInboundPacketListener) {
        mOnInboundPacketListener = onInboundPacketListener;
    }

    private void onConnectionInitiationCompleted() {
        mInboundHandlerThread = new Thread(mInboundHandler);
        mInboundHandlerThread.start();
        mOutboundHandlerThread = new Thread(mOutboundHandler);
        mOutboundHandlerThread.start();
        Log.d(TAG, "onConnectionInitiationCompleted:");
    }

    Runnable connectionInitiation = new Runnable() {
        @Override
        public void run() {
            try {
                mBluetoothSocket.connect();
                Log.d(TAG, "run: connected");
            } catch (IOException e) {
                if(onConnectionErrorListener != null) {
                    onConnectionErrorListener.onConnectionError("Cannot connect to remote host.");
                }
                return;
            }
            try {
                mInputStream = mBluetoothSocket.getInputStream();
                Log.d(TAG, "run: got input stream");
            } catch (IOException e) {
                if(onConnectionErrorListener != null) {
                    onConnectionErrorListener.onConnectionError("Cannot connect to remote host.");
                }
                try {
                    mBluetoothSocket.close();
                } catch (IOException ignored) {

                }
                return;
            }
            try {
                mOutputStream = mBluetoothSocket.getOutputStream();
                Log.d(TAG, "run: got output stream");
            } catch (IOException e) {
                try {
                    mInputStream.close();
                    mBluetoothSocket.close();
                } catch (IOException ignored) {

                }
                if(onConnectionErrorListener != null) {
                    onConnectionErrorListener.onConnectionError("Cannot connect to remote host.");
                }
                return;
            }
            try {
                initConnection();
                Log.d(TAG, "run: connection initiated");
            } catch (IOException e) {
                try {
                    mInputStream.close();
                    mOutputStream.close();
                    mBluetoothSocket.close();
                } catch (IOException ignored) {

                }
                if(onConnectionErrorListener != null) {
                    onConnectionErrorListener.onConnectionError("Cannot initiate connection.");
                }
                return;
            }
            onConnectionInitiationCompleted();
        }

        private void initConnection() throws IOException {

            ConnectionInitPacket connInit = new ConnectionInitPacket();
            connInit.writeToStream(mOutputStream);

            byte typeByte[] = new byte[1];
            mInputStream.read(typeByte);

            if(Packet.fromByte(typeByte[0]) != Packet.PacketType.ACK) {
                throw new IllegalStateException("received not expected "+
                        Packet.fromByte(typeByte[0]).toString());
            }
        }

    };

    public void startDataTransfer() {
        if(mBluetoothSocket == null) {
            throw new IllegalStateException("cannot establish connection");
        }
        Thread connectionInitiationThread = new Thread(connectionInitiation);
        connectionInitiationThread.start();
    }

    public void stopDataTransfer() throws IOException {
        closeConnection();
    }
}
