package com.cosmicpve.economy;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class RawExperienceServiceTest {
    @Test void derivesExactDisplayedStateFromRawPointsAcrossLevelBoundaries() {
        var state = RawExperienceService.stateForTotal(2_275);
        assertEquals(36, state.level());
        assertEquals(73, state.pointsIntoLevel());
    }

    @Test void rejectsInvalidNegativeSnapshotValues() {
        assertThrows(IllegalArgumentException.class, () -> RawExperienceService.stateForTotal(-1));
    }
}
