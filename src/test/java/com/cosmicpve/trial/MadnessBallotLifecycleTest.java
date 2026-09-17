package com.cosmicpve.trial;

import com.cosmicpve.trial.madness.MadnessState;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class MadnessBallotLifecycleTest {
    private MadnessState ballot(int pending) throws Exception {
        return MadnessState.EMPTY.queue(pending).offer(MadnessStateTest.definitions(),5,RandomSource.create(7));
    }
    @Test void soloSubmissionCompletesWithoutTimeoutAndDoesNotOweReopen() throws Exception {
        UUID player=UUID.randomUUID(); var state=ballot(1);
        assertFalse(state.allSubmitted(List.of(player))); assertTrue(state.owesVote(player));
        state=state.vote(player,state.options().getFirst().id());
        assertTrue(state.allSubmitted(List.of(player))); assertFalse(state.owesVote(player));
        assertEquals(0,state.resolveBallot(state.ballotSerial(),List.of(player),RandomSource.create(1)).pending());
    }
    @Test void threePlayersWaitOnlyForCurrentContinuingElectorate() throws Exception {
        UUID a=UUID.randomUUID(),b=UUID.randomUUID(),c=UUID.randomUUID(); var state=ballot(1);
        state=state.vote(a,state.options().getFirst().id()).vote(b,state.options().getFirst().id());
        assertFalse(state.allSubmitted(List.of(a,b,c))); assertTrue(state.owesVote(c));
        // DEAL, departure and disconnect all remove the UUID from the continuing roster.
        assertTrue(state.allSubmitted(List.of(a,b)));
        state=state.vote(c,state.options().getFirst().id()); assertTrue(state.allSubmitted(List.of(a,b,c)));
        assertFalse(state.allSubmitted(List.of()));
    }
    @Test void consecutiveBallotsResetSubmissionAndExcludeFirstWinner() throws Exception {
        UUID player=UUID.randomUUID(); var first=ballot(2);
        first=first.vote(player,first.options().getFirst().id()); long serial=first.ballotSerial();
        var resolved=first.resolveBallot(serial,List.of(player),RandomSource.create(1));
        var second=resolved.offer(MadnessStateTest.definitions(),5,RandomSource.create(2));
        assertEquals(1,second.pending()); assertEquals(serial+1,second.ballotSerial());
        assertTrue(second.votes().isEmpty()); assertTrue(second.owesVote(player));
        assertFalse(second.options().contains(resolved.active().getFirst()));
        assertSame(second,second.resolveBallot(serial,List.of(player),RandomSource.create(3)));
        var finalState=second.vote(player,second.options().getFirst().id())
                .resolveBallot(second.ballotSerial(),List.of(player),RandomSource.create(4));
        assertEquals(0,finalState.pending()); assertEquals(2,finalState.active().size());
    }
    @Test void finalVoteAndTimeoutCannotResolveSameBallotTwice() throws Exception {
        UUID player=UUID.randomUUID(); var state=ballot(2); long serial=state.ballotSerial();
        var resolved=state.vote(player,state.options().getFirst().id()).resolveBallot(serial,List.of(player),RandomSource.create(1));
        assertSame(resolved,resolved.resolveBallot(serial,List.of(player),RandomSource.create(2)));
        assertEquals(1,resolved.pending()); assertEquals(1,resolved.active().size());
        var timeout=state.resolveBallot(serial,List.of(player),RandomSource.create(3));
        assertSame(timeout,timeout.resolveBallot(serial,List.of(player),RandomSource.create(4)));
    }
}
