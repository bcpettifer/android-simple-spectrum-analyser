package com.csvlt.android.simplespectrumanalyser.utils;

import android.graphics.Color;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

@Config(manifest= Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ColourShifterUnitTest {

    @Test
    public void builderUsesSensibleDefaults() {
        ColourShifter shifter = new ColourShifter.Builder().build();
        assertThat("0 is not a sensible default for alpha", shifter.getAlpha(), greaterThan(0));
    }

    @Test
    public void colourShifterFunctionsWithDefaultSettings() {
        ColourShifter shifter = new ColourShifter.Builder().build();
        assertValidColour(shifter.getColour());
        shifter.step();
        assertValidColour(shifter.getColour());
    }

    @Test
    public void colourShifterHonoursInitialColour() {
        ColourShifter shifter = new ColourShifter.Builder().initialColour(Color.YELLOW).build();
        assertThat(shifter.getColour(), is(Color.YELLOW));
    }

    @Test
    public void colourShifterChangesColourAfterShift() {
        // TODO: Need to inject random component to validate this correctly.
        // Currently this will occasionally produce false negative results.
        ColourShifter shifter = new ColourShifter.Builder().build();
        int c1 = shifter.getColour();
        shifter.step();
        assertThat(shifter.getColour(), not(c1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void colourShifterThrowsWhenAlphaSetIncorrectly() {
        ColourShifter shifter = new ColourShifter.Builder().build();
        shifter.setAlpha(500);
    }

    private void assertValidColour(int c) {
        int r = Color.red(c);
        int g = Color.green(c);
        int b = Color.blue(c);
        assertThat(r, is(both(greaterThanOrEqualTo(0)).and(lessThanOrEqualTo(255))));
        assertThat(g, is(both(greaterThanOrEqualTo(0)).and(lessThanOrEqualTo(255))));
        assertThat(b, is(both(greaterThanOrEqualTo(0)).and(lessThanOrEqualTo(255))));
    }
}
