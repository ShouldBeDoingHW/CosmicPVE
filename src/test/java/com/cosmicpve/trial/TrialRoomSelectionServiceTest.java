package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.instance.InstanceBounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class TrialRoomSelectionServiceTest {
    private static final Identifier A=Identifier.parse("cosmicpve:trial/a"), B=Identifier.parse("cosmicpve:trial/b");
    private static TrialSession session(TrialProgress progress) {
        return TrialSession.joining(java.util.UUID.randomUUID(), Identifier.parse("minecraft:overworld"), BlockPos.ZERO,
                List.of(), List.of(new InstanceBounds(BlockPos.ZERO, BlockPos.ZERO))).withProgress(progress);
    }
    @Test void weightsDeclineWithoutASeparateAppearanceCap() {
        var service=new TrialRoomSelectionService(); var progress=TrialProgress.EMPTY;
        assertEquals(5,service.weight(session(progress),A));
        for(int expected:new int[]{3,1,-1}) { progress=progress.beginRoom(A,TrialEncounterState.EMPTY); assertEquals(expected,service.weight(session(progress),A)); }
        service.register((ignored,room)->room.equals(A)?4:0);
        assertEquals(3,service.weight(session(progress),A));
    }
    @Test void immediatelyPreviousRoomIsExcludedAndRngIsInjectable() {
        var service=new TrialRoomSelectionService();
        var current=session(TrialProgress.EMPTY.beginRoom(A,TrialEncounterState.EMPTY));
        assertEquals(B,service.select(current,List.of(A,B),RandomSource.create(42)).orElseThrow());
    }
    @Test void hardcorePoolUsesTheSharedWeightingServiceAndContainsOnlyProductionRooms() {
        var pool=TrialSessionService.roomPool(TrialPhase.HARDCORE);
        assertEquals(List.of(TrialSessionService.FIRE_COLONY,TrialSessionService.ZERO_G,TrialSessionService.BOMB_SQUAD),pool);
        var service=new TrialRoomSelectionService(); var current=session(TrialProgress.EMPTY.debugEnterPhase(TrialPhase.HARDCORE));
        assertEquals(5,service.weight(current,TrialSessionService.FIRE_COLONY));
        assertEquals(5,service.weight(current,TrialSessionService.ZERO_G));
        assertEquals(5,service.weight(current,TrialSessionService.BOMB_SQUAD));
        var afterFire=session(current.progress().beginRoom(TrialSessionService.FIRE_COLONY,TrialEncounterState.EMPTY));
        assertNotEquals(TrialSessionService.FIRE_COLONY,service.select(afterFire,pool,RandomSource.create(7)).orElseThrow());
        assertEquals(3,service.weight(afterFire,TrialSessionService.FIRE_COLONY));
    }
    @Test void apprenticePoolContainsExactlyThreeProductionRoomsAndColdSnapUsesNormalWeighting() {
        var pool=TrialSessionService.roomPool(TrialPhase.APPRENTICE);
        assertEquals(List.of(TrialSessionService.CIRCUIT_CIRCUS,TrialSessionService.RAIDING_RAINBOW,
                TrialSessionService.COLD_SNAP),pool);
        var service=new TrialRoomSelectionService(); var current=session(TrialProgress.EMPTY);
        assertEquals(5,service.weight(current,TrialSessionService.COLD_SNAP));
        var afterCold=session(current.progress().beginRoom(TrialSessionService.COLD_SNAP,TrialEncounterState.EMPTY));
        assertEquals(3,service.weight(afterCold,TrialSessionService.COLD_SNAP));
        assertNotEquals(TrialSessionService.COLD_SNAP,service.select(afterCold,pool,RandomSource.create(4)).orElseThrow());
    }
    @Test void demonicPoolAddsHiddenGraveyardWithoutDiscardingEarlierEligibleRooms() {
        var pool=TrialSessionService.roomPool(TrialPhase.DEMONIC);
        assertEquals(7,pool.size());
        assertTrue(pool.containsAll(TrialSessionService.roomPool(TrialPhase.APPRENTICE)));
        assertTrue(pool.containsAll(TrialSessionService.roomPool(TrialPhase.HARDCORE)));
        assertTrue(pool.contains(TrialSessionService.HIDDEN_GRAVEYARD));
        var service=new TrialRoomSelectionService(); var current=session(TrialProgress.EMPTY.debugEnterPhase(TrialPhase.DEMONIC));
        assertEquals(5,service.weight(current,TrialSessionService.HIDDEN_GRAVEYARD));
        var after=session(current.progress().beginRoom(TrialSessionService.HIDDEN_GRAVEYARD,TrialEncounterState.EMPTY));
        assertEquals(3,service.weight(after,TrialSessionService.HIDDEN_GRAVEYARD));
        assertNotEquals(TrialSessionService.HIDDEN_GRAVEYARD,
                service.select(after,pool,RandomSource.create(11)).orElseThrow());
    }
}
