package com.cosmicpve.combat.enchantment;

import com.cosmicpve.registry.ModEnchantments;
import java.util.Optional;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public final class MoltenBehavior {
    public static final float FIRE_SECONDS = 3.0F;

    private MoltenBehavior() {}

    public static int equippedLevelHighest(LivingEntity entity) {
        return aggregateHighest(
                EnchantmentLevels.onStack(entity.getItemBySlot(EquipmentSlot.HEAD), ModEnchantments.MOLTEN),
                EnchantmentLevels.onStack(entity.getItemBySlot(EquipmentSlot.CHEST), ModEnchantments.MOLTEN),
                EnchantmentLevels.onStack(entity.getItemBySlot(EquipmentSlot.LEGS), ModEnchantments.MOLTEN),
                EnchantmentLevels.onStack(entity.getItemBySlot(EquipmentSlot.FEET), ModEnchantments.MOLTEN));
    }

    public static int aggregateHighest(int... levels) {
        int highest = 0;
        for (int level : levels) highest = Math.max(highest, Math.max(0, level));
        return highest;
    }

    public static double chance(int effectiveLevel) {
        return Math.min(1.0, 0.03 * Math.max(0, effectiveLevel));
    }

    /** One plan represents one candidate and therefore one roll for a committed hit. */
    public static Optional<ProcPlan> planForLevels(int... levels) {
        int highest = aggregateHighest(levels);
        return highest == 0 ? Optional.empty() : Optional.of(new ProcPlan(highest, chance(highest)));
    }

    public static void ignite(LivingEntity attacker) {
        attacker.igniteForSeconds(FIRE_SECONDS);
    }

    public record ProcPlan(int effectiveLevel, double chance) {}
}
