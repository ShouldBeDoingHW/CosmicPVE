package com.cosmicpve.personalvault;

import com.cosmicpve.activity.ActivityType;
import com.cosmicpve.combat.CosmicCombat;
import com.cosmicpve.trial.TrialRuntime;
import net.minecraft.server.level.ServerPlayer;

/** Central open/forced-close policy; future activity session owners call {@link #onActivityEntered}. */
public final class PersonalVaultAccessService {
    private final PersonalVaultCombatTagService combatTags;
    public PersonalVaultAccessService(PersonalVaultCombatTagService combatTags) { this.combatTags = combatTags; }

    public AccessResult evaluate(ServerPlayer player) {
        if (combatTags.tagged(player)) return new AccessResult(false,
                "Personal Vaults cannot be accessed while combat tagged.");
        boolean trial = TrialRuntime.sessions().active(player.level().getServer())
                .map(session -> session.activeParticipant(player.getUUID())).orElse(false);
        ActivityType activity = CosmicCombat.activities().current(player);
        if (trial || activity == ActivityType.TRIAL) return new AccessResult(false,
                "Personal Vaults cannot be accessed during a Trial.");
        if (activity == ActivityType.ADVENTURE || com.cosmicpve.adventure.AdventureRules.restricted(player)) return new AccessResult(false, "Personal Vaults cannot be accessed during an Adventure.");
        if (activity == ActivityType.INVASION) return new AccessResult(false,
                "Personal Vaults cannot be accessed during an Invasion.");
        if (activity == ActivityType.DUNGEON) return new AccessResult(false,
                "Personal Vaults cannot be accessed during a Dungeon.");
        return new AccessResult(true, "");
    }

    public void onActivityEntered(ServerPlayer player, ActivityType activity) {
        if (restricted(activity)) closeIfOpen(player);
    }

    public void closeIfOpen(ServerPlayer player) {
        if (player.containerMenu instanceof PersonalVaultMenu) player.closeContainer();
    }

    public static boolean restricted(ActivityType activity) {
        return activity == ActivityType.ADVENTURE || activity == ActivityType.TRIAL || activity == ActivityType.INVASION || activity == ActivityType.DUNGEON;
    }

    public record AccessResult(boolean allowed, String message) {}
}
