package com.cosmicpve.combat.enchantment;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public final class EnderShiftBehavior {
    public static final long BASE_COOLDOWN_TICKS = 600L;

    private EnderShiftBehavior() {}

    public static boolean shouldTrigger(double healthAfterDamage, double maxHealth) {
        return healthAfterDamage > 0.0 && maxHealth > 0.0 && healthAfterDamage / maxHealth < 0.25;
    }

    public static int durationTicks(int level) {
        return Math.max(0, level) * 3 * 20;
    }

    public static void activate(LivingEntity wearer, int level) {
        int duration = durationTicks(level);
        wearer.addEffect(new MobEffectInstance(MobEffects.SPEED, duration, 0));
        wearer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, 0));
    }
}
