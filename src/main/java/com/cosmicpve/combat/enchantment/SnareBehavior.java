package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.proc.ProcEvent;

public final class SnareBehavior {
    private SnareBehavior() {}

    public static double chance(int level) {
        return level <= 0 ? 0.0 : 0.03 * Math.min(4, level);
    }

    public static boolean eligible(ProcEvent event) {
        // Normal enchanting still restricts Snare to crossbows. Explicitly assigned mob equipment (the
        // Forest Fanatic bow) is authoritative, so runtime eligibility is the projectile hit itself.
        return event.combatResult().map(result -> result.context().category() == AttackCategory.PROJECTILE)
                .orElse(false);
    }

    public static void activate(ProcEvent event, SnareRootService roots) {
        if (event.target() != null && !event.target().isDeadOrDying()) {
            roots.apply(event.target(), event.serverTick());
        }
    }
}
