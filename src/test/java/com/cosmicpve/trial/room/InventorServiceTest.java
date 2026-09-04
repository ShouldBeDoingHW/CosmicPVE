package com.cosmicpve.trial.room;

import com.cosmicpve.instance.InstanceBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InventorServiceTest {
    @Test void partyScalingAndAxeThresholdAreExact() {
        assertEquals(100.0, InventorService.bossHealth(1));
        assertEquals(200.0, InventorService.bossHealth(2));
        assertEquals(300.0, InventorService.bossHealth(3));
        assertEquals(400.0, InventorService.bossHealth(4));
        assertSame(Items.DIAMOND_AXE, InventorService.bossAxe(1));
        assertSame(Items.DIAMOND_AXE, InventorService.bossAxe(2));
        assertSame(Items.NETHERITE_AXE, InventorService.bossAxe(3));
        assertSame(Items.NETHERITE_AXE, InventorService.bossAxe(4));
    }

    @Test void hazardAndOrdinaryBossBonusScaleOnlyWithActiveStationCount() {
        for (int count = 0; count <= 4; count++) {
            assertEquals(count * 1.5D, InventorService.hazardDamage(count));
            assertEquals(count * 0.05D, InventorCombatContributor.stationBonus(count), 1.0E-9);
        }
    }

    @Test void activationSelectsOnlyInactiveStationsAndAllActiveIsNoOp() {
        var stations = states(false);
        stations.put(InventorService.Station.BELL,
                new InventorService.StationState(BlockPos.ZERO, BlockPos.ZERO, true, 2, false));
        var attempt = attempt(stations, RandomSource.create(7));
        assertTrue(InventorService.activateRandomStation(attempt));
        long active = stations.values().stream().filter(InventorService.StationState::active).count();
        assertEquals(2, active);
        assertTrue(stations.get(InventorService.Station.BELL).active());
        assertEquals(2, stations.get(InventorService.Station.BELL).progress());

        var all = states(true);
        assertFalse(InventorService.activateRandomStation(attempt(all, RandomSource.create(1))));
        assertTrue(all.values().stream().allMatch(InventorService.StationState::active));
    }

    @Test void activationIntervalContractIsInclusiveFifteenToTwentySeconds() {
        assertEquals(300, InventorService.ACTIVATION_MIN_TICKS);
        assertEquals(400, InventorService.ACTIVATION_MAX_TICKS);
        assertEquals(30, InventorService.HAZARD_INTERVAL_TICKS);
        assertEquals(3, InventorService.REQUIRED_INTERACTIONS);
        assertEquals(100, InventorService.SPEED_DURATION_TICKS);
    }

    private static EnumMap<InventorService.Station, InventorService.StationState> states(boolean active) {
        var result = new EnumMap<InventorService.Station, InventorService.StationState>(InventorService.Station.class);
        for (var station : InventorService.Station.values()) result.put(station,
                new InventorService.StationState(station.controlLocal, station.beaconLocal, active, 0, false));
        return result;
    }
    private static InventorService.Attempt attempt(
            EnumMap<InventorService.Station, InventorService.StationState> states, RandomSource random) {
        return new InventorService.Attempt(UUID.randomUUID(), new InstanceBounds(BlockPos.ZERO, BlockPos.ZERO),
                BlockPos.ZERO, states, UUID.randomUUID(), random, 300, 30);
    }
}
