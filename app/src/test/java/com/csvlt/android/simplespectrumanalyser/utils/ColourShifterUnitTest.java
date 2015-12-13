package com.csvlt.android.simplespectrumanalyser.utils;

import android.graphics.Color;

import org.junit.Ignore;
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

    @Test
    public void colourShifterHonoursPaletteWhenInitialColourNotSet() {
        ColourShifter shifter = new ColourShifter.Builder()
                .palette(new ColourShifter.Palette(Color.CYAN, Color.CYAN))
                .build();
        assertThat(shifter.getColour(), is(Color.CYAN));
    }

    @Test
    public void colourShifterHonoursPaletteWhenShiftingColour() {
        ColourShifter shifter = new ColourShifter.Builder()
                .palette(new ColourShifter.Palette(Color.MAGENTA, Color.MAGENTA))
                .build();
        shifter.step();
        assertThat(shifter.getColour(), is(Color.MAGENTA));
    }

    @Test
    public void colourShifterChangesAlphaWhenRandomModeEnabled() {
        // TODO: Need to inject random component to validate this correctly.
        // Currently this will occasionally produce false negative results.
        ColourShifter shifter = new ColourShifter.Builder()
                .alphaMode(ColourShifter.ALPHA_MODE_RANDOM)
                .build();
        int a1 = Color.alpha(shifter.getColour());
        shifter.step();
        assertThat(Color.alpha(shifter.getColour()), not(a1));
    }

    @Test
    public void colourShifterHonoursMinAlpha() {
        ColourShifter shifter = new ColourShifter.Builder()
                .minAlpha(128)
                .build();
        shifter.setAlpha(64);
        shifter.step();
        assertThat(Color.alpha(shifter.getColour()), is(128));
    }

    @Test
    @Ignore("Requires target colour to be exposed")
    public void colourShifterConvergesTowardsTargetColourOnEachStep() {
        // TODO: Implement functionality and test
    }

    @Test
    @Ignore("Requires target colour to be exposed; requires threshold to be exposed")
    public void colourShifterChangesTargetColourAfterThresholdSteps() {
        // TODO: Implement functionality and test
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
