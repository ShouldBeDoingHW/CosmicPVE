package com.cosmicpve.combat.enchantment;

import net.minecraft.world.entity.LivingEntity;

public final class SoulSiphonBehavior {
    private SoulSiphonBehavior() {}
    public static int cooldownTicks(int level) { return (7 - Math.max(1, Math.min(4, level))) * 20; }
    public static boolean targetBelowHalf(LivingEntity target) {
        return targetBelowHalf(target.getHealth(), target.getMaxHealth());
    }
    static boolean targetBelowHalf(float health, float maximumHealth) {
        return maximumHealth > 0 && health / maximumHealth < .5F;
    }
    public static void activate(LivingEntity attacker) { attacker.heal(1.0F); }
}
