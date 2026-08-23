package com.cosmicpve.trial.room;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class RaidingRainbowLogicTest {
    @Test void sequenceIsSeededUniqueAndWrongKillPreservesIt() {
        var first=RaidingRainbowLogic.shuffled(RandomSource.create(77));
        var second=RaidingRainbowLogic.shuffled(RandomSource.create(77));
        assertEquals(first,second); assertEquals(8,new HashSet<>(first).size());
        var wrong=RaidingRainbowLogic.evaluate(first,0,first.get(1));
        assertFalse(wrong.correct()); assertEquals(0,wrong.progress());
        assertEquals(first,first);
    }
    @Test void eightCorrectKillsCompleteOnlyOnTheLast() {
        var sequence=RaidingRainbowLogic.shuffled(RandomSource.create(3));
        for(int i=0;i<8;i++) { var result=RaidingRainbowLogic.evaluate(sequence,i,sequence.get(i)); assertEquals(i==7,result.complete()); }
    }
    @Test void canonicalInitialAndResetSpawnProfileIsAlwaysAnActiveDamageableZombie() {
        var profile=RaidingRainbowService.ACTIVE_ZOMBIE;
        assertEquals(1.0F,profile.health()); assertFalse(profile.noAi()); assertFalse(profile.silent());
        assertFalse(profile.invulnerable()); assertFalse(profile.noGravity());
    }
    @Test void repeatedMistakesResetProgressAndStillAcceptTheNextCorrectColor() {
        var sequence=RaidingRainbowLogic.shuffled(RandomSource.create(91));
        for(int reset=0;reset<2;reset++) {
            var wrong=RaidingRainbowLogic.evaluate(sequence,0,sequence.get(1));
            assertFalse(wrong.correct()); assertEquals(0,wrong.progress());
        }
        var correct=RaidingRainbowLogic.evaluate(sequence,0,sequence.getFirst());
        assertTrue(correct.correct()); assertEquals(1,correct.progress());
    }
}
