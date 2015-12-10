package com.csvlt.android.simplespectrumanalyser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import com.csvlt.android.simplespectrumanalyser.utils.ColourShifter;

/**
 * Simple visualisation of a sine wave.
 */
public class SineWaveVisualiserView extends View {

    static final int INTERVAL = 60;
    static final int DATA_POINT_COUNT = 800;
    private Paint mPaint;
    private ColourShifter mColourShifter;

    private Handler mHandler;
    private Runnable mRunnable;

    private Canvas mCanvas;
    private Bitmap mBitmap;

    private float mPos = 0;
    private int mAngle = 0;
    private float mStepSize = 1.0f;

    public SineWaveVisualiserView(Context context) {
        super(context);
        init();
    }

    public SineWaveVisualiserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackgroundColor(Color.rgb(32, 32, 48));

        ColourShifter.Builder builder = new ColourShifter.Builder();
        builder.initialColour(Color.argb(128, 0, 128, 0));
        builder.palette(new ColourShifter.Palette(Color.rgb(0, 48, 48), Color.rgb(0, 255, 255)));

        mColourShifter = builder.build();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mColourShifter.getColour());

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                invalidate();
                mHandler.postDelayed(this, INTERVAL);
            }
        };
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacks(mRunnable);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHandler.postDelayed(mRunnable, INTERVAL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mBitmap != null) {
            mBitmap.recycle();
        }
        mCanvas = new Canvas();
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap);

        mStepSize = w / DATA_POINT_COUNT;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int h = canvas.getHeight();
        int w = canvas.getWidth();

        float x = mPos;
        double rad = Math.toRadians(mAngle);
        float yVariation = (float) Math.sin(rad) * h/2;
        float y = h/2 + yVariation;
        mCanvas.drawCircle(x, y, 5f, mPaint);

        mPos += mStepSize;
        if (mPos > w) {
            mPos -= w;
        }
        mAngle++;
        mAngle %= 360;

        canvas.drawBitmap(mBitmap, 0, 0, null);

        mColourShifter.step();
        mPaint.setColor(mColourShifter.getColour());
    }
}
