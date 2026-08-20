package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.TrueDamagePacket;
import com.cosmicpve.combat.proc.ProcEvent;
import net.minecraft.resources.Identifier;

public final class BleedBehavior {
    public static final Identifier STACK_ID = CosmicPVE.id("bleed");
    public static final int TICK_INTERVAL = 30;
    public static final double DAMAGE_PER_STACK = 1.0;
    public static final double MOVEMENT_PENALTY_PER_STACK = 0.01;

    private BleedBehavior() {}

    public static double chance(int level) {
        return 0.01 * level;
    }

    public static boolean isEligibleMelee(ProcEvent event) {
        return event.combatResult()
                .map(result -> result.context().category() == AttackCategory.MELEE)
                .orElse(false);
    }

    public static boolean isTickDue(long applicationTick, long expirationTick, long currentTick) {
        long age = currentTick - applicationTick;
        return currentTick < expirationTick && age >= TICK_INTERVAL && age % TICK_INTERVAL == 0;
    }

    public static double movementMultiplierAmount(int activeStacks) {
        return -MOVEMENT_PENALTY_PER_STACK * activeStacks;
    }

    public static TrueDamagePacket tickPacket() {
        return TrueDamagePacket.standard(STACK_ID, DAMAGE_PER_STACK);
    }
}
