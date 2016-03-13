package com.freeware.nikola.blueraspberrycontrol;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by nikola on 2/28/16.
 */
public class EnterTextDialog extends DialogFragment{

    private final static String TAG = "EnterTextDialog";

    public interface OnKeyboardEventGeneratedListener {
        void onKeyboardEventGenerated(String keyboardEvent);
    }

    private Dialog thisDialog;
    private KeyboardEmuView mKeyboardEmuView;

    private OnKeyboardEventGeneratedListener onKeyboardEventGeneratedListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.keyboard_emulation_view, null);
        builder.setView(dialogView);
        mKeyboardEmuView = (KeyboardEmuView)dialogView.findViewById(R.id.keyboard_emulation);
        mKeyboardEmuView.setOnKeyPressedListener(new KeyboardEmuView.OnKeyPressedListener() {
            @Override
            public void onKeyPressed(String key) {
                if(onKeyboardEventGeneratedListener != null) {
                    onKeyboardEventGeneratedListener.onKeyboardEventGenerated(key);
                }
            }
        });

        thisDialog= builder.create();
        return thisDialog;
    }

    public void setOnKeyboardEventGeneratedListener(OnKeyboardEventGeneratedListener callback) {
        onKeyboardEventGeneratedListener = callback;
    }

}
