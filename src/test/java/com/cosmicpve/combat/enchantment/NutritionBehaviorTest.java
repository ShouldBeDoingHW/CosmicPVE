package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.food.FoodData;
import org.junit.jupiter.api.Test;

class NutritionBehaviorTest {
    @Test void bonusesUseExactMinecraftFoodDataUnitsAtEveryLevel() {
        for (int level = 1; level <= 3; level++) {
            var food = food(5, 1.0F);
            NutritionBehavior.applyBonus(food, level);
            assertEquals(5 + level, food.getFoodLevel());
            assertEquals(1.0F + 0.25F * level, food.getSaturationLevel(), 1.0E-6F);
            assertEquals(level, NutritionBehavior.hungerBonus(level));
            assertEquals(0.25F * level, NutritionBehavior.saturationBonus(level), 1.0E-6F);
        }
    }

    @Test void bonusComposesAfterNormalFoodAndRespectsFoodDataCaps() {
        var composed = food(6, 2.0F);
        composed.eat(5, 0.6F);
        float afterVanillaSaturation = composed.getSaturationLevel();
        NutritionBehavior.applyBonus(composed, 3);
        assertEquals(14, composed.getFoodLevel());
        assertEquals(afterVanillaSaturation + 0.75F, composed.getSaturationLevel(), 1.0E-6F);

        var capped = food(19, 19.0F);
        NutritionBehavior.applyBonus(capped, 3);
        assertEquals(20, capped.getFoodLevel());
        assertTrue(capped.getSaturationLevel() <= capped.getFoodLevel());
        assertEquals(19.75F, capped.getSaturationLevel(), 1.0E-6F);
    }

    @Test void zeroLevelIsDeterministicNoOp() {
        var food = food(10, 3.0F);
        NutritionBehavior.applyBonus(food, 0);
        assertEquals(10, food.getFoodLevel());
        assertEquals(3.0F, food.getSaturationLevel());
    }

    private static FoodData food(int hunger, float saturation) {
        var food = new FoodData();
        food.setFoodLevel(hunger);
        food.setSaturation(saturation);
        return food;
    }
}
