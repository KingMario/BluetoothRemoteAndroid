package com.freeware.nikola.blueraspberrycontrol;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;

/**
 * Created by nikola on 3/3/16.
 */
public class KeyboardButtonView extends Button {

    public static final String TAG = "KeyboardButtonView";

    private String mTextUpperLeft;
    private String mTextUpperRight;
    private String mTextBottomLeft;
    private String mTextBottomRight;
    private String mTextCenter;

    private int mImageUpperLeft;

    private float mWidth;
    private float mHeight;

    private float mOffset;

    private PointF mUpperLeftDrawPosition;
    private PointF mUpperRightDrawPosition;
    private PointF mBottomLeftDrawPosition;
    private PointF mBottomRightDrawPosition;
    private PointF mCenterDrawPossiotion;

    private Paint mTextPaint;
    private Paint mBackgroundPaint;

    public KeyboardButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray attributes = context.getTheme().
                obtainStyledAttributes(attrs, R.styleable.KeyboardButtonView, 0, 0);

        try {
            mTextUpperLeft = attributes.getString(R.styleable.KeyboardButtonView_text_upper_left);
            Log.d(TAG, "KeyboardButtonView: "+mTextUpperLeft);
            mTextUpperRight = attributes.getString(R.styleable.KeyboardButtonView_text_upper_right);
            Log.d(TAG, "KeyboardButtonView: "+mTextUpperRight);
            mTextBottomLeft = attributes.getString(R.styleable.KeyboardButtonView_text_bottom_left);
            Log.d(TAG, "KeyboardButtonView: "+mTextBottomLeft);
            mTextBottomRight = attributes.getString(R.styleable.KeyboardButtonView_text_bottom_right);
            Log.d(TAG, "KeyboardButtonView: "+mTextBottomRight);
            mTextCenter = attributes.getString(R.styleable.KeyboardButtonView_text_center);
            Log.d(TAG, "KeyboardButtonView: "+mTextCenter);
        } finally {
            attributes.recycle();
        }

        initPaints();

    }

    private void initPaints() {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTypeface(Typeface.MONOSPACE);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setARGB(255, 255, 255, 255);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingTop() + getPaddingBottom());

        mWidth = w;
        mHeight = h;

        float ww = (float) w - xpad;
        float hh = (float) h - ypad;

        mOffset = (ww*0.1f + hh*0.1f)/2.0f;

        mTextPaint.setTextSize(hh/2-(hh*0.1f));
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mUpperLeftDrawPosition = new PointF(mOffset + getPaddingLeft(), h/2-getPaddingTop()+mOffset);
        mUpperRightDrawPosition = new PointF(mOffset + getPaddingLeft()+w/2, h/2-getPaddingTop()+mOffset);
        mBottomLeftDrawPosition = new PointF(mOffset + getPaddingLeft(), h-getPaddingBottom()-mOffset);
        mBottomRightDrawPosition = new PointF(getPaddingLeft()+w/2, h-getPaddingBottom()-mOffset);

        mCenterDrawPossiotion = new PointF(w/2.0f - mTextPaint.getTextSize()/2, h/2 + getPaddingTop());
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if(mTextCenter != null) {
            canvas.drawText(mTextCenter,
                    mCenterDrawPossiotion.x,
                    mCenterDrawPossiotion.y,
                    mTextPaint);
        }
        if(mTextUpperLeft != null) {
            canvas.drawText(mTextUpperLeft,
                    mUpperLeftDrawPosition.x,
                    mUpperLeftDrawPosition.y,
                    mTextPaint);
        }
        if(mTextUpperRight != null) {
            canvas.drawText(mTextUpperRight,
                    mUpperRightDrawPosition.x,
                    mUpperRightDrawPosition.y,
                    mTextPaint);
        }
        if(mTextBottomLeft != null) {
            canvas.drawText(mTextBottomLeft,
                    mBottomLeftDrawPosition.x,
                    mBottomLeftDrawPosition.y,
                    mTextPaint);
        }
        if(mTextBottomRight != null) {
            canvas.drawText(mTextBottomRight,
                    mBottomRightDrawPosition.x,
                    mBottomRightDrawPosition.y,
                    mTextPaint);
        }
    }

}
