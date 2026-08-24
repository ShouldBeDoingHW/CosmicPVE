package com.cosmicpve.client;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TrialDecisionScreenLayoutTest {
    @Test void threeRowsRetainSlotsAndReceiveARealLowerFrame() {
        assertEquals(71,TrialDecisionScreen.ROWS_HEIGHT);
        assertEquals(7,TrialDecisionScreen.BOTTOM_FRAME_HEIGHT);
        assertTrue(TrialDecisionScreen.BOTTOM_FRAME_HEIGHT > 0);
    }
}
