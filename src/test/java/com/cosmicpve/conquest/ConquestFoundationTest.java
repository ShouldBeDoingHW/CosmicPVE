package com.cosmicpve.conquest;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.serialization.JsonOps;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class ConquestFoundationTest {
    @Test void thirdDayScheduleIsDeterministicAndMigratesOldSevenDayReceiptsForward() {
        assertEquals(1, ConquestEventService.currentDay(0));
        assertEquals(6, ConquestEventService.currentDay(5 * 24_000L));
        assertEquals(7, ConquestEventService.currentDay(6 * 24_000L));
        assertEquals(0, ConquestEventService.latestThirdDay(2));
        assertEquals(3, ConquestEventService.latestThirdDay(3));
        assertEquals(6, ConquestEventService.latestThirdDay(8));
        assertEquals(9, ConquestEventService.latestThirdDay(9));
        assertFalse(ConquestEventService.shouldSchedule(0, 0));
        assertTrue(ConquestEventService.shouldSchedule(7, 0));
        assertFalse(ConquestEventService.shouldSchedule(7, 7));
        assertFalse(ConquestEventService.shouldSchedule(7, 14));
        assertTrue(ConquestEventService.shouldSchedule(21, 7));
        assertFalse(ConquestEventService.shouldSchedule(6, 7));
        assertTrue(ConquestEventService.shouldSchedule(9, 7));
    }

    @Test void eventIdentityAndAmbushReceiptRoundTrip() {
        UUID id = UUID.randomUUID();
        ConquestEvent event = ConquestEvent.create(id, ConquestOrigin.NATURAL, new BlockPos(12, 70, -5), 1234).discovered();
        var json = ConquestEvent.CODEC.encodeStart(JsonOps.INSTANCE, event).getOrThrow();
        ConquestEvent decoded = ConquestEvent.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        assertEquals(event, decoded);
        assertTrue(decoded.interacted());
        assertTrue(decoded.piratesSpawned());
        assertEquals(ConquestEventState.ACTIVE, decoded.state());
    }

    @Test void completedAndExpiredStatesAreCodecVisible() {
        ConquestEvent event = ConquestEvent.create(UUID.randomUUID(), ConquestOrigin.FLARE,
                BlockPos.ZERO, 0).withState(ConquestEventState.EXPIRED);
        assertEquals(ConquestEventState.EXPIRED, ConquestEvent.CODEC.parse(JsonOps.INSTANCE,
                ConquestEvent.CODEC.encodeStart(JsonOps.INSTANCE, event).getOrThrow()).getOrThrow().state());
    }

    @Test void boundsAreExactlyTwentyByTwentyAndOverlapIsSymmetric() {
        ConquestBounds bounds = ConquestBounds.around(new BlockPos(0, 64, 0));
        assertEquals(-10, bounds.minimumX()); assertEquals(9, bounds.maximumX());
        assertEquals(-10, bounds.minimumZ()); assertEquals(9, bounds.maximumZ());
        assertTrue(bounds.contains(new BlockPos(-10, -64, 9)));
        assertFalse(bounds.contains(new BlockPos(10, 64, 0)));
        assertTrue(bounds.overlaps(ConquestBounds.around(new BlockPos(19, 80, 0))));
        assertFalse(bounds.overlaps(ConquestBounds.around(new BlockPos(20, 80, 0))));
    }

    @Test void surfaceRuleRequiresTwoDistinctAirFaces() {
        BlockPos center = new BlockPos(4, 70, 4);
        var air = new HashSet<BlockPos>();
        air.add(center.above());
        assertFalse(ConquestEventService.hasTwoAirFaces(air::contains, center));
        air.add(center.north());
        assertTrue(ConquestEventService.hasTwoAirFaces(air::contains, center));
    }

    @Test void ordinaryOpenSurfaceAndReplaceableVegetationAreValidSemantics() {
        assertTrue(ConquestEventService.validSurfaceSemantics(true, true, true, true, 5),
                "air above an ordinary flat grass block must be valid");
        assertTrue(net.minecraft.world.level.block.Blocks.SHORT_GRASS.defaultBlockState().canBeReplaced(),
                "short grass is expected to be replaceable by the event chest");
        assertTrue(ConquestEventService.validSurfaceSemantics(true, true, true, true, 4),
                "replaceable dry vegetation on a sturdy surface must not reject placement");
        assertFalse(ConquestEventService.validSurfaceSemantics(true, true, true, true, 1));
        assertFalse(ConquestEventService.validSurfaceSemantics(false, true, true, true, 5));
        assertFalse(ConquestEventService.validSurfaceSemantics(true, false, true, true, 5));
        assertFalse(ConquestEventService.validSurfaceSemantics(true, true, false, true, 5));
        assertFalse(ConquestEventService.validSurfaceSemantics(true, true, true, false, 5));
    }

    @Test void protectionAllowsOnlyIntendedChestMiningAndCreativeBypass() {
        assertTrue(ConquestEventService.shouldDeny(true, true, false, true, false));
        assertTrue(ConquestEventService.shouldDeny(true, true, false, false, true));
        assertFalse(ConquestEventService.shouldDeny(true, true, false, true, true));
        assertFalse(ConquestEventService.shouldDeny(true, true, true, false, false));
        assertFalse(ConquestEventService.shouldDeny(true, false, false, false, false));
        assertFalse(ConquestEventService.shouldDeny(false, true, false, false, false));
    }

    @Test void pirateCountAndGuaranteedBanknoteAlwaysStayCanonical() {
        RandomSource random = RandomSource.create(77);
        for (int i = 0; i < 500; i++) {
            int pirates = ConquestEventService.pirateCount(random);
            assertTrue(pirates >= 3 && pirates <= 5);
            long cents = ConquestEventService.randomBanknoteCents(random);
            assertTrue(cents >= ConquestEventService.MIN_BANKNOTE_CENTS);
            assertTrue(cents <= ConquestEventService.MAX_BANKNOTE_CENTS);
            assertEquals(0, cents % ConquestEventService.BANKNOTE_STEP_CENTS);
        }
    }

    @Test void unattendedTimerAndFlarePolicyAreExplicit() {
        assertEquals(30, ConquestEventService.remainingMinutes(0));
        assertEquals(25, ConquestEventService.remainingMinutes(6_000));
        assertEquals(0, ConquestEventService.remainingMinutes(36_000));
        assertEquals(36_000, ConquestEventService.NATURAL_LIFETIME_TICKS);
        assertEquals(6_000, ConquestEventService.ANNOUNCEMENT_INTERVAL_TICKS);
        assertTrue(ConquestEventService.shouldExpire(ConquestOrigin.NATURAL, false, 36_000));
        assertTrue(ConquestEventService.shouldExpire(ConquestOrigin.NATURAL, true, 36_000));
        assertFalse(ConquestEventService.shouldExpire(ConquestOrigin.FLARE, false, Long.MAX_VALUE));
        assertEquals(3, ConquestEventService.REWARD_ROLLS);
    }

    @Test void canonicalAlertsUseFinalThreeDimensionalCoordinatesAndSemanticStyles() {
        BlockPos position = new BlockPos(125, 73, -42);
        var spawn = ConquestEventService.spawnAnnouncement(position);
        assertEquals("[CONQUEST] Chest spawned at 125, 73, -42!", spawn.getString());
        assertTrue(spawn.getStyle().isBold());
        assertEquals(0xFFAA00, spawn.getStyle().getColor().getValue());
        assertEquals(0x55FFFF, spawn.getSiblings().get(1).getStyle().getColor().getValue());
        assertTrue(spawn.getSiblings().get(1).getStyle().isBold());

        var warning = ConquestEventService.fiveMinuteWarning(position);
        assertEquals("[CONQUEST] 5 MINUTES LEFT — 125, 73, -42!", warning.getString());
        assertEquals(0xFF5555, warning.getSiblings().getFirst().getStyle().getColor().getValue());
        assertTrue(warning.getSiblings().getFirst().getStyle().isBold());
        assertEquals(0x55FFFF, warning.getSiblings().get(2).getStyle().getColor().getValue());
        assertTrue(warning.getSiblings().get(2).getStyle().isBold());
    }

    @Test void persistedAnnouncementReceiptPreventsDuplicateFiveMinuteWarning() {
        ConquestEvent event = ConquestEvent.create(UUID.randomUUID(), ConquestOrigin.NATURAL,
                new BlockPos(1, 70, 2), 0).announcedAt(24_000);
        assertFalse(ConquestEventService.announcementDue(event, 29_999));
        assertTrue(ConquestEventService.announcementDue(event, 30_000));
        ConquestEvent warned = event.announcedAt(30_000);
        var decoded = ConquestEvent.CODEC.parse(JsonOps.INSTANCE,
                ConquestEvent.CODEC.encodeStart(JsonOps.INSTANCE, warned).getOrThrow()).getOrThrow();
        assertFalse(ConquestEventService.announcementDue(decoded, 30_100));
        assertFalse(ConquestEventService.announcementDue(decoded.withState(ConquestEventState.COMPLETED), 40_000));
        assertTrue(ConquestEventService.receivesLifetimeAnnouncements(ConquestOrigin.NATURAL, 30_000));
        assertTrue(ConquestEventService.receivesLifetimeAnnouncements(ConquestOrigin.FLARE, 30_000));
        assertFalse(ConquestEventService.receivesLifetimeAnnouncements(ConquestOrigin.FLARE, 36_000));
    }

    @Test void savedScheduleAndMultipleEventsRoundTripTogether() {
        ConquestSavedData data = new ConquestSavedData();
        data.setLastScheduledDay(21);
        ConquestEvent natural = ConquestEvent.create(UUID.randomUUID(), ConquestOrigin.NATURAL,
                new BlockPos(100, 70, 100), 1000);
        ConquestEvent flare = ConquestEvent.create(UUID.randomUUID(), ConquestOrigin.FLARE,
                new BlockPos(-100, 80, -100), 2000).discovered();
        data.upsert(natural);
        data.upsert(flare);
        var encoded = ConquestSavedData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        ConquestSavedData decoded = ConquestSavedData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(21, decoded.lastScheduledDay());
        assertEquals(List.of(natural, flare), decoded.events());
    }
}
