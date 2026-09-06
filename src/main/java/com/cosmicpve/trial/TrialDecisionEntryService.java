package com.cosmicpve.trial;

import com.cosmicpve.command.PlayerUtilityService;
import net.minecraft.server.level.ServerPlayer;

/** One transition-scoped ordering point for teleport, canonical /restore behavior, and Decision presentation. */
public final class TrialDecisionEntryService {
    public void enter(ServerPlayer player, Runnable teleport, Runnable presentation) {
        perform(teleport, () -> PlayerUtilityService.restore(player), player::removeAllEffects, presentation);
    }

    static void perform(Runnable teleport, Runnable restore, Runnable clearEffects, Runnable presentation) {
        teleport.run();
        restore.run();
        clearEffects.run();
        // Removing Health Boost can reduce maximum HP; restore again against the final attributes.
        restore.run();
        presentation.run();
    }
}
