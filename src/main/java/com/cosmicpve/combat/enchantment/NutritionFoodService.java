package com.cosmicpve.combat.enchantment;

import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import com.cosmicpve.equipment.enchantment.VirtualEnchantmentGrant;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;

/** Deterministic completed-food-use behavior; deliberately independent of the probabilistic ProcEngine. */
public final class NutritionFoodService {
    private final EffectiveEnchantmentsResolver enchantments;

    public NutritionFoodService(EffectiveEnchantmentsResolver enchantments) {
        this.enchantments = enchantments;
    }

    public boolean applyCompletedFood(ServerPlayer player) {
        return applyCompletedFood(player, List.of());
    }

    public boolean applyCompletedFood(ServerPlayer player, List<VirtualEnchantmentGrant> virtualGrants) {
        int level = enchantments.resolve(player.getItemBySlot(EquipmentSlot.LEGS), virtualGrants)
                .level(ModEnchantments.NUTRITION.identifier());
        if (level <= 0) return false;
        NutritionBehavior.applyBonus(player.getFoodData(), level);
        return true;
    }
}
