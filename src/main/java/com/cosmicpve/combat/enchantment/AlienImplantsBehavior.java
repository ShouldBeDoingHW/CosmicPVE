package com.cosmicpve.combat.enchantment;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class AlienImplantsBehavior {
    public static final float HEAL_AMOUNT = 1.0F;
    private static final String HUNGER_KEY = "cosmicpve.alien_implants_hunger";
    private AlienImplantsBehavior() {}
    public static int intervalTicks(int level) {
        if (level < 1 || level > 3) throw new IllegalArgumentException("Alien Implants level must be I-III");
        return 100 - 18 * level;
    }
    public static void activate(LivingEntity entity) {
        if (entity.getHealth() < entity.getMaxHealth()) entity.heal(HEAL_AMOUNT);
        if (!(entity instanceof Player player)) return;
        double stored = entity.getPersistentData().getDoubleOr(HUNGER_KEY, 0.0) + .25;
        if (stored >= 1.0) {
            if (player.getFoodData().getFoodLevel() < 20) player.getFoodData().setFoodLevel(
                    Math.min(20, player.getFoodData().getFoodLevel() + 1));
            stored -= 1.0;
        }
        entity.getPersistentData().putDouble(HUNGER_KEY, stored);
    }
    public static double storedHunger(LivingEntity entity) {
        return entity.getPersistentData().getDoubleOr(HUNGER_KEY, 0.0);
    }
}
