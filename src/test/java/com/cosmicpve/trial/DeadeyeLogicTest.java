package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.trial.room.DeadeyeService;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class DeadeyeLogicTest {
    @Test void targetAndRevealMetadataIsOrderedDistinctAndBounded() {
        assertEquals(4, DeadeyeService.SECTION_COUNT);
        assertEquals(4, DeadeyeService.targets().size());
        assertEquals(4, new HashSet<>(DeadeyeService.targets()).size());
        assertEquals(new BlockPos(11, 20, 3), DeadeyeService.targets().get(0));
        assertEquals(new BlockPos(68, 22, 6), DeadeyeService.targets().get(3));
        assertEquals(new BlockPos(68, 23, 21), DeadeyeService.FINAL_LEVER_LOCAL);
        assertTrue(DeadeyeService.revealGroups().values().stream().allMatch(group -> !group.isEmpty()));
    }

    @Test void fallBoundaryIsInclusive() {
        assertFalse(TrialSessionService.shouldExecuteDeadeyeFall(70.0001D, 70));
        assertTrue(TrialSessionService.shouldExecuteDeadeyeFall(70.0D, 70));
        assertTrue(TrialSessionService.shouldExecuteDeadeyeFall(4.0D, 70));
    }

    @Test void targetOrderCannotBeSkippedAndCompletionRequiresAllFour() {
        BlockPos origin = new BlockPos(128, 64, 0);
        assertTrue(DeadeyeService.expectedTarget(origin, 0, origin.offset(DeadeyeService.targets().get(0))));
        assertFalse(DeadeyeService.expectedTarget(origin, 0, origin.offset(DeadeyeService.targets().get(1))));
        assertFalse(DeadeyeService.expectedTarget(origin, 1, origin.offset(DeadeyeService.targets().get(0))));
        assertFalse(DeadeyeService.expectedTarget(origin, 4, origin.offset(DeadeyeService.targets().get(3))));
        assertFalse(DeadeyeService.completionReady(3));
        assertTrue(DeadeyeService.completionReady(4));
        assertFalse(DeadeyeService.completionReady(5));
    }

    @Test void canonicalTemporaryLoadoutConstantsAreExact() {
        assertEquals(16, TrialRoomLoadoutService.DEADEYE_GOLDEN_APPLES);
        assertEquals(1, TrialRoomLoadoutService.DEADEYE_ARROWS);
        assertEquals(3, TrialRoomLoadoutService.DEADEYE_NUTRITION);
        assertEquals(4, TrialRoomLoadoutService.DEADEYE_LIGHTNING);
        assertEquals(5, TrialRoomLoadoutService.DEADEYE_EAGLE_EYE);
    }
}
