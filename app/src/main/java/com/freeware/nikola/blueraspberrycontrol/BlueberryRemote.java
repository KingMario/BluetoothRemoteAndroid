package com.freeware.nikola.blueraspberrycontrol;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto.InboundPacket;
import com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto.Mapper;
import com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto.OutboundPacket;
import com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto.ScreenFramePacket;
import com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto.SingleKeyboardEvent;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class BlueberryRemote extends AppCompatActivity {

    private static final String TAG = "BlueberryRemote";

    private RemoteView mRemoteView;

    private static final int REQUEST_ENABLE_BLUETOOTH = 0xB10E;
    private static final int REQUEST_PAIR_WITH_DEVICE = 0x9A1D;

    private BluetoothAdapter mAdapter;
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;

    private boolean autoInitiate = false;
    private DataTransferManager mDataTransferManager;

    public interface OnOutboundPacketListener {
        void onOutboundPacket(OutboundPacket outboundPacket);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blueberry_remote_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mRemoteView = (RemoteView) findViewById(R.id.remote_surface_view);

        if(autoInitiate) {
            enableConnection();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: begin");
        super.onStop();

        if(mDataTransferManager != null) {
            try {
                Log.d(TAG, "onStop: mDataTransferManager.stopDataTransfer()");
                mDataTransferManager.stopDataTransfer();
                mDataTransferManager = null;
            } catch (IOException e) {
                Log.e(TAG, "onStop: ", e);
            }
        }
        if(mSocket != null) {
            try {
                mSocket.close();
                mSocket = null;
                Log.d(TAG, "onStop: mSocket.close()");
            } catch (IOException e) {
                Log.e(TAG, "onStop: ", e);
            }
        }
        mDevice = null;
        mAdapter = null;
        Log.d(TAG, "onStop: end");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_pause_stream:

                return true;
            case R.id.menu_play_stream: {
                enableConnection();
                return true;
            }
            case R.id.menu_stop_stream:
                stopDataTransfer();
                autoInitiate = false;
                return true;
            case R.id.menu_type_text:
                EnterTextDialog dialogFragment = new EnterTextDialog();
                dialogFragment.setOnKeyboardEventGeneratedListener(new EnterTextDialog.OnKeyboardEventGeneratedListener() {
                    @Override
                    public void onKeyboardEventGenerated(String keyboardEvent) {
                        if(mDataTransferManager != null) {
                            int keyEvent = Mapper.mapString(keyboardEvent);
                            if(keyEvent != -1) {
                                SingleKeyboardEvent ske = new SingleKeyboardEvent(keyEvent);
                                Log.d(TAG, "onKeyboardEventGenerated: "+ske);
                                mDataTransferManager.onOutboundPacket(ske);
                            }
                        }
                    }
                });
                dialogFragment.show(getSupportFragmentManager(), "EnterTextFragment");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void enableConnection() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mAdapter == null) {
            showAdpaterNotAvailable();
            return;
        }
        if (!mAdapter.isEnabled()) {
            promptEnableAdapter();
            return;
        }
        bindToDevice();
    }

    private void bindToDevice() {
        Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();
        for (BluetoothDevice sDevice : pairedDevices) {
            mDevice = sDevice;
            break;
        }
        Log.d(TAG, "bindToDevice: " + mDevice);
        if (mDevice == null) {
            Log.d(TAG, "bindToDevice: prompt pair with device");
            promptPairWithDevice();
            Log.d(TAG, "bindToDevice: return");
            return;
        }
        connectToServer();
    }

    private void connectToServer() {
        try {
            Log.d("Paired with", mDevice.getName());
            mSocket = mDevice.createRfcommSocketToServiceRecord(new UUID(0x0000000000000000L, 0x000000000000abcdL));
            Log.d(TAG, "connectToServer: Connected");
        } catch (IOException e) {
            Log.e("Blueberry Remote", "onOptionsItemSelected: Unable to connect to remote server", e);
            notifyConnectionEstablishmentFailed();
            return;
        }
        startDataTransfer();
    }

    private void startDataTransfer() {
        mDataTransferManager = new DataTransferManager(mSocket);
        mDataTransferManager.setOnConnectionErrorListener(new DataTransferManager.OnConnectionErrorListener() {
            @Override
            public void onConnectionError(String message) {
                final String errorMessage = message;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BlueberryRemote.this);
                        alertDialog.setMessage("Error occurred during packet processing: " + errorMessage)
                                .setNeutralButton("Ok", null);
                        alertDialog.create().show();
                    }
                });
            }
        });
        // make shure that only data transfer manager knows about the
        // Packet class. which means that
        mDataTransferManager.setOnInboundPacketListener(new DataTransferManager.OnInboundPacketListener() {
            @Override
            public void onInboundPacket(InboundPacket inboundPacket) {
                if (inboundPacket instanceof ScreenFramePacket) {
                    ScreenFramePacket scrFrame = (ScreenFramePacket) inboundPacket;
                    mRemoteView.postImageToDisplay(scrFrame.getBitmap());
                }
            }
        });
        mRemoteView.setOnOutboundEventListener(new RemoteView.OnOutboundEventListener() {
            @Override
            public void onOutboundEvent(OutboundPacket packet) {
                if (mDataTransferManager != null) {
                    mDataTransferManager.onOutboundPacket(packet);
                }
            }
        });
        mDataTransferManager.startDataTransfer();
        autoInitiate = true;
    }

    private void stopDataTransfer() {
        if(mDataTransferManager == null)
            return;
        try {
            mDataTransferManager.stopDataTransfer();
        } catch (IOException e) {
            Log.e(TAG, "stopDataTransfer: ", e);
        }
        Log.d("DataTransfer Stopped", "stopDataTransfer: Thread stopped");
    }

    private void notifyConnectionEstablishmentFailed() {
        throw new UnsupportedOperationException("");
    }

    private void promptPairWithDevice() {
        Log.d(TAG, "promptPairWithDevice: begin");
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("The device is not paired with the server. Please pair and try again.")
                .setNeutralButton("Ok", null);
        alertDialog.create().show();
        Log.d(TAG, "promptPairWithDevice: end");
    }

    private void promptEnableAdapter() {
        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == RESULT_OK) {
                    enableConnection();
                } else if (resultCode == RESULT_CANCELED) {
                    AlertDialog.Builder alertCannotUse = new AlertDialog.Builder(this);
                    alertCannotUse.setMessage("The app cannot be used without bluetooth.")
                            .setNeutralButton("Ok", null);
                    alertCannotUse.create().show();
                }
                break;
            case REQUEST_PAIR_WITH_DEVICE: {
                if (resultCode == RESULT_OK) {

                }
            }
        }
    }

    private void showAdpaterNotAvailable() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Bluetooth is not supported on this device.")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        dialogBuilder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blueberry_remote_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
