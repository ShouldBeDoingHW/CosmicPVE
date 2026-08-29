package com.cosmicpve.trial.room;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.trial.TrialRoomLoadoutService;
import java.util.HashSet;
import java.util.List;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

final class Step7GTrialLogicTest {
    @Test void hazePartyScalingAndCycleAreCanonical() {
        assertEquals(3, HazeAndSeekService.requiredPlateCount(1));
        assertEquals(4, HazeAndSeekService.requiredPlateCount(2));
        assertEquals(5, HazeAndSeekService.requiredPlateCount(3));
        assertEquals(6, HazeAndSeekService.requiredPlateCount(4));
        assertEquals(100, HazeAndSeekService.BLIND_TICKS);
        assertEquals(200, HazeAndSeekService.CLEAR_TICKS);
        assertEquals(1, HazeAndSeekService.BLINDNESS_AMPLIFIER);
        assertFalse(HazeAndSeekService.objectivesComplete(3, 2));
        assertTrue(HazeAndSeekService.objectivesComplete(3, 3));
        assertFalse(HazeAndSeekService.shouldCompleteRoom(false, true));
        assertTrue(HazeAndSeekService.shouldCompleteRoom(true, true));
        var remaining = HazeAndSeekService.plateMessage(2);
        assertEquals("You found a pressure plate! 2 more to go!", remaining.getString());
        assertEquals(0x1B4F2C, remaining.getStyle().getColor().getValue());
        assertTrue(remaining.getStyle().isBold()); assertTrue(remaining.getStyle().isItalic());
        assertEquals("You found a pressure plate! Locate the exit portal!",
                HazeAndSeekService.plateMessage(0).getString());
        assertEquals(net.minecraft.sounds.SoundEvents.WITHER_SPAWN, HazeAndSeekService.plateSound());
        assertEquals(net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE, HazeAndSeekService.blindStartSound());
        assertEquals(net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE, HazeAndSeekService.blindClearSound());
    }

    @Test void warzonePartyScalingSelectsUniqueTiersAndAllSixForFourPlayers() {
        for (int party = 1; party <= 4; party++) {
            var tiers = WarzoneGiantsService.selectTiers(party, RandomSource.create(44 + party));
            assertEquals(party + 2, tiers.size());
            assertEquals(tiers.size(), new HashSet<>(tiers).size());
        }
        assertEquals(new HashSet<>(List.of(WarzoneGiantsService.GiantTier.values())),
                new HashSet<>(WarzoneGiantsService.selectTiers(4, RandomSource.create(9))));
    }

    @Test void warzoneHazardUsesDistinctColorsAndExactPhaseDurations() {
        var colors = WarzoneGiantsService.chooseTwoColors(RandomSource.create(18));
        assertEquals(2, colors.size()); assertNotEquals(colors.get(0), colors.get(1));
        assertEquals(200, WarzoneGiantsService.NORMAL_TICKS);
        assertEquals(100, WarzoneGiantsService.WARNING_TICKS);
        assertEquals(60, WarzoneGiantsService.ABSENT_TICKS);
        assertEquals(4, WarzoneGiantsService.unsafeColors(colors).size());
        assertTrue(WarzoneGiantsService.unsafeColors(colors).stream().noneMatch(colors::contains));
        assertTrue(WarzoneGiantsService.hazardSoundTick(0));
        assertTrue(WarzoneGiantsService.hazardSoundTick(5));
        assertTrue(WarzoneGiantsService.hazardSoundTick(10));
        assertFalse(WarzoneGiantsService.hazardSoundTick(4));
    }

    @Test void giantTierOrderIsCanonicalWeakestToStrongest() {
        assertArrayEquals(new WarzoneGiantsService.GiantTier[]{
                WarzoneGiantsService.GiantTier.LEATHER, WarzoneGiantsService.GiantTier.GOLD,
                WarzoneGiantsService.GiantTier.CHAINMAIL, WarzoneGiantsService.GiantTier.IRON,
                WarzoneGiantsService.GiantTier.DIAMOND, WarzoneGiantsService.GiantTier.NETHERITE},
                WarzoneGiantsService.GiantTier.values());
        assertEquals(100.0D, WarzoneGiantsService.GIANT_HEALTH);
        assertEquals(6.0D, WarzoneGiantsService.GIANT_SCALE);
        assertEquals(36_000, WarzoneGiantsService.STRENGTH_TICKS);
        assertEquals(1, WarzoneGiantsService.STRENGTH_AMPLIFIER);
        var survivors = List.of(WarzoneGiantsService.GiantTier.NETHERITE,
                WarzoneGiantsService.GiantTier.IRON, WarzoneGiantsService.GiantTier.DIAMOND);
        assertEquals(WarzoneGiantsService.GiantTier.IRON,
                WarzoneGiantsService.weakestSurviving(survivors).orElseThrow());
        assertTrue(WarzoneGiantsService.damageAllowed(WarzoneGiantsService.GiantTier.IRON, survivors));
        assertFalse(WarzoneGiantsService.damageAllowed(WarzoneGiantsService.GiantTier.DIAMOND, survivors));
    }

    @Test void zeroGHelmetUsesCanonicalImplantsAndLoverMask() {
        assertEquals(3, TrialRoomLoadoutService.ZERO_G_IMPLANTS_LEVEL);
        assertEquals(CosmicPVE.id("lover"), TrialRoomLoadoutService.ZERO_G_MASK_ID);
    }
}
