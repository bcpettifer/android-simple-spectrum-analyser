package com.csvlt.android.simplespectrumanalyser.utils;

import android.animation.ArgbEvaluator;
import android.graphics.Color;

import java.util.Random;

/**
 * Provides a way to gradually shift colour.
 */
public class ColourShifter {

    private static final Random RANDOM = new Random();
    private static final int COLOUR_CHANGE_SPEED = 1;

    private static final int COLOUR_FRAME_COUNT_THRESHOLD = 600;
    private int mFrameCount;

    private static final int DEFAULT_ALPHA = 255;
    private int mAlpha = DEFAULT_ALPHA;
    private int mColour;
    private int mTargetColour;

    public ColourShifter() {
        init(pickColour());
    }

    public ColourShifter(int initialColour) {
        init(initialColour);
    }

    private void init(int initialColour) {
        mColour = initialColour;
        mAlpha = Color.alpha(initialColour);
    }

    private int pickColour() {
        return Color.argb(mAlpha, RANDOM.nextInt(255), RANDOM.nextInt(255), RANDOM.nextInt(255));
    }

    /**
     * Gets the current colour.
     * @return the current colour.
     */
    public int getColour() {
        return mColour;
    }

    /**
     * Gets the current alpha value of the generated colours.
     * @return the current alpha.
     */
    public int getAlpha() {
        return mAlpha;
    }

    /**
     * Sets the alpha value to use when generating colours.
     * @param alpha the new alpha to use.
     * @throws IllegalArgumentException if alpha is not between 0 and 255.
     */
    public void setAlpha(int alpha) {
        if (alpha < 0 || alpha > 255) {
            throw new IllegalArgumentException("alpha must be between 0 and 255");
        }
        mAlpha = alpha;
    }

    /**
     * Step change towards the next target colour.
     * Call this once in your loop to change the colour slightly.
     */
    public void step() {
        shiftColour();
        mFrameCount++;
    }

    private void shiftColour() {
        // Change the target colour every X frames
        if (mFrameCount % COLOUR_FRAME_COUNT_THRESHOLD == 0) {
            mTargetColour = pickColour();
        }

        int red = Color.red(mColour);
        int green = Color.green(mColour);
        int blue = Color.blue(mColour);

        // Shift red component close to red target
        if (red < Color.red(mTargetColour))
            red += COLOUR_CHANGE_SPEED;
        if (red > Color.red(mTargetColour))
            red -= COLOUR_CHANGE_SPEED;

        // Shift green component closer to green target
        if (green < Color.green(mTargetColour))
            green += COLOUR_CHANGE_SPEED;
        if (green > Color.green(mTargetColour))
            green -= COLOUR_CHANGE_SPEED;

        // Shift blue component closer to blue target
        if (blue < Color.blue(mTargetColour))
            blue += COLOUR_CHANGE_SPEED;
        if (blue > Color.blue(mTargetColour))
            blue -= COLOUR_CHANGE_SPEED;

        mColour = Color.argb(mAlpha, red, green, blue);
    }
}
