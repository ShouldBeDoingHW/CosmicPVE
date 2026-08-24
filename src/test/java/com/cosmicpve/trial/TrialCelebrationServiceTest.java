package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TrialCelebrationServiceTest {
    @Test void oneHarmlessPresentationIsScheduledPerBeatenRoomAtHalfSecondIntervals() {
        assertEquals(java.util.List.of(0,10,20,30),TrialCelebrationService.launchOffsets(4));
        assertTrue(TrialCelebrationService.launchOffsets(0).isEmpty());
    }
}
