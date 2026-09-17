package com.cosmicpve.trial;

import com.cosmicpve.data.component.TrialPortalModifiers;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
import java.util.List;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TrialProgressionTest {
    @Test void canonicalOrdinalBoundaries() {
        assertEquals(TrialPhase.APPRENTICE, TrialProgression.phaseForRoomOrdinal(1));
        assertEquals(TrialPhase.APPRENTICE, TrialProgression.phaseForRoomOrdinal(4));
        assertEquals(TrialPhase.HARDCORE, TrialProgression.phaseForRoomOrdinal(5));
        assertEquals(TrialPhase.HARDCORE, TrialProgression.phaseForRoomOrdinal(8));
        assertEquals(TrialPhase.IMPOSSIBLE, TrialProgression.phaseForRoomOrdinal(9));
        assertEquals(TrialPhase.IMPOSSIBLE, TrialProgression.phaseForRoomOrdinal(12));
        assertEquals(TrialPhase.DEMONIC, TrialProgression.phaseForRoomOrdinal(13));
        assertEquals(TrialPhase.DEMONIC, TrialProgression.phaseForRoomOrdinal(100000));
        assertThrows(IllegalArgumentException.class, () -> TrialProgression.phaseForRoomOrdinal(0));
    }
    @Test void thresholdsAreDifferencesNotRepeatedAwards() {
        for (int[] row : new int[][]{{0,4,0},{4,5,1},{5,9,0},{9,10,1},{4,10,2},{0,15,3}})
            assertEquals(row[2], TrialProgression.crossedMadnessThresholds(row[0], row[1]));
        assertEquals(3, TrialProgression.crossedPhaseBoundaries(0, 100));
        assertEquals(0, TrialProgression.crossedPhaseBoundaries(13, 100));
    }
    @Test void sequentialSkipsUseEachOrdinalPhaseAndNeverActualRoomHistory() {
        for (int skip : new int[]{1,4,5,8,10,13}) {
            var progress = TrialProgress.initial(new TrialPortalModifiers(3,0,skip,0,0,0));
            List<TrialPhase> rewarded = new ArrayList<>();
            int timer = 12000;
            for (int i = 0; i < skip; i++) {
                rewarded.add(TrialProgression.phaseForRoomOrdinal(progress.nextRoomOrdinal()));
                int before = progress.completedRooms();
                progress = progress.appendSkippedReward(List.of());
                timer += TrialProgression.phaseEntryTicks(before, progress.completedRooms());
            }
            assertEquals(skip, progress.pot().size());
            assertEquals(skip + 1, progress.nextRoomOrdinal());
            assertEquals(TrialProgression.phaseAfter(skip), progress.phase());
            assertTrue(progress.appearances().isEmpty());
            assertTrue(progress.lastRoom().isEmpty());
            assertEquals(0, progress.baseFame());
            assertEquals(Math.min(skip,4), rewarded.stream().filter(p -> p == TrialPhase.APPRENTICE).count());
            assertEquals(Math.min(Math.max(skip-4,0),4), rewarded.stream().filter(p -> p == TrialPhase.HARDCORE).count());
            assertEquals(Math.min(Math.max(skip-8,0),4), rewarded.stream().filter(p -> p == TrialPhase.IMPOSSIBLE).count());
            assertEquals(Math.max(skip-12,0), rewarded.stream().filter(p -> p == TrialPhase.DEMONIC).count());
            if (skip == 10) {
                assertEquals(11, progress.nextRoomOrdinal());
                assertEquals(TrialPhase.IMPOSSIBLE, progress.phase());
                assertEquals(840*20, timer);
                assertTrue(progress.hardcoreBonusApplied());
                assertTrue(progress.impossibleBonusApplied());
                assertFalse(progress.demonicBonusApplied());
                assertEquals(2, TrialProgression.crossedMadnessThresholds(0,10));
                assertEquals(2, progress.madness().pending());
            }
            var committed = progress.markInitialSkipProcessed();
            assertThrows(IllegalStateException.class, () -> committed.appendSkippedReward(List.of()));
        }
    }
    @Test void arbitraryBoundedModifiersAndFameFloor() {
        var modifiers = new TrialPortalModifiers(3,600,10,8,200,99);
        assertTrue(modifiers.valid());
        assertEquals(24000, modifiers.initialTimerTicks());
        assertEquals(5, modifiers.madnessChoices(8));
        assertEquals(3, modifiers.madnessChoices(3));
        assertEquals(21, modifiers.cashoutFame(7));
        assertEquals(1, new TrialPortalModifiers(3,0,0,0,33,0).cashoutFame(1));
        assertFalse(new TrialPortalModifiers(3,-1,0,0,0,0).valid());
        assertFalse(new TrialPortalModifiers(3,0,1001,0,0,0).valid());
        assertTrue(new TrialPortalModifiers(3,86400,1000,1000,100000,1000).valid());
    }
    @Test void legacyCodecMigratesMinutesAndInsuranceWithoutLosingFame() {
        var legacy = JsonParser.parseString("{\"data_version\":2,\"time_minutes\":3,\"skip_rooms\":2,\"insurance_level\":3,\"fame_percent\":66}");
        var value = TrialPortalModifiers.CODEC.parse(JsonOps.INSTANCE, legacy).getOrThrow();
        assertEquals(180, value.timeBonusSeconds());
        assertEquals(3, value.insuranceItems());
        assertEquals(66, value.famePercent());
        var encoded = TrialPortalModifiers.CODEC.encodeStart(JsonOps.INSTANCE,value).getOrThrow();
        assertEquals(value, TrialPortalModifiers.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow());
        assertTrue(encoded.getAsJsonObject().has("time_bonus_seconds"));
    }
}
