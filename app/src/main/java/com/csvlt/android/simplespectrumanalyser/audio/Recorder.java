package com.csvlt.android.simplespectrumanalyser.audio;

/**
 * Simple interface to wrap AudioRecord.
 */
public interface Recorder {
    void start();
    void stop();
    void read();
    int getMeanAmplitude();
}
