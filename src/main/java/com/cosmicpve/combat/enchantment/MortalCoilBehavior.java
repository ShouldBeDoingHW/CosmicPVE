package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.proc.ProcEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class MortalCoilBehavior {
    public static final int DURATION_TICKS = 100;
    public static final int ABSORPTION_AMPLIFIER = 0;
    public static final float ABSORPTION_HP = 4.0F;

    private MortalCoilBehavior() {}

    public static double chance(int level) {
        return Math.min(1.0, 0.03 * Math.max(0, Math.min(2, level)));
    }

    public static void activate(ProcEvent event) {
        var wearer = event.target();
        if (wearer != null && !wearer.isDeadOrDying()) {
            wearer.addEffect(new MobEffectInstance(
                    MobEffects.ABSORPTION, DURATION_TICKS, ABSORPTION_AMPLIFIER, true, false, true));
        }
    }
}
