package com.cosmicpve.combat.enchantment;

import net.minecraft.world.entity.LivingEntity;

public final class ObliterateBehavior {
    public static final double CHANCE = .10;
    private ObliterateBehavior() {}
    public static boolean belowThreshold(float health, float maximum) { return maximum > 0 && health / maximum < .20F; }
    public static double intendedBlocks(int level) { return 3.0 * Math.max(1, Math.min(3, level)); }
    public static double knockbackStrength(int level) { return intendedBlocks(level) * .20; }
    public static void activate(LivingEntity attacker, LivingEntity target, int level) {
        target.knockback(knockbackStrength(level), attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
        target.hurtMarked = true;
    }
}
