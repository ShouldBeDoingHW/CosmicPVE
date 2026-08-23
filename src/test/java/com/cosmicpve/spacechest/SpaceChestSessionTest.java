package com.cosmicpve.spacechest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mojang.serialization.JsonOps;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpaceChestSessionTest {
    @Test void selectingStateRoundTripsThroughPersistentCodec() {
        var original = new SpaceChestSession(1, SpaceChestTier.LEGENDARY, SpaceChestPhase.SELECTING,
                List.of(0, 13, 26), List.of());
        var json = SpaceChestSession.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        assertEquals(original, SpaceChestSession.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }

    @Test void committedStateRequiresExactlyFiveSelectionsAndFiveBundles() {
        assertThrows(IllegalArgumentException.class, () -> new SpaceChestSession(1, SpaceChestTier.ULTIMATE,
                SpaceChestPhase.COMMITTED, List.of(0, 1, 2, 3), List.of()));
    }

    @Test void selectionsMustBeDistinctAndBounded() {
        assertThrows(IllegalArgumentException.class, () -> new SpaceChestSession(1, SpaceChestTier.MASTERY,
                SpaceChestPhase.SELECTING, List.of(2, 2), List.of()));
        assertThrows(IllegalArgumentException.class, () -> new SpaceChestSession(1, SpaceChestTier.MASTERY,
                SpaceChestPhase.SELECTING, List.of(0, 1, 2, 3, 4, 5), List.of()));
    }

    @Test void tierOwnsStableTableIdentityAndCanonicalColors() {
        assertEquals("cosmicpve:space_chest/ultimate", SpaceChestTier.ULTIMATE.rewardTableId().toString());
        assertEquals(0xFFFF55, SpaceChestTier.ULTIMATE.color());
        assertEquals(0xFFAA00, SpaceChestTier.LEGENDARY.color());
        assertEquals(0xAA0000, SpaceChestTier.MASTERY.color());
    }
}
