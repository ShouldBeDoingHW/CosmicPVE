package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.proc.ProcModifierResolver;
import com.cosmicpve.combat.proc.ProcModifiers;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/** Generic proc-chance modifier source backed by actual Luck on leggings and boots. */
public final class LuckBehavior implements ProcModifierResolver {
    @Override
    public ProcModifiers resolve(LivingEntity owner) {
        int totalLevel = EnchantmentLevels.onStack(owner, owner.getItemBySlot(EquipmentSlot.LEGS), ModEnchantments.LUCK)
                + EnchantmentLevels.onStack(owner, owner.getItemBySlot(EquipmentSlot.FEET), ModEnchantments.LUCK);
        if (owner instanceof net.minecraft.world.entity.player.Player player
                && new com.cosmicpve.upgrade.PlayerUpgradeService().tier(
                        player, com.cosmicpve.upgrade.PlayerUpgrade.PURE_RNG) > 0) totalLevel += 2;
        return modifiersForLevel(totalLevel);
    }

    static ProcModifiers modifiersForLevel(int totalLevel) {
        double multiplier = chanceMultiplier(totalLevel);
        return new ProcModifiers(
                List.of(multiplier), List.of(1.0),
                totalLevel == 0 ? Map.of() : Map.of(ModEnchantments.LUCK.identifier(), multiplier));
    }

    public static double chanceMultiplier(int totalLevel) {
        if (totalLevel < 0) {
            throw new IllegalArgumentException("Luck level total cannot be negative");
        }
        return 1.0 + totalLevel * 0.01;
    }
}
