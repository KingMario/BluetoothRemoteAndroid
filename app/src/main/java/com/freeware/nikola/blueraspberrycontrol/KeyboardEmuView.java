package com.freeware.nikola.blueraspberrycontrol;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.async.Consumer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KeyboardEmuView extends View {

    private static final String TAG  = "KeyboardEmuView";
    public static final String TAB_SYMBOL = "\u21C4";
    public static final String BACKSPACE_SYMBOL = "\u232B";
    public static final String CAPSLOCK_SYMBOL = "\u21EA";
    public static final String ENTER_SYMBOL = "\u23CE";
    public static final String SHIFT_SYMBOL = "\u21E7";
    public static final String CTRL_SYMBOL = "Ctr";
    public static final String ALT_SYMBOL = "Alt";
    public static final String SPACE_SYMBOL = " ";
    private float mMarginWidth;
    private float mMarginHeight;

    public interface OnKeyPressedListener {
        void onKeyPressed(String key);
    }

    private OnKeyPressedListener mOnKeyPressedListener;

    public void setOnKeyPressedListener(OnKeyPressedListener onKeyPressedListener) {
        mOnKeyPressedListener = onKeyPressedListener;
    }

    private static final int MAX_COLUMNS = 12;
    private static final int MAX_ROWS = 5;

    private float mWidth;
    private float mHeight;

    private boolean mShiftHolden;

    private RectF mButtonRect;
    private float mButtonMargin;

    private Paint mTextDrawPaint;
    private Paint mBackgroundPaint;
    private Paint mButtonBackgroundPaint;

    private GestureDetector mGestureDetector;
    private Paint mButtonPressedBackgroundPaint;
    private Paint mButtonPressedTextDrawPaint;

    class ButtonShape {

        Rect location;

        private PointF center;

        String character;
        String alterCharacter;

        ButtonShape(Rect location, String character) {
            this(location, character, null);
        }

        ButtonShape(Rect location, String character, String alterCharacter) {

            this.location = location;
            this.center = new PointF(location.centerX(), location.centerY());

            this.character = character;
            this.alterCharacter = alterCharacter;
        }

    }

    private List<List<ButtonShape>> buttonRows;
    private Set<ButtonShape> pressed;

    private Thread viewUpdateThread;

    public KeyboardEmuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mShiftHolden = false;

        mGestureDetector = new GestureDetector(context, simpleOnGestureListener);
        pressed = new HashSet<>();

        viewUpdateThread = new Thread(viewUpdater);

        initPaint();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            // move this to onTouch to handle multiple touches

            for(int i = 0 ; i  < buttonRows.size() ; i++) {
                List<ButtonShape> currentRow = buttonRows.get(i);
                for(int j = 0 ; j < currentRow.size() ; j++) {

                    ButtonShape btnShape = currentRow.get(j);
                    Rect loc = btnShape.location;

                    if(loc.contains((int)e.getX(), (int)e.getY())) {

                        if(btnShape.character != null && btnShape.character.equals("\u21E7")) {
                            mShiftHolden = !mShiftHolden;
                        }

                        pressed.add(btnShape);
                        if(mOnKeyPressedListener!=null) {

                            if(!mShiftHolden) {
                                Log.d(TAG, "onDown: "+btnShape.character);
                                mOnKeyPressedListener.onKeyPressed(btnShape.character);
                            } else if (btnShape.alterCharacter != null) {
                                mOnKeyPressedListener.onKeyPressed(btnShape.alterCharacter);
                            }

                        }

                        invalidate();

                        break;
                    }
                }
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            return true;
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        viewUpdateThread.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        boolean retry = true;
        int retryCount = 10;
        viewUpdater.doInterrupt();
        while (retry) {
            try {
                viewUpdateThread.join();
                retry = false;
            } catch (InterruptedException e) {
                Log.e(TAG, "onDetachedFromWindow: ", e);
            }
            if(retryCount-- == 0) {
                viewUpdateThread.interrupt(); // try hard stop
                break;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int size[] = calculateSize(width, height);

        setMeasuredDimension(size[0], size[1]);
    }

    protected int[] calculateSize(int width, int height) {

        double xpad = (float) (getPaddingLeft() + getPaddingRight());
        double ypad = (float) (getPaddingTop() + getPaddingBottom());
        double btnWidthPadded = (width - xpad) / MAX_COLUMNS;
        double btnHeightPadded = (height - ypad) / MAX_ROWS;

        double minSizePadded = btnWidthPadded < btnHeightPadded ? btnWidthPadded : btnHeightPadded;

        int size[] = {
                (int)Math.round(minSizePadded*MAX_COLUMNS),
                (int)Math.round(minSizePadded*MAX_ROWS)
        };

        return size;
    }

    @Override
    protected final void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged: "+w+" "+h+" "+oldw+" "+oldh);

        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingTop() + getPaddingBottom());

        mWidth = w;
        mHeight = h;

        float mWidthPadded = (mWidth - xpad) / MAX_COLUMNS;
        float mHeightPadded = (mHeight - ypad) / MAX_ROWS;

        float mMinSizePadded = mWidthPadded < mHeightPadded ? (mWidth - xpad) : (mHeight - ypad);

        mButtonMargin = 0.005f* mMinSizePadded;

        mMarginWidth = mButtonMargin*(MAX_COLUMNS+1);
        mMarginHeight = mButtonMargin*(MAX_ROWS+1);

        float buttonHeight = (mMinSizePadded-mMarginHeight)/MAX_ROWS;
        float buttonWidth =  (mMinSizePadded-mMarginWidth)/MAX_COLUMNS;

        float minButtonSize = buttonWidth < buttonHeight ? buttonWidth : buttonHeight;
        float mButtonPadding = minButtonSize * 0.10f;

        mButtonRect = new RectF(0, 0, minButtonSize, minButtonSize);

        mTextDrawPaint.setTextSize((minButtonSize*0.75f - mButtonPadding *2));

        mButtonPressedTextDrawPaint.setTextSize(mTextDrawPaint.getTextSize()*1.5f);

        generateButtons();

    }

    private void generateButtons() {

        buttonRows = new ArrayList<>();

        float padLeft = getPaddingLeft();
        float padTop = getPaddingTop();

        List<ButtonShape> numPadRow = createNumPadRow(padLeft, padTop+mButtonMargin);
        buttonRows.add(numPadRow);

        padTop += mButtonRect.height() + mButtonMargin*2;

        float currentStartX[] = {padLeft};
        float currentStartY[] = {padTop};

        Rect first = new Rect((int)(currentStartX[0]+=mButtonMargin),
                (int)(currentStartY[0]),
                (int)(currentStartX[0]+=mButtonRect.width()*1.f),
                (int)(currentStartY[0]+mButtonRect.height()));

        List<ButtonShape> tabStartRow = createKeyboardRow(
                currentStartX, currentStartY,
                new ButtonShape(first, TAB_SYMBOL, TAB_SYMBOL),
                new Character[]{'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'},
                null,
                new ButtonShape(new Rect(), BACKSPACE_SYMBOL, BACKSPACE_SYMBOL), 1.0f);

        buttonRows.add(tabStartRow);

        padTop += mButtonRect.height() + mButtonMargin;

        currentStartX[0] = padLeft;
        currentStartY[0] = padTop;

        first = new Rect((int)(currentStartX[0]+=mButtonMargin),
                (int)(currentStartY[0]),
                (int)(currentStartX[0]+=mButtonRect.width()*1.f),
                (int)(currentStartY[0]+mButtonRect.height()));

        List<ButtonShape> capsStartRow = createKeyboardRow(
                currentStartX, currentStartY,
                new ButtonShape(first, CAPSLOCK_SYMBOL, CAPSLOCK_SYMBOL),
                new Character[]{'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'},
                null,
                new ButtonShape(new Rect(), ENTER_SYMBOL, ENTER_SYMBOL), 2.f);

        buttonRows.add(capsStartRow);

        padTop += mButtonRect.height() + mButtonMargin;
        currentStartX[0] = padLeft;
        currentStartY[0] = padTop;

        first = new Rect((int)(currentStartX[0]+=mButtonMargin),
                (int)currentStartY[0],
                (int)(currentStartX[0]+=mButtonRect.width()*1.f),
                (int)(currentStartY[0]+mButtonRect.height()));

        List<ButtonShape> shiftStartRow = createKeyboardRow(
                currentStartX, currentStartY,
                new ButtonShape(first, SHIFT_SYMBOL, SHIFT_SYMBOL),
                new Character[]{'Z', 'X', 'C', 'V', 'B', 'N', 'M', '[', ']', ',', '.'},
                new Character[]{null, null, null, null, null, null, null, '{', '}', '<', '>'},
                null, 0.f);

        buttonRows.add(shiftStartRow);

        padTop += mButtonRect.height() + mButtonMargin;

        List<ButtonShape> bottomLine = new ArrayList<>();

        currentStartX[0] = padLeft;
        currentStartY[0] = padTop;

        Rect ctrlPos = new Rect((int)(currentStartX[0]+=mButtonMargin),
                (int)(currentStartY[0]),
                (int)(currentStartX[0]+=mButtonRect.width()*1.5f + 0.5f*mButtonMargin),
                (int)(currentStartY[0]+mButtonRect.height()));

        bottomLine.add(new ButtonShape(ctrlPos, CTRL_SYMBOL, CTRL_SYMBOL));

        Rect altPos = new Rect((int)(currentStartX[0]+=mButtonMargin),
                (int)currentStartY[0],
                (int)(currentStartX[0]+=mButtonRect.width()*1.5f + 0.5f*mButtonMargin),
                (int)(currentStartY[0]+mButtonRect.height()));

        bottomLine.add(new ButtonShape(altPos, ALT_SYMBOL, ALT_SYMBOL));

        Rect spacePos = new Rect((int)(currentStartX[0]+=mButtonMargin),
                (int)(currentStartY[0]),
                (int)(currentStartX[0]+=Math.round(mButtonRect.width()*5f+mButtonMargin*4f)),
                (int)(currentStartY[0]+mButtonRect.height()));

        bottomLine.add(new ButtonShape(spacePos, SPACE_SYMBOL, SPACE_SYMBOL));

        List<ButtonShape> remaining = createKeyboardRow(
                    currentStartX, currentStartY, null,
                    new Character[]{';', '\'', '\\', '/'},
                    new Character[]{':', '"', '|', null },
                    null, 0.f);

        bottomLine.addAll(remaining);

        buttonRows.add(bottomLine);

    }

    @NonNull
    private List<ButtonShape> createKeyboardRow(float padLeft[], float padTop[], ButtonShape addToFront,
                                                Character rowSymbolsPrimary[], Character rowSymbolsSecondary[],
                                                ButtonShape addToEnd, float scaleLast) {

        List<ButtonShape> currentRow = new ArrayList<>();

        float currentPosX = padLeft[0];
        float currentPosY = padTop[0];

        Rect currentLocation;

        if(addToFront != null) {
            currentRow.add(addToFront);
        }

        for(int i =  0 ; i < rowSymbolsPrimary.length ; i++) {

            currentLocation = new Rect((int)(currentPosX+=mButtonMargin),
                    (int)currentPosY,
                    (int) (currentPosX+=mButtonRect.width()),
                    (int)(currentPosY+mButtonRect.height()));

            if(rowSymbolsSecondary != null && rowSymbolsSecondary.length > i-1 && rowSymbolsSecondary[i] != null) {
                currentRow.add(new ButtonShape(currentLocation, rowSymbolsPrimary[i] + "", rowSymbolsSecondary[i] + ""));
            } else {
                currentRow.add(new ButtonShape(currentLocation, rowSymbolsPrimary[i] + ""));
            }

        }

        if(addToEnd != null) {
            addToEnd.location.left = (int)(currentPosX+=mButtonMargin);
            addToEnd.location.top = (int)currentPosY;
            addToEnd.location.right = (int)(currentPosX+=mButtonRect.width()*scaleLast+(mButtonMargin*(scaleLast-1)));
            addToEnd.location.bottom = (int)(currentPosY+mButtonRect.height());
            currentRow.add(addToEnd);
        }

        padLeft[0]=currentPosX;
        padTop[0]=currentPosY;

        return currentRow;
    }

    @NonNull
    private List<ButtonShape> createNumPadRow(float padLeft, float padTop) {
        List<ButtonShape> numPadRow = new ArrayList<>();

        float currentPosX = padLeft;
        float currentPosY = padTop;

        for(int i = 1; i <= 12 ; i++) {

            Rect btnLocation = new Rect(
                    (int)(currentPosX+=mButtonMargin),
                    (int) currentPosY,
                    (int)(currentPosX+=mButtonRect.width()),
                    (int)(currentPosY+mButtonRect.height()));

            Character character = null;
            switch (i) {
                case 10: character = '0'; break;
                case 11: character = '-'; break;
                case 12: character = '='; break;
                default: character = (char)('0'+i);
            }
            Character alter = null;
            switch (i) {
                case 1: alter = '!'; break;
                case 2: alter = '@'; break;
                case 3: alter = '#'; break;
                case 4: alter = '$'; break;
                case 5: alter = '%'; break;
                case 6: alter = '^'; break;
                case 7: alter = '&'; break;
                case 8: alter = '*'; break;
                case 9: alter = '('; break;
                case 10: alter = ')'; break;
                case 11: alter = '_'; break;
                case 12: alter = '+'; break;
            }
            numPadRow.add(new ButtonShape(btnLocation, character+"", alter+""));
        }

        return numPadRow;
    }

    private void initPaint() {
        mTextDrawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextDrawPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC));
        mTextDrawPaint.setARGB(255, 0, 26, 26);
        mTextDrawPaint.setMaskFilter(new BlurMaskFilter(5, BlurMaskFilter.Blur.INNER));

        mButtonPressedTextDrawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mButtonPressedTextDrawPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC));
        mButtonPressedTextDrawPaint.setARGB(255, 0, 13, 13);
        mButtonPressedTextDrawPaint.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.INNER));

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setARGB(255, 255, 255, 230);

        mButtonBackgroundPaint = new Paint();
        mButtonBackgroundPaint.setARGB(255, 239, 239, 245);
        mButtonBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mButtonPressedBackgroundPaint = new Paint();
        mButtonPressedBackgroundPaint.setARGB(255, 255, 230, 204);
        mButtonPressedBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    private static List<ButtonShape> delayedLocation = new ArrayList<>();

    @Override
    protected final void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean repaint = false;

        List<ButtonShape> delayed = delayedLocation;

        for(int i = 0 ; i  < buttonRows.size() ; i++) {
            List<ButtonShape> currentRow = buttonRows.get(i);

            for(int j = 0 ; j < currentRow.size() ; j++) {

                ButtonShape currentButton = currentRow.get(j);
                if(pressed.contains(currentButton)) {
                    delayed.add(currentButton);
                    pressed.remove(currentButton);
                    continue;
                } else {
                    canvas.drawRect(currentButton.location, mButtonBackgroundPaint);
                }

                String charToShow;
                if(mShiftHolden) {
                    charToShow = currentButton.alterCharacter;
                } else {
                    charToShow = currentButton.character;
                }
                if(charToShow != null) {
                    float[] textStart_XY = calculateTextLocation(currentButton, charToShow, mTextDrawPaint);
                    canvas.drawText(charToShow, textStart_XY[0], textStart_XY[1], mTextDrawPaint);
                }

            }
        }

        if(delayed.size() != 0) {
            repaint = true;
        }

        for(int i = 0 ; i < delayed.size() ; i++) {

            ButtonShape currentButton = delayed.get(i);
            float upperLeft_XY[] = {
                    currentButton.location.left-mButtonRect.width()*0.25f,
                    currentButton.location.top-mButtonRect.height()*0.25f };
            float bottomRight_XY[] = {
                    currentButton.location.right+mButtonRect.width()*0.25f,
                    currentButton.location.bottom+mButtonRect.height()*0.25f };

            canvas.drawRect(upperLeft_XY[0], upperLeft_XY[1],
                            bottomRight_XY[0], bottomRight_XY[1],
                            mButtonPressedBackgroundPaint);

            String charToShow;
            if(mShiftHolden) {
                charToShow = currentButton.alterCharacter;
            } else {
                charToShow = currentButton.character;
            }
            if(charToShow != null) {
                float[] textStart_XY = calculateTextLocation(currentButton, charToShow, mButtonPressedTextDrawPaint);
                canvas.drawText(charToShow, textStart_XY[0], textStart_XY[1], mButtonPressedTextDrawPaint);
            }
        }

        delayed.clear();

        if(repaint) {
            viewUpdater.add((byte)0);
        }
    }

    private float[] calculateTextLocation(ButtonShape currentButton, String charToShow, Paint textPaint) {
        float textStart_XY[] = new float[2];
        float textWidth = textPaint.measureText(charToShow);
        textStart_XY[0] = currentButton.location.centerX() - textWidth/2;
        textStart_XY[1] = currentButton.location.centerY() + textPaint.getTextSize()/2;
        return textStart_XY;
    }

    private Consumer<Byte> viewUpdater = new Consumer<Byte>() {
        @Override
        public void processItem(Byte item) {

            try {
                Thread.sleep(250); // wait before redraw
            } catch (InterruptedException e) {
                Log.e(TAG, "processItem: ", e);
            }
            KeyboardEmuView.this.postInvalidate();
        }
    } ;

}
