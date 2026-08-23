package com.cosmicpve.spacechest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class SpaceChestSessionTransitionsTest {
    @Test void duplicateSelectionIsRejectedAndFifthSelectionCanCommitExactlyOnce() {
        SpaceChestSession state = SpaceChestSession.selecting(SpaceChestTier.ULTIMATE);
        for (int slot = 0; slot < 5; slot++) state = SpaceChestSessionTransitions.select(state, slot).orElseThrow();
        assertTrue(SpaceChestSessionTransitions.select(state, 4).isEmpty());
        var positions = positions();
        SpaceChestSession committed = SpaceChestSessionTransitions.commit(state, positions);
        assertEquals(SpaceChestPhase.COMMITTED, committed.phase());
        assertEquals(List.of(0, 1, 2, 3, 4), committed.selectedSlots());
        assertEquals(5, committed.committedRewards().size());
        assertEquals(Items.DIAMOND, committed.committedRewards().get(3).items().getFirst().getItem());
        assertThrows(IllegalArgumentException.class, () -> SpaceChestSessionTransitions.commit(committed, positions));
    }

    @Test void deliveryMarkerIsIdempotentAndDoesNotMutateRewardItems() {
        SpaceChestSession selected = SpaceChestSession.selecting(SpaceChestTier.MASTERY);
        for (int slot = 0; slot < 5; slot++) selected = SpaceChestSessionTransitions.select(selected, slot).orElseThrow();
        SpaceChestSession committed = SpaceChestSessionTransitions.commit(selected, positions());
        SpaceChestSession delivered = SpaceChestSessionTransitions.markDelivered(committed, 2);
        assertTrue(delivered.committedRewards().get(2).delivered());
        assertFalse(delivered.committedRewards().get(1).delivered());
        assertSame(delivered, SpaceChestSessionTransitions.markDelivered(delivered, 2));
        assertEquals(Items.DIAMOND, delivered.committedRewards().get(2).items().getFirst().getItem());
    }

    @Test void missedRevealSpreadsAcrossEightyTicksThenRemainsForThirtyTickPause() {
        assertEquals(0, SpaceChestSessionTransitions.visibleMissedRewards(0));
        assertEquals(11, SpaceChestSessionTransitions.visibleMissedRewards(40));
        assertEquals(22, SpaceChestSessionTransitions.visibleMissedRewards(80));
        assertEquals(22, SpaceChestSessionTransitions.visibleMissedRewards(109));
        assertEquals(22, SpaceChestSessionTransitions.visibleMissedRewards(1_000));
        assertEquals(80, SpaceChestSessionService.REVEAL_TICKS);
        assertEquals(30, SpaceChestSessionService.FULL_BOARD_PAUSE_TICKS);
        assertEquals(110, SpaceChestSessionService.SELECTED_REWARD_PHASE_TICK);
    }

    @Test void transitionMarkersPreventRepeatedClaimPresentation() {
        SpaceChestSession selected = SpaceChestSession.selecting(SpaceChestTier.ULTIMATE);
        for (int slot = 0; slot < 5; slot++) selected = SpaceChestSessionTransitions.select(selected, slot).orElseThrow();
        SpaceChestSession committed = SpaceChestSessionTransitions.commit(selected, positions());
        SpaceChestSession claimed = SpaceChestSessionTransitions.markDelivered(committed, 0);
        assertFalse(committed.committedRewards().getFirst().delivered());
        assertTrue(claimed.committedRewards().getFirst().delivered());
        assertSame(claimed, SpaceChestSessionTransitions.markDelivered(claimed, 0));
    }

    private static List<List<ItemStack>> positions() {
        var positions = new ArrayList<List<ItemStack>>();
        for (int slot = 0; slot < 27; slot++) positions.add(List.of(new ItemStack(Items.DIAMOND, slot + 1)));
        return positions;
    }
}
