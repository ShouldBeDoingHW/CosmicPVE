package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.proc.ProcEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class VenomBehavior {
    public static final int DURATION_TICKS = 60;
    public static final int AMPLIFIER = 0;

    private VenomBehavior() {}

    public static double chance(int level) {
        return Math.min(1.0, 0.15 * Math.max(0, level));
    }

    public static void activate(ProcEvent event) {
        if (event.target() != null && !event.target().isDeadOrDying()) {
            event.target().addEffect(new MobEffectInstance(MobEffects.POISON, DURATION_TICKS, AMPLIFIER));
        }
    }
}
