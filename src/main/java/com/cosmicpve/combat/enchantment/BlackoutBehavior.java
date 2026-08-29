package com.cosmicpve.combat.enchantment;

import com.cosmicpve.equipment.armor.ArmorSetSuppressionService;
import net.minecraft.world.entity.LivingEntity;

public final class BlackoutBehavior {
    private BlackoutBehavior() {}
    public static double chance(int level) { return Math.max(0, Math.min(4, level)) * .02; }
    public static int durationTicks(int level) { return Math.max(1, Math.min(4, level)) * 20; }
    public static void activate(ArmorSetSuppressionService suppression, LivingEntity target, int level, long tick) {
        suppression.suppress(target, durationTicks(level), tick);
    }
}
