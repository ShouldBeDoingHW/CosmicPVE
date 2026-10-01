package com.cosmicpve.combat.enchantment;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/** Heroic Plague Carrier payload; trigger phase and cooldown group stay shared with Plague Carrier. */
public final class EpidemicCarrierBehavior {
    public static final int POISON_TICKS = 160;
    public static final int POISON_AMPLIFIER = 1;
    private EpidemicCarrierBehavior() {}

    public static double bonus(int level) { return Math.max(0, level) * .01; }
    public static double healFraction(int level) { return Math.max(0, level) * .03; }
    public static boolean belowThreshold(double health, double maxHealth) {
        return PlagueCarrierBehavior.belowThreshold(health, maxHealth);
    }
    public static void poison(LivingEntity attacker) {
        attacker.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_TICKS, POISON_AMPLIFIER));
    }
}
