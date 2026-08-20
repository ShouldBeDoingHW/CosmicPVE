package com.cosmicpve.equipment.armor;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import net.minecraft.sounds.SoundEvents;
import org.junit.jupiter.api.Test;

class ArmorCrystalFeedbackTest {
    @Test void successSelectsLevelUpExactlyOnce() {
        assertEquals(List.of(SoundEvents.PLAYER_LEVELUP),
                ArmorCrystalFeedback.soundsFor(ArmorCrystalApplicationService.Outcome.SUCCESS));
    }

    @Test void destructiveFailureSelectsLavaAndAnvilExactlyOnceEach() {
        assertEquals(List.of(SoundEvents.LAVA_AMBIENT, SoundEvents.ANVIL_DESTROY),
                ArmorCrystalFeedback.soundsFor(ArmorCrystalApplicationService.Outcome.FAILED_DESTROYED));
    }
    @Test void protectedFailureKeepsFailureCueWithoutDestructionCue() {
        assertEquals(List.of(SoundEvents.LAVA_AMBIENT),
                ArmorCrystalFeedback.soundsFor(ArmorCrystalApplicationService.Outcome.FAILED_PROTECTED));
    }

    @Test void everyRejectedOutcomeIsSilent() {
        for (var outcome : ArmorCrystalApplicationService.Outcome.values()) {
            if (outcome != ArmorCrystalApplicationService.Outcome.SUCCESS
                    && outcome != ArmorCrystalApplicationService.Outcome.FAILED_DESTROYED
                    && outcome != ArmorCrystalApplicationService.Outcome.FAILED_PROTECTED) {
                assertTrue(ArmorCrystalFeedback.soundsFor(outcome).isEmpty(), outcome.toString());
            }
        }
    }
}
