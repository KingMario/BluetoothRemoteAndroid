package com.freeware.nikola.blueraspberrycontrol;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by nikola on 1/2/16.
 */
public class RemoteViewFragment extends Fragment {

    private OnClickRemoteViewListener mClickListener;
    private RemoteView mRemoteView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public interface OnClickRemoteViewListener {
        public void clicked(float x, float y);
    }

    public void setOnClickRemoteViewListener(OnClickRemoteViewListener listener) {
        this.mClickListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.remote_view_layout, container, false);

        mRemoteView = (RemoteView)view.findViewById(R.id.remote_view_control);

        mRemoteView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mClickListener != null) {
                    mClickListener.clicked(event.getX(), event.getY());
                }
                return false;
            }
        });

        return view;
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    public void updateImage(Bitmap image) {
        if(mRemoteView != null) {
            mRemoteView.postImageToDisplay(image);
        }
    }

}
