package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public final class PlagueCarrierBehavior {
    public static final Identifier COOLDOWN_KEY = CosmicPVE.id("plague_carrier");
    public static final long COOLDOWN_TICKS = 600L;

    private PlagueCarrierBehavior() {}

    public static boolean belowThreshold(double health, double maxHealth) {
        return health > 0.0 && maxHealth > 0.0 && health / maxHealth < 0.25;
    }

    public static int amplifier(int level) {
        return level >= 6 ? 1 : 0;
    }

    public static int durationTicks(int level) {
        return (2 + Math.max(1, Math.min(7, level))) * 20;
    }

    public static void activate(LivingEntity attacker, int level) {
        attacker.addEffect(new MobEffectInstance(MobEffects.POISON, durationTicks(level), amplifier(level)));
    }
}
