package com.cosmicpve.client;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TrialHudLayoutTest {
    @Test void expandedCardKeepsNormalTextScaleWithSubstantialPadding() {
        assertEquals(132, TrialClientPresentation.HUD_HEIGHT);
        assertTrue(TrialClientPresentation.MIN_HUD_WIDTH >= 150);
        assertTrue(TrialClientPresentation.HORIZONTAL_PADDING >= 18);
        assertTrue(TrialClientPresentation.LINE_STEP >= 24);
        assertTrue(13 + TrialClientPresentation.LINE_STEP * 3 + 9 < TrialClientPresentation.HUD_HEIGHT);
    }
}
