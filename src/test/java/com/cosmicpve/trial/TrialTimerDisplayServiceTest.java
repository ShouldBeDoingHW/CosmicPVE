package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

class TrialTimerDisplayServiceTest {
    @Test void formatsAuthoritativeTicksWithPositiveCeiling() {
        assertEquals("10:00",TrialTimerDisplayService.formatTicks(12_000));
        assertEquals("8:35",TrialTimerDisplayService.formatTicks(10_300));
        assertEquals("0:09",TrialTimerDisplayService.formatTicks(180));
        assertEquals("0:01",TrialTimerDisplayService.formatTicks(1));
        assertEquals("0:00",TrialTimerDisplayService.formatTicks(0));
    }
    @Test void unchangedDisplayedSecondDoesNotRefresh() {
        var service=new TrialTimerDisplayService(); UUID player=UUID.randomUUID();
        assertTrue(service.accept(player,600));
        assertFalse(service.accept(player,600));
        assertTrue(service.accept(player,599));
        assertFalse(service.accept(player,599));
    }
    @Test void immutableOwnerHeadingIsAcceptedOnlyWhenItChanges() {
        var service=new TrialTimerDisplayService(); UUID player=UUID.randomUUID();
        assertTrue(service.acceptOwner(player,"MrWoofless's Trial"));
        assertFalse(service.acceptOwner(player,"MrWoofless's Trial"));
        assertTrue(service.acceptOwner(player,"Another's Trial"));
    }
    @Test void authoritativePhaseIsAcceptedOnlyWhenItChangesAndUsesCanonicalColors() {
        var service=new TrialTimerDisplayService(); UUID player=UUID.randomUUID();
        assertTrue(service.acceptPhase(player,TrialPhase.APPRENTICE));
        assertFalse(service.acceptPhase(player,TrialPhase.APPRENTICE));
        assertTrue(service.acceptPhase(player,TrialPhase.HARDCORE));
        assertFalse(service.acceptPhase(player,TrialPhase.HARDCORE));
        assertEquals(0xE6E032,TrialPhase.APPRENTICE.color());
        assertEquals(0xE6A732,TrialPhase.HARDCORE.color());
        assertEquals(0xE65C32,TrialPhase.DEMONIC.color());
    }
    @Test void roomLineUsesOverallOrdinalAndDecisionContextWithoutPacketSpam() {
        var base=TrialSession.joining(UUID.randomUUID(),Identifier.parse("minecraft:overworld"),BlockPos.ZERO,List.of(),List.of());
        var service=new TrialTimerDisplayService(); UUID player=UUID.randomUUID();
        assertEquals("Decision Box",TrialTimerDisplayService.roomLine(base,id->"Cold Snap"));
        var room=base.withProgress(base.progress().debugSetCompletedRooms(2)).withState(
                TrialLifecycleState.ROOM_INTRO,100,Optional.of(Identifier.parse("cosmicpve:trial/cold_snap")),false,List.of());
        assertEquals("Room #3---Cold Snap",TrialTimerDisplayService.roomLine(room,id->"Cold Snap"));
        assertTrue(service.acceptRoom(player,"Room #3---Cold Snap"));
        assertFalse(service.acceptRoom(player,"Room #3---Cold Snap"));
        assertTrue(service.acceptRoom(player,"Decision Box"));
    }
    @Test void introAnchorDetectsTranslationButDoesNotConstrainRotation() {
        assertFalse(TrialSessionService.needsIntroCorrection(10.5, 64, 20.5, 10.5, 64, 20.5));
        assertTrue(TrialSessionService.needsIntroCorrection(10.6, 64, 20.5, 10.5, 64, 20.5));
        assertTrue(TrialSessionService.needsIntroCorrection(10.5, 64.1, 20.5, 10.5, 64, 20.5));
    }

    @Test void performanceTrackerKeepsBoundedActiveWindow() {
        var tracker = new TrialPerformanceTracker();
        var session = TrialSession.joining(UUID.randomUUID(), Identifier.parse("minecraft:overworld"),
                BlockPos.ZERO, List.of(), List.of());
        for (int i = 1; i <= 250; i++) tracker.record(i * 1_000L, session);
        var snapshot = tracker.snapshot();
        assertEquals(200, snapshot.samples());
        assertEquals(250, snapshot.maximumMicros());
        assertEquals("JOINING", snapshot.state());
        assertEquals(0, snapshot.participants());
    }
}
