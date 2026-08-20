package com.cosmicpve.combat.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import com.cosmicpve.registry.ModEnchantments;

public final class AngelicBehavior {
    public static final float HEAL_AMOUNT = 1.0F;

    private AngelicBehavior() {}

    public static int equippedLevelTotal(LivingEntity entity) {
        return EnchantmentLevels.onStack(entity.getItemBySlot(EquipmentSlot.HEAD), ModEnchantments.ANGELIC)
                + EnchantmentLevels.onStack(entity.getItemBySlot(EquipmentSlot.CHEST), ModEnchantments.ANGELIC)
                + EnchantmentLevels.onStack(entity.getItemBySlot(EquipmentSlot.LEGS), ModEnchantments.ANGELIC)
                + EnchantmentLevels.onStack(entity.getItemBySlot(EquipmentSlot.FEET), ModEnchantments.ANGELIC);
    }

    public static int aggregateLevels(int... levels) {
        int total = 0;
        for (int level : levels) {
            total += Math.max(0, level);
        }
        return total;
    }

    public static double chance(int totalLevel) {
        return Math.min(1.0, 0.01 * Math.max(0, totalLevel));
    }

    /** One aggregated plan means at most one Angelic roll and one heal for the committed event. */
    public static java.util.Optional<ProcPlan> planForLevels(int... levels) {
        int total = aggregateLevels(levels);
        return total == 0 ? java.util.Optional.empty()
                : java.util.Optional.of(new ProcPlan(total, chance(total), HEAL_AMOUNT));
    }

    public static float healedHealth(float currentHealth, float maxHealth) {
        return Math.min(maxHealth, currentHealth + HEAL_AMOUNT);
    }

    public record ProcPlan(int totalLevel, double chance, float healAmount) {}
}
