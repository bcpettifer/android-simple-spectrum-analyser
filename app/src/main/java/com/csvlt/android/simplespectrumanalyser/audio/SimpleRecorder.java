package com.csvlt.android.simplespectrumanalyser.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Simple implementation of the Recorder interface.
 */
public class SimpleRecorder implements Recorder {

    private static final String TAG = "SimpleRecorder";
    private static final int SAMPLE_RATE = 44100;
    public static final int CORE_POOL_SIZE = 4;
    private int mMinBufferSize;
    private Executor mReadExecutor;
    private AudioRecord mAudioRecord = null;
    private int mMeanAmplitude;

    public SimpleRecorder() {
        this(new ScheduledThreadPoolExecutor(CORE_POOL_SIZE));
    }

    public SimpleRecorder(Executor executor) {
        this(executor,
                AudioRecord.getMinBufferSize(
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT));
    }

    public SimpleRecorder(Executor executor, int minBufferSize) {
        mReadExecutor = executor;
        mMinBufferSize = minBufferSize;
    }

    @Override
    public void start() {
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

    @Override
    public void stop() {
        if (mAudioRecord != null && mAudioRecord.getState() != AudioRecord.STATE_UNINITIALIZED) {
            if (mAudioRecord.getState() != AudioRecord.RECORDSTATE_STOPPED) {
                mAudioRecord.stop();
            }
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    @Override
    public void read() {
        mReadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                readMeanAmplitude();
            }
        });
    }

    @Override
    public int getMeanAmplitude() {
        return mMeanAmplitude;
    }

    private void readMeanAmplitude() {
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
