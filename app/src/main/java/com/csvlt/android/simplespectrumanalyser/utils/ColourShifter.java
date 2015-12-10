package com.csvlt.android.simplespectrumanalyser.utils;

import android.graphics.Color;

import java.util.Random;

/**
 * Provides a way to gradually shift colour.
 */
public class ColourShifter {

    public static class Builder {

        private static final int DEFAULT_COLOUR_FRAME_COUNT_THRESHOLD = 600;
        private static final Palette DEFAULT_PALETTE = new Palette(Color.BLACK, Color.WHITE);

        private int colour;
        private int alphaMode = ALPHA_MODE_FIXED;
        private int minAlpha = 0;
        private int threshold = DEFAULT_COLOUR_FRAME_COUNT_THRESHOLD;
        private Palette palette = DEFAULT_PALETTE;

        private boolean isColourSet = false;

        public Builder() {}

        public Builder alphaMode(int mode) {
            alphaMode = mode;
            return this;
        }

        public Builder minAlpha(int min) {
            // todo: if min > 255 throw
            minAlpha = min;
            return this;
        }

        public Builder initialColour(int initialColour) {
            colour = initialColour;
            isColourSet = true;
            return this;
        }

        public Builder palette(Palette colourPalette) {
            palette = colourPalette;
            return this;
        }

        public ColourShifter build() {
            if (!isColourSet) {
                colour = pickColour(DEFAULT_ALPHA, palette);
                isColourSet = true;
            }
            return new ColourShifter(this);
        }
    }

    /**
     * Used to set the range of colours that the colour shifter should produce.
     */
    public static class Palette {
        int minRed;
        int maxRed;
        int minGreen;
        int maxGreen;
        int minBlue;
        int maxBlue;

        // TODO: Santise input.
        public Palette(int min, int max) {
            minRed = Color.red(min);
            minGreen = Color.green(min);
            minBlue = Color.blue(min);
            maxRed = Color.red(max);
            maxGreen = Color.green(max);
            maxBlue = Color.blue(max);
        }
    }

    public static class Randomiser extends Random {
        @Override
        public int nextInt(int n) {
            if (n == 0) {
                return 0;
            }
            return super.nextInt(n);
        }
    }

    public static final int ALPHA_MODE_RANDOM = 0;
    public static final int ALPHA_MODE_FIXED = 1;

    private static final Random RANDOM = new Randomiser();
    private static final int COLOUR_CHANGE_SPEED = 1;

    private int mFrameCountThreshold;
    private int mFrameCount;

    private static final int DEFAULT_ALPHA = 255;
    private int mAlpha = DEFAULT_ALPHA;
    private int mAlphaMode;
    private int mAlphaMin;
    private int mColour;
    private int mTargetColour;
    private Palette mPalette;

    private ColourShifter(Builder builder) {
        init(builder);
    }

    private void init(Builder builder) {
        mColour = builder.colour;
        mPalette = builder.palette;
        mAlpha = Color.alpha(builder.colour);
        mAlphaMode = builder.alphaMode;
        mAlphaMin = builder.minAlpha;
        mFrameCountThreshold = builder.threshold;
    }

    private static int pickColour(int alpha, Palette palette) {
        return Color.argb(alpha,
                RANDOM.nextInt(palette.maxRed - palette.minRed) + palette.minRed,
                RANDOM.nextInt(palette.maxGreen - palette.minGreen) + palette.minGreen,
                RANDOM.nextInt(palette.maxBlue - palette.minBlue) + palette.minBlue);
    }

    private int pickColour() {
        if (mAlphaMode == ALPHA_MODE_RANDOM) {
            mAlpha = RANDOM.nextInt(255);
        }
        if (mAlpha < mAlphaMin) {
            mAlpha = mAlphaMin;
        }
        return pickColour(mAlpha, mPalette);
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
        if (mFrameCount % mFrameCountThreshold == 0) {
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
