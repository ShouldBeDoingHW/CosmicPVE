package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import net.minecraft.sounds.SoundEvents;
import org.junit.jupiter.api.Test;

class ItemApplicationFeedbackTest {
    @Test void canonicalSoundSemanticsAreExact() {
        assertEquals(List.of(SoundEvents.PLAYER_LEVELUP),
                ItemApplicationFeedback.soundsFor(ItemApplicationFeedback.Cue.SUCCESS));
        assertEquals(List.of(SoundEvents.LAVA_AMBIENT),
                ItemApplicationFeedback.soundsFor(ItemApplicationFeedback.Cue.FAILED_SURVIVED));
        assertEquals(List.of(SoundEvents.LAVA_AMBIENT),
                ItemApplicationFeedback.soundsFor(ItemApplicationFeedback.Cue.FAILED_PROTECTED));
        assertEquals(List.of(SoundEvents.LAVA_AMBIENT, SoundEvents.ANVIL_DESTROY),
                ItemApplicationFeedback.soundsFor(ItemApplicationFeedback.Cue.FAILED_DESTROYED));
        assertTrue(ItemApplicationFeedback.soundsFor(ItemApplicationFeedback.Cue.REJECTED).isEmpty());
    }

    @Test void everyBookAndOrbRejectionIsSilent() {
        for (var outcome : CosmicBookApplicationResult.Outcome.values()) {
            boolean applied = outcome == CosmicBookApplicationResult.Outcome.SUCCESS
                    || outcome == CosmicBookApplicationResult.Outcome.FAILED_SURVIVED
                    || outcome == CosmicBookApplicationResult.Outcome.FAILED_PROTECTED
                    || outcome == CosmicBookApplicationResult.Outcome.FAILED_DESTROYED;
            assertEquals(applied, !ItemApplicationFeedback.soundsFor(
                    ItemApplicationFeedback.cueFor(outcome)).isEmpty(), outcome.toString());
        }
        for (var outcome : OrbApplicationResult.Outcome.values()) {
            boolean applied = outcome == OrbApplicationResult.Outcome.SUCCESS
                    || outcome == OrbApplicationResult.Outcome.FAILED_SURVIVED
                    || outcome == OrbApplicationResult.Outcome.FAILED_PROTECTED
                    || outcome == OrbApplicationResult.Outcome.FAILED_DESTROYED;
            assertEquals(applied, !ItemApplicationFeedback.soundsFor(
                    ItemApplicationFeedback.cueFor(outcome)).isEmpty(), outcome.toString());
        }
        for (var outcome : TransmogApplicationService.Outcome.values()) {
            assertEquals(outcome == TransmogApplicationService.Outcome.SUCCESS,
                    !ItemApplicationFeedback.soundsFor(ItemApplicationFeedback.cueFor(outcome)).isEmpty());
        }
    }

    @Test void bookAndOrbRateColorsRemainCanonical() {
        assertEquals(0x4DFF74, ItemApplicationColors.SUCCESS);
        assertEquals(0xC92C2C, ItemApplicationColors.DESTROY);
    }
}
