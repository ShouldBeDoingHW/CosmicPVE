package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.proc.ProcEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public final class PoisonBehavior {
    public static final int DURATION_TICKS = 60;
    public static final int AMPLIFIER = 0;

    private PoisonBehavior() {}

    public static double chance(int level) {
        return 0.05 * level;
    }

    public static void activate(ProcEvent event) {
        LivingEntity target = event.target();
        if (target != null && !target.isDeadOrDying()) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, DURATION_TICKS, AMPLIFIER));
        }
    }
}
