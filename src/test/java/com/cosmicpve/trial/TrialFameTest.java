package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.data.attachment.PlayerProfileData;
import com.cosmicpve.data.component.TrialPortalModifiers;
import com.cosmicpve.data.component.TrialTrinketData;
import com.cosmicpve.data.component.TrialTrinketType;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class TrialFameTest {
    @Test void profilePersistsIndependentNonNegativeFame() {
        var data = new PlayerProfileData(1, 125L, 987L);
        var json = PlayerProfileData.CODEC.codec().encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        assertEquals(data, PlayerProfileData.CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow());
        assertThrows(IllegalArgumentException.class, () -> new PlayerProfileData(1, 0, -1));
    }
    @Test void phaseRangesAreInclusiveAndActiveTierDriven() {
        var service = new TrialFameService();
        for (TrialPhase phase : TrialPhase.values()) for (int seed=0; seed<100; seed++) {
            long value = service.roll(phase, RandomSource.create(seed));
            long min = switch (phase) { case APPRENTICE -> 1; case HARDCORE -> 4; case IMPOSSIBLE -> 8; case DEMONIC -> 12; };
            long max = min + (phase == TrialPhase.APPRENTICE ? 2 : 3);
            assertTrue(value >= min && value <= max);
        }
    }
    @Test void fameAccumulatesOnlyOnCompletedRoomsAndNotSkippedRooms() {
        TrialProgress progress = TrialProgress.EMPTY.completeRoom(java.util.List.of(new ItemStack(Items.APPLE)), 7);
        assertEquals(7, progress.baseFame());
        TrialProgress skipped = TrialProgress.initial(new TrialPortalModifiers(1,0,1,0))
                .appendSkippedReward(java.util.List.of(new ItemStack(Items.CARROT)));
        assertEquals(0, skipped.baseFame());
    }
    @Test void bonusFameFloorsWholeRunExactlyOnce() {
        assertEquals(31, TrialPortalModifiers.EMPTY.cashoutFame(31));
        assertEquals(41, TrialPortalModifiers.EMPTY.with(new TrialTrinketData(TrialTrinketType.FAME,33)).cashoutFame(31));
        assertEquals(51, TrialPortalModifiers.EMPTY.with(new TrialTrinketData(TrialTrinketType.FAME,66)).cashoutFame(31));
        assertEquals(62, TrialPortalModifiers.EMPTY.with(new TrialTrinketData(TrialTrinketType.FAME,100)).cashoutFame(31));
    }
}
