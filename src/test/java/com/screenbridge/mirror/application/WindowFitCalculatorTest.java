package com.screenbridge.mirror.application;

import com.screenbridge.mirror.domain.WindowSize;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class WindowFitCalculatorTest {
    @Test
    public void fitUsesHeightWhenHostScreenIsWiderThanAspectRatio() {
        WindowSize size = WindowFitCalculator.fit(1920, 1080, "4:3");

        assertEquals(1440, size.width());
        assertEquals(1080, size.height());
    }

    @Test
    public void fitUsesWidthWhenHostScreenIsTallerThanAspectRatio() {
        WindowSize size = WindowFitCalculator.fit(1000, 1400, "4:3");

        assertEquals(1000, size.width());
        assertEquals(750, size.height());
    }

    @Test
    public void fitRejectsInvalidAspectRatio() {
        try {
            WindowFitCalculator.fit(1920, 1080, "4x3");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertEquals("Aspect ratio must have the form width:height.", expected.getMessage());
        }
    }
}
