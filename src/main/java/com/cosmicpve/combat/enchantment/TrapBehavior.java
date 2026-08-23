package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.proc.ProcEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class TrapBehavior {
    public static final double BASE_CHANCE = 0.04;
    public static final int SLOWNESS_V_AMPLIFIER = 4;
    private TrapBehavior() {}
    public static double chance(int level) { return level <= 0 ? 0.0 : BASE_CHANCE; }
    public static int durationTicks(int level) { return 20 + 5 * Math.max(1, Math.min(3, level)); }
    public static void activate(ProcEvent event, int level) {
        if (event.target() != null && !event.target().isDeadOrDying()) {
            event.target().addEffect(new MobEffectInstance(MobEffects.SLOWNESS,
                    durationTicks(level), SLOWNESS_V_AMPLIFIER));
        }
    }
}
