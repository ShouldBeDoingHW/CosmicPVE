package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.instance.InstanceBounds;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class TrialSessionTest {
    private TrialSession session() {
        return TrialSession.joining(UUID.randomUUID(), Identifier.parse("minecraft:overworld"), BlockPos.ZERO,
                List.of(BlockPos.ZERO, BlockPos.ZERO.above()),
                List.of(new InstanceBounds(BlockPos.ZERO, new BlockPos(10, 10, 10))));
    }
    @Test void newSessionOwnsCanonicalTimersAndStableId() {
        var session = session();
        assertEquals(12_000, session.timerTicks()); assertEquals(600, session.stateTicksRemaining());
        assertEquals(TrialLifecycleState.JOINING, session.state()); assertNotNull(session.sessionId());
    }
    @Test void acceptsExactlyFourDistinctParticipants() {
        var session = session();
        for (int i=0;i<4;i++) session=session.addParticipant(UUID.randomUUID());
        assertEquals(4, session.participants().size());
        var full=session; assertThrows(IllegalStateException.class, () -> full.addParticipant(UUID.randomUUID()));
    }
    @Test void sameParticipantCannotJoinTwice() {
        UUID id=UUID.randomUUID(); var session=session().addParticipant(id); assertSame(session, session.addParticipant(id));
    }
    @Test void removalIsIndividualAndRecorded() {
        UUID a=UUID.randomUUID(), b=UUID.randomUUID(); var next=session().addParticipant(a).addParticipant(b).removeParticipant(a);
        assertEquals(List.of(b), next.participants()); assertTrue(next.removedParticipants().contains(a));
    }
    @Test void timerOnlyTicksDuringActiveGameplay() {
        var decision=session().withState(TrialLifecycleState.DECISION,600,Optional.empty(),true,session().protectedBounds());
        assertEquals(12_000, TrialStateMachine.tickGameplayTimer(decision).timerTicks());
        var active=decision.withState(TrialLifecycleState.ROOM_ACTIVE,0,Optional.of(Identifier.parse("cosmicpve:trial/development_room")),false,decision.protectedBounds());
        assertEquals(11_999, TrialStateMachine.tickGameplayTimer(active).timerTicks());
    }
    @Test void roomIntroCountdownDoesNotConsumeSharedTimer() {
        var intro=session().withState(TrialLifecycleState.ROOM_INTRO,100,Optional.empty(),false,session().protectedBounds());
        assertEquals(99, TrialStateMachine.tickStateCountdown(intro).stateTicksRemaining());
        assertEquals(12_000, TrialStateMachine.tickGameplayTimer(intro).timerTicks());
    }
    @Test void onlyRoomActiveCanComplete() {
        assertFalse(TrialStateMachine.canCompleteRoom(session()));
        assertTrue(TrialStateMachine.canCompleteRoom(session().withState(TrialLifecycleState.ROOM_ACTIVE,0,Optional.empty(),false,session().protectedBounds())));
    }
    @Test void persistentSessionRoundTrips() {
        var original=session().addParticipant(UUID.randomUUID());
        var json=TrialSession.CODEC.encodeStart(JsonOps.INSTANCE,original).getOrThrow();
        assertEquals(original,TrialSession.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow());
    }
    @Test void portalOwnerIsStableAndPersistsEvenAfterOwnerLeaves() {
        UUID ownerId=UUID.randomUUID(); var owner=new TrialOwner(ownerId,"MrWoofless");
        var original=TrialSession.joining(UUID.randomUUID(),Identifier.parse("minecraft:overworld"),BlockPos.ZERO,
                List.of(),List.of(new InstanceBounds(BlockPos.ZERO,BlockPos.ZERO)),owner).addParticipant(ownerId);
        var withoutOwner=original.removeParticipant(ownerId);
        assertEquals(owner,withoutOwner.owner()); assertEquals("MrWoofless's Trial",withoutOwner.owner().header());
        var json=TrialSession.CODEC.encodeStart(JsonOps.INSTANCE,withoutOwner).getOrThrow();
        assertEquals(owner,TrialSession.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow().owner());
    }
    @Test void productionPotDecisionsAppearancesAndEncounterRoundTrip() {
        UUID player=UUID.randomUUID(); var room=Identifier.parse("cosmicpve:trial/raiding_rainbow");
        var encounter=new TrialEncounterState(List.of("red","blue"),1,List.of(),List.of(),List.of());
        var progress=TrialProgress.EMPTY.beginRoom(room,encounter)
                .completeRoom(List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND,2)))
                .beginDecision(List.of(player)).decide(player,TrialDecision.NO_DEAL);
        var original=session().addParticipant(player).withProgress(progress);
        var json=TrialSession.CODEC.encodeStart(JsonOps.INSTANCE,original).getOrThrow();
        var decoded=TrialSession.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow();
        assertEquals(original.sessionId(),decoded.sessionId()); assertEquals(1,decoded.progress().pot().size());
        assertTrue(decoded.progress().pot().getFirst().items().getFirst().is(net.minecraft.world.item.Items.DIAMOND));
        assertEquals(2,decoded.progress().pot().getFirst().items().getFirst().getCount());
        assertEquals(TrialDecision.NO_DEAL,decoded.progress().decision(player));
    }
    @Test void soloNoDealIsImmediatelyReadyButRoomStillUsesNormalIntroState() {
        UUID player=UUID.randomUUID();
        var decision=session().addParticipant(player)
                .withState(TrialLifecycleState.DECISION,600,Optional.empty(),false,session().protectedBounds())
                .withProgress(TrialProgress.EMPTY.beginDecision(List.of(player)).decide(player,TrialDecision.NO_DEAL));
        assertTrue(TrialSessionService.allContinuingReady(decision));
        var intro=decision.withState(TrialLifecycleState.ROOM_INTRO,TrialSession.ROOM_INTRO_TICKS,
                Optional.of(Identifier.parse("cosmicpve:trial/raiding_rainbow")),false,decision.protectedBounds());
        assertEquals(100,intro.stateTicksRemaining());
        assertEquals(TrialLifecycleState.ROOM_INTRO,intro.state());
        assertEquals(12_000,TrialStateMachine.tickGameplayTimer(intro).timerTicks());
    }
    @Test void onlyActiveColdSnapParticipantsCanActivateSharedRoomMechanics() {
        UUID participant=UUID.randomUUID(),outsider=UUID.randomUUID();
        var active=session().addParticipant(participant).withState(TrialLifecycleState.ROOM_ACTIVE,0,
                Optional.of(TrialSessionService.COLD_SNAP),false,session().protectedBounds());
        assertTrue(TrialSessionService.canActivateColdSnap(active,participant));
        assertFalse(TrialSessionService.canActivateColdSnap(active,outsider));
        assertFalse(TrialSessionService.canActivateColdSnap(active.withState(TrialLifecycleState.ROOM_INTRO,100,
                Optional.of(TrialSessionService.COLD_SNAP),false,active.protectedBounds()),participant));
    }
}
