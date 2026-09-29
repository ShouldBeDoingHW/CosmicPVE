package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.trial.room.PitchPerfectService;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class PitchPerfectTest {
    @Test void registrationAndPartyRoundSnapshot() {
        assertTrue(TrialSessionService.roomPool(TrialPhase.APPRENTICE).contains(TrialSessionService.PITCH_PERFECT));
        for (int party = 1; party <= 4; party++) assertEquals(party + 3, PitchPerfectService.requiredRounds(party));
        assertEquals(4, PitchPerfectService.TARGET_PLATES.size());
        assertEquals(6, PitchPerfectService.PORTAL_BUTTONS.size());
        for (int portal = 0; portal < 6; portal++) {
            for (BlockPos button : PitchPerfectService.PORTAL_BUTTONS.get(portal))
                assertEquals(portal, PitchPerfectService.portalForButton(button));
        }
        var selector = new TrialRoomSelectionService();
        var id = TrialSessionService.PITCH_PERFECT;
        var progress = TrialProgress.EMPTY;
        for (int weight : new int[]{5, 3, 1, -1}) {
            var session = TrialSession.joining(java.util.UUID.randomUUID(),
                    net.minecraft.resources.Identifier.parse("minecraft:overworld"), BlockPos.ZERO,
                    java.util.List.of(), java.util.List.of(new com.cosmicpve.instance.InstanceBounds(BlockPos.ZERO, BlockPos.ZERO)))
                    .withProgress(progress);
            assertEquals(weight, selector.weight(session, id));
            progress = progress.beginRoom(id, TrialEncounterState.EMPTY);
        }
    }

    @Test void sixUniqueHarpNotesAndTargetFromCandidates() {
        assertEquals(4.0F, PitchPerfectService.NOTE_VOLUME);
        var random = RandomSource.create(1777);
        for (int generation = 1; generation <= 100; generation++) {
            var round = PitchPerfectService.newRound(random, generation);
            assertEquals(6, round.portalNotes().size());
            assertEquals(6, new HashSet<>(round.portalNotes()).size());
            assertTrue(round.portalNotes().stream().allMatch(note -> note >= 0 && note <= 24));
            assertTrue(round.portalNotes().contains(round.targetNote()));
            assertEquals(generation, round.generation());
        }
        assertEquals(1.0F, PitchPerfectService.pitch(12));
    }

    @Test void wrongAnswerRerollsOnlyCurrentRoundAndStaleGuessesAreIgnored() {
        var round = PitchPerfectService.newRound(RandomSource.create(4), 1);
        var attempt = new PitchPerfectService.Attempt(4, round, new BlockPos(8, 2, 10));
        int wrong = java.util.stream.IntStream.range(0, 6)
                .filter(i -> round.portalNotes().get(i) != round.targetNote()).findFirst().orElseThrow();
        var result = PitchPerfectService.guess(attempt, wrong, 1, 10, RandomSource.create(8));
        assertTrue(result.accepted()); assertFalse(result.correct());
        assertEquals(0, attempt.completed()); assertEquals(4, attempt.required());
        assertEquals(2, attempt.round().generation()); assertNotSame(round, attempt.round());
        assertFalse(PitchPerfectService.guess(attempt, wrong, 1, 11, RandomSource.create(9)).accepted());
        assertFalse(PitchPerfectService.guess(attempt, wrong, 2, 10, RandomSource.create(9)).accepted());
    }

    @Test void wrongAnswerRollsProgressBackOnceAtEveryBoundary() {
        for (int before : new int[]{0, 1, 2, 6}) {
            var attempt = new PitchPerfectService.Attempt(7,
                    PitchPerfectService.newRound(RandomSource.create(7), 1), BlockPos.ZERO);
            for (int i = 0; i < before; i++) {
                var round = attempt.round();
                assertTrue(PitchPerfectService.guess(attempt, round.portalNotes().indexOf(round.targetNote()),
                        round.generation(), i + 1, RandomSource.create(i + 10)).correct());
            }
            var round = attempt.round();
            int wrong = java.util.stream.IntStream.range(0, 6)
                    .filter(i -> round.portalNotes().get(i) != round.targetNote()).findFirst().orElseThrow();
            assertFalse(PitchPerfectService.guess(attempt, wrong, round.generation(), 100,
                    RandomSource.create(99)).correct());
            assertEquals(Math.max(0, before - 1), attempt.completed());
            assertEquals(7, attempt.required());
            assertFalse(PitchPerfectService.guess(attempt, wrong, round.generation(), 100,
                    RandomSource.create(100)).accepted());
            assertTrue(attempt.round().generation() > round.generation());
        }
    }

    @Test void successCueIsFiveStrictlyAscendingNotes() {
        assertEquals(5, PitchPerfectService.SUCCESS_PITCHES.size());
        for (int i = 1; i < 5; i++)
            assertTrue(PitchPerfectService.SUCCESS_PITCHES.get(i) > PitchPerfectService.SUCCESS_PITCHES.get(i - 1));
    }

    @Test void correctAnswerAdvancesOnceAndFinalAnswerCompletesWithoutBonusRound() {
        var attempt = new PitchPerfectService.Attempt(4,
                PitchPerfectService.newRound(RandomSource.create(1), 1), BlockPos.ZERO);
        for (int completed = 1; completed <= 4; completed++) {
            var round = attempt.round();
            int portal = round.portalNotes().indexOf(round.targetNote());
            var result = PitchPerfectService.guess(attempt, portal, round.generation(), completed * 2,
                    RandomSource.create(50 + completed));
            assertTrue(result.accepted()); assertTrue(result.correct());
            assertEquals(completed, attempt.completed()); assertEquals(completed == 4, result.complete());
            assertFalse(PitchPerfectService.guess(attempt, portal, round.generation(), completed * 2,
                    RandomSource.create()).accepted());
        }
        assertEquals(4, attempt.round().generation());
        var message = PitchPerfectService.successMessage(1, 4);
        assertEquals("Pitch number 1 identified! Only 3 more to go!", message.getString());
        assertTrue(message.getStyle().isBold()); assertTrue(message.getStyle().isItalic());
        assertEquals(0xE1BAE8, message.getStyle().getColor().getValue());
    }
}
