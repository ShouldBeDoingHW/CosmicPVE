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
        assertEquals(List.of(TrialSessionService.FIRE_COLONY,TrialSessionService.ZERO_G),pool);
        var service=new TrialRoomSelectionService(); var current=session(TrialProgress.EMPTY.debugEnterPhase(TrialPhase.HARDCORE));
        assertEquals(5,service.weight(current,TrialSessionService.FIRE_COLONY));
        assertEquals(5,service.weight(current,TrialSessionService.ZERO_G));
        var afterFire=session(current.progress().beginRoom(TrialSessionService.FIRE_COLONY,TrialEncounterState.EMPTY));
        assertEquals(TrialSessionService.ZERO_G,service.select(afterFire,pool,RandomSource.create(7)).orElseThrow());
        assertEquals(3,service.weight(afterFire,TrialSessionService.FIRE_COLONY));
    }
}
