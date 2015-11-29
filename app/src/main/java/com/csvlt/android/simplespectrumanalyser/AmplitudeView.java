package com.csvlt.android.simplespectrumanalyser;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Simple custom view that reads input from the mic and displays the amplitude.
 * TODO: Move the audio record logic out of the view. It doesn't belong here.
 */
public class AmplitudeView extends View {

    static final String TAG = "AmplitudeView";
    static final int INTERVAL = 50;
    static final int MAX_DATA_POINTS = 500;
    static final int CURSOR_COLOUR = Color.GREEN;

    private static final int SAMPLE_RATE = 44100;
    public static final int CORE_POOL_SIZE = 4;
    private int mMinBufferSize;
    private Executor mReadExecutor;
    private AudioRecord mAudioRecord = null;
    private int mMeanAmplitude;

    private Random mRandom;
    private int mPos;
    Handler mHandler;
    Runnable mRunnable;
    private Paint mPaint;
    private int[] mAmplitudes = new int[MAX_DATA_POINTS];
    private Shader mShader;

    private Normaliser mNormaliser;
    private float mBandSize;
    private Rect mClipBounds = new Rect();

    public AmplitudeView(Context context) {
        super(context);
        init();
    }

    public AmplitudeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AmplitudeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mRandom = new Random();
        mPos = 0;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        // Initialise audio record settings
        mNormaliser = new Normaliser();
        mMinBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mReadExecutor = new ScheduledThreadPoolExecutor(CORE_POOL_SIZE);

        startAudioRecord();

        // Set up values that require the view to have been measured (i.e. require height and width values)
        this.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                try {
                    int viewHeight = AmplitudeView.this.getHeight();
                    int viewWidth = AmplitudeView.this.getWidth();
                    if (mNormaliser.height == 0) {
                        mNormaliser.height = viewHeight;
                    }
                    if (mBandSize == 0) {
                        mBandSize = viewWidth / (float) MAX_DATA_POINTS;
                    }
                    if (mShader == null) {
                        int[] gradientColours = new int[] {Color.RED, Color.YELLOW, Color.GREEN };
                        mShader = new LinearGradient(0, 0, 0, mNormaliser.height, gradientColours, null, Shader.TileMode.MIRROR);
                    }
                    return true;
                } finally {
                    AmplitudeView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                }
            }
        });

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                invalidate();
                mHandler.postDelayed(this, INTERVAL);
                mPos += 1;
                mPos %= MAX_DATA_POINTS;
            }
        };
    }

    private void startAudioRecord() {
        if (mAudioRecord == null) {
            try {
                mAudioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        mMinBufferSize);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Could not start: " + e.getMessage());
            }
        }

        try {
            mAudioRecord.startRecording();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Could not start: " + e.getMessage());
        }
    }

    private void stopAudioRecord() {
        if (mAudioRecord != null) {
            mAudioRecord.stop();
        }
    }

    private void readMeanAmplitude() {
        mReadExecutor.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        int sum = 0;
                        short[] buffer = new short[mMinBufferSize];
                        if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                            mAudioRecord.read(buffer, 0, mMinBufferSize);
                        }

                        for (short sample : buffer) {
                            sum += sample;
                        }

                        mMeanAmplitude = Math.abs(sum / mMinBufferSize);
                    }
                }
        );
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAudioRecord();
        mHandler.removeCallbacks(mRunnable);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAudioRecord();
        mHandler.postDelayed(mRunnable, INTERVAL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.getClipBounds(mClipBounds);

        if (!mClipBounds.isEmpty()) {
            readMeanAmplitude();
            int amplitude = mMeanAmplitude; //mRandom.nextInt(canvas.getHeight());
            int normalisedAmplitude = mNormaliser.normalise(amplitude);
            mAmplitudes[mPos] = normalisedAmplitude;

            mPaint.setShader(mShader);
            for (int i=0; i<MAX_DATA_POINTS; i++) {
                canvas.drawRect(i* mBandSize, mNormaliser.height-mAmplitudes[i], i* mBandSize + mBandSize, mNormaliser.height, mPaint);
            }

            // draw cursor line
            mPaint.setColor(CURSOR_COLOUR);
            float cursorPos = mPos* mBandSize + mBandSize;
            canvas.drawLine(cursorPos, 0, cursorPos, mNormaliser.height, mPaint);
        }
    }

    private static class Normaliser {

        int height;
        private int mMaxValue;

        private void setMaxValue(int value) {
            if (value > mMaxValue) {
                mMaxValue = value;
            }
        }

        public int normalise(int value) {
            setMaxValue(value);
            float norm = (value * height * (1 / (float) mMaxValue));
            return mMaxValue != 0 ? (int) norm : 0;
        }
    }
}
