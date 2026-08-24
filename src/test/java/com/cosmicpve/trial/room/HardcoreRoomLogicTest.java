package com.cosmicpve.trial.room;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.instance.InstanceBounds;
import com.cosmicpve.trial.TrialEncounterState;
import com.cosmicpve.trial.TrialLifecycleState;
import com.cosmicpve.trial.TrialRoomLoadoutService;
import com.cosmicpve.trial.TrialSession;
import com.cosmicpve.trial.TrialSessionService;
import java.util.List;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import net.minecraft.util.RandomSource;

class HardcoreRoomLogicTest {
    @Test void fireColonyUsesOnlyItsCanonicalFinalLever() {
        var service=new FireColonyService(); var origin=new BlockPos(128,64,0);
        assertTrue(service.isFinalLever(origin.offset(43,20,7),origin));
        assertFalse(service.isFinalLever(origin.offset(42,20,7),origin));
        assertEquals(0xFF0000,TrialRoomLoadoutService.FIRE_COLONY_ARMOR_COLOR);
    }
    @Test void zeroGHasTenDistinctObjectivesAndEightDistinctFixtures() {
        assertEquals(10,ZeroGService.OBJECTIVE_PLATES.size());
        assertEquals(10,ZeroGService.OBJECTIVE_PLATES.stream().distinct().count());
        assertEquals(8,ZeroGService.SHULKER_FIXTURES.size());
        assertEquals(8,ZeroGService.SHULKER_FIXTURES.stream().distinct().count());
    }
    @Test void zeroGObjectiveLookupIsPlayerPositionBoundAndExactlyOnce() {
        var service=new ZeroGService(); var origin=new BlockPos(128,64,0);
        BlockPos first=origin.offset(ZeroGService.OBJECTIVE_PLATES.getFirst());
        assertEquals(first,service.objectiveAt(first,origin,TrialEncounterState.EMPTY).orElseThrow());
        assertEquals(first,service.objectiveAt(first.above(),origin,TrialEncounterState.EMPTY).orElseThrow());
        var advanced=ZeroGService.advanceObjective(TrialEncounterState.EMPTY,first);
        assertTrue(service.objectiveAt(first,origin,advanced).isEmpty());
        assertSame(advanced,ZeroGService.advanceObjective(advanced,first));
        var all=TrialEncounterState.EMPTY;
        for(BlockPos local:ZeroGService.OBJECTIVE_PLATES) all=ZeroGService.advanceObjective(all,origin.offset(local));
        assertEquals(10,all.completedObjectives().size());
    }
    @Test void tenSequentialObjectivesRemainAuthoritativeAcrossTimerPublication() {
        var bounds=new InstanceBounds(BlockPos.ZERO,new BlockPos(32,64,32));
        var session=TrialSession.joining(java.util.UUID.randomUUID(),net.minecraft.resources.Identifier.parse("minecraft:overworld"),
                BlockPos.ZERO,List.of(BlockPos.ZERO),List.of(bounds));
        var encounter=TrialEncounterState.EMPTY; var rolls=new java.util.concurrent.atomic.AtomicInteger();
        for(int i=0;i<ZeroGService.OBJECTIVE_PLATES.size();i++) {
            var progress=ZeroGService.resolveObjective(encounter,ZeroGService.OBJECTIVE_PLATES.get(i),() -> { rolls.incrementAndGet(); return false; });
            assertTrue(progress.accepted()); assertEquals(i==9,progress.complete()); encounter=progress.state();
            session=TrialSessionService.withZeroGEncounter(session,encounter);
            session=session.withTimer(session.timerTicks()-1);
            assertEquals(i+1,session.progress().encounter().completedObjectives().size());
        }
        assertEquals(10,rolls.get());
        assertEquals(10,session.progress().encounter().completedObjectives().stream().distinct().count());
        var duplicate=ZeroGService.resolveObjective(encounter,ZeroGService.OBJECTIVE_PLATES.getLast(),() -> { rolls.incrementAndGet(); return true; });
        assertFalse(duplicate.accepted()); assertSame(encounter,duplicate.state()); assertEquals(10,rolls.get());
    }
    @Test void zeroGLoadoutConstantsPinCanonicalEquipmentAndConsumables() {
        assertEquals(4,TrialRoomLoadoutService.ZERO_G_PROTECTION_LEVEL);
        assertEquals(5,TrialRoomLoadoutService.ZERO_G_ANGELIC_LEVEL);
        assertEquals(3,TrialRoomLoadoutService.ZERO_G_UNBREAKING_LEVEL);
        assertEquals(2,TrialRoomLoadoutService.ZERO_G_GOLDEN_APPLES);
        assertEquals(3,TrialRoomLoadoutService.ZERO_G_MILK_BUCKETS);
    }
    @Test void raidingRainbowLoadoutPinsNetheriteProtectionPuzzleSupplies() {
        assertEquals(3,TrialRoomLoadoutService.RAIDING_RAINBOW_UNBREAKING_LEVEL);
        assertEquals(16,TrialRoomLoadoutService.RAIDING_RAINBOW_PORKCHOPS);
        assertEquals(0,TrialRoomLoadoutService.RAIDING_RAINBOW_ENDER_PEARLS);
        assertEquals(List.of(net.minecraft.world.item.Items.NETHERITE_HELMET,net.minecraft.world.item.Items.NETHERITE_CHESTPLATE,
                net.minecraft.world.item.Items.NETHERITE_LEGGINGS,net.minecraft.world.item.Items.NETHERITE_BOOTS),
                TrialRoomLoadoutService.RAIDING_RAINBOW_ARMOR);
    }
    @Test void zeroGPearlRollIsAnIndependentDeterministicTwentyFivePercentBoundary() {
        var random=RandomSource.create(91); int awards=0;
        for(int i=0;i<1000;i++) if(ZeroGService.awardsPearl(random)) awards++;
        assertTrue(awards>200 && awards<300, "seeded sample should remain near 25%, got "+awards);
    }
}
