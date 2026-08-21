package com.cosmicpve.combat.enchantment;

import net.minecraft.world.food.FoodData;

public final class NutritionBehavior {
    /** Minecraft converts nutrition * modifier * 2 into saturation, so this yields exactly 0.25 per level. */
    public static final float SATURATION_MODIFIER = 0.125F;

    private NutritionBehavior() {}

    public static int hungerBonus(int level) {
        return Math.max(0, level);
    }

    public static float saturationBonus(int level) {
        return 0.25F * Math.max(0, level);
    }

    public static void applyBonus(FoodData foodData, int level) {
        int bonus = hungerBonus(level);
        if (bonus > 0) foodData.eat(bonus, SATURATION_MODIFIER);
    }
}
