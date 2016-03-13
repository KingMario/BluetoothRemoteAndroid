package com.freeware.nikola.blueraspberrycontrol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto.MouseClickEvent;
import com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto.OutboundPacket;

/**
 * Created by nikola on 1/2/16.
 */
public class RemoteView extends SurfaceView implements SurfaceHolder.Callback {

    public static final String TAG = "RemoteView";
    private OnOutboundEventListener mOnOutboundEventListener;
    private Context mContext;

    private class DrawThread extends Thread {

        public static final String TAG = "DrawThread";

        public static final int SLEEP_INTERVAL = 20;

        private SurfaceHolder mSurfaceHolder;

        private boolean mRun;
        private final Object mRunLock = new Object();

        private int mWidth;
        private int mHeight;

        private float primaryScale = 1.0f;

        private float scaleX = 1.0f;
        private float scaleY = 1.0f;
        private float translateX = 0.0f;
        private float translateY = 0.0f;

        Matrix transform = new Matrix();

        private Bitmap mImageToDisplay;
        private RectF scaledDim = new RectF(0, 0, 0, 0);

        private Paint mBackgroundPaint;

        public DrawThread(SurfaceHolder holder, Context context) {
            mSurfaceHolder = holder;
            mContext = context;
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setStyle(Paint.Style.FILL);
            mBackgroundPaint.setARGB(255, 255, 255, 255);
        }

        private void doDraw(Canvas canvas) {
            if(mImageToDisplay != null) {
                float realScaleX = scaleX*primaryScale;
                float realScaleY = scaleY*primaryScale;
                transform.setScale(realScaleX, realScaleY);
                transform.postTranslate(-translateX, -translateY);
                canvas.drawBitmap(mImageToDisplay, transform, null);
            }
        }

        @Override
        public void run() {
            while (mRun) {
                Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {

                        synchronized (mRunLock) {

                            if(mRun) {
                                doDraw(c);
                            }

                            try {
                                Thread.sleep(SLEEP_INTERVAL);
                            } catch (InterruptedException e) {
                                Log.e(TAG, "run: ", e);
                            }

                        }
                    }
                } finally {
                    if(c!=null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }

        public void setImageToDisplay(Bitmap image) {
            synchronized (mSurfaceHolder) {
                mImageToDisplay = image;
                if(mWidth > mHeight) {
                    scaleX = (float)mWidth / (float)image.getWidth();
                    scaleY = scaleX;
                } else {
                    scaleY = (float)mHeight / (float)image.getHeight();
                    scaleX = scaleY;
                }
                scaledDim.set(0,0, scaleX*image.getWidth(), scaleY*image.getHeight());
            }
        }

        public PointF getScale() {
            synchronized (mSurfaceHolder) {
                return new PointF(scaleX, scaleY);
            }
        }

        public PointF getTranslate() {
            synchronized (mSurfaceHolder) {
                return new PointF(translateX, translateY);
            }
        }

        public Matrix getTransform() {
            synchronized (mSurfaceHolder) {
                Matrix invertTransform = new Matrix();
                transform.invert(invertTransform);
                return invertTransform;
            }
        }

        public void setRunning(boolean run) {
            synchronized (mRunLock) {
                mRun = run;
            }
        }

        public boolean isRunning() {
            synchronized (mRunLock) {
                return mRun;
            }
        }

        public void setSize(int width, int height) {
            synchronized (mSurfaceHolder) {
                mWidth = width;
                mHeight = height;
            }
        }

        public void setScale(float scale) {
            synchronized (mSurfaceHolder) {
                if (1.0 > scale || scale > 5.0) return;
                primaryScale = scale;
            }
        }

        public void setTranslate(float x, float y) {
            synchronized (mSurfaceHolder) {
                translateX += x;
                translateY += y;
                translateX = Math.max(0, translateX);
                translateY = Math.max(0, translateY);
                translateX = Math.min(translateX, scaledDim.right - mWidth);
                translateY = Math.min(translateY, scaledDim.bottom - mHeight);
            }

        }
    }

    private GestureDetector mGestureDetector;

    private DrawThread mDrawThread;

    public RemoteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        mContext = context;
        setFocusable(true);
        mGestureDetector = new GestureDetector(context, simpleOnGestureListener);
        mGestureDetector.setOnDoubleTapListener(onDoubleTapListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    public void postImageToDisplay(Bitmap bmp) {
        if(mDrawThread != null) {
            mDrawThread.setImageToDisplay(bmp);
        }
    }

    public void setScale(float scale) {
        if(mDrawThread != null) {
            mDrawThread.setScale(scale);
        }
    }

    public void setTranslate(float x, float y) {
        if(mDrawThread != null) {
            mDrawThread.setTranslate(x, y);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated: begin");
        mDrawThread = new DrawThread(holder, mContext);
        mDrawThread.setRunning(true);
        mDrawThread.start();
        Log.d(TAG, "surfaceCreated: end");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged: begin");
        mDrawThread.setSize(width, height);
        Log.d(TAG, "surfaceChanged: end");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed: begin");
        doShutdown();
        Log.d(TAG, "surfaceDestroyed: end");
    }

    public void doShutdown() {
        Log.d(TAG, "doShutdown: begin");
        if(mDrawThread == null) {
            Log.d(TAG, "doShutdown: previously stopped end");
            return;
        }
        mDrawThread.setRunning(false);
        boolean retry = true;
        while (retry) {
            try {
                Log.d(TAG, "doShutdown: joining mDrawThread");
                mDrawThread.join();
                retry = false;
            } catch (InterruptedException ignored) {
                Log.e(TAG, "surfaceDestroyed: ", ignored);
            }
        }
        mDrawThread = null;
        Log.d(TAG, "doShutdown: end");
    }

    interface OnOutboundEventListener {
        void onOutboundEvent(OutboundPacket packet);
    }

    public void  setOnOutboundEventListener(OnOutboundEventListener onOutboundEventListener) {
        mOnOutboundEventListener = onOutboundEventListener;
    }

    GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(mDrawThread != null) {
                mDrawThread.setTranslate(distanceX, distanceY);
            }
            return false;
        }
    };

    GestureDetector.OnDoubleTapListener onDoubleTapListener = new GestureDetector.OnDoubleTapListener() {

        private float[] getCoordinatesTransformed(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();
            float vec[] = {
                    e.getX(), e.getY()
            };
            Matrix transform = mDrawThread.getTransform();
            transform.mapPoints(vec);
            return vec;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if(mDrawThread == null) {
                return false;
            }
            float vec[] = getCoordinatesTransformed(e);
            MouseClickEvent mouseClickEvent = new MouseClickEvent(vec[0], vec[1], MouseClickEvent.MouseButton.LEFT_BUTTON);
            if(mOnOutboundEventListener != null) {
                mOnOutboundEventListener.onOutboundEvent(mouseClickEvent);
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if(mDrawThread == null) {
                return true;
            }
            float vec[] = getCoordinatesTransformed(e);
            MouseClickEvent mouseClickEvent = new MouseClickEvent(vec[0], vec[1], MouseClickEvent.MouseButton.DOUBLE_LEFT_BUTTON);
            if(mOnOutboundEventListener != null) {
                mOnOutboundEventListener.onOutboundEvent(mouseClickEvent);
            }
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {

            return true;
        }
    };

}
