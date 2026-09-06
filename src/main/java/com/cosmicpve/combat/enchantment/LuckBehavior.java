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
    private final com.cosmicpve.combat.ownership.GeneralAllyResolver allies;

    public LuckBehavior() { this(com.cosmicpve.combat.ownership.GeneralAllyResolver.production()); }
    public LuckBehavior(com.cosmicpve.combat.ownership.GeneralAllyResolver allies) { this.allies = allies; }

    @Override
    public ProcModifiers resolve(LivingEntity owner) {
        int totalLevel = EnchantmentLevels.onStack(owner, owner.getItemBySlot(EquipmentSlot.LEGS), ModEnchantments.LUCK)
                + EnchantmentLevels.onStack(owner, owner.getItemBySlot(EquipmentSlot.FEET), ModEnchantments.LUCK);
        if (owner instanceof net.minecraft.world.entity.player.Player player
                && new com.cosmicpve.upgrade.PlayerUpgradeService().tier(
                        player, com.cosmicpve.upgrade.PlayerUpgrade.PURE_RNG) > 0) totalLevel += 2;
        int solitude = Math.min(3, EnchantmentLevels.onStack(
                owner, owner.getMainHandItem(), ModEnchantments.SOLITUDE));
        int activeSolitude = solitude > 0 && !allies.hasAllyWithin(owner, solitudeRadius(solitude)) ? solitude : 0;
        totalLevel += activeSolitude;
        return modifiersForLevels(totalLevel, activeSolitude);
    }

    public static double solitudeRadius(int level) { return 8.0 - Math.max(1, Math.min(3, level)); }

    static ProcModifiers modifiersForLevel(int totalLevel) {
        return modifiersForLevels(totalLevel, 0);
    }

    static ProcModifiers modifiersForLevels(int totalLevel, int activeSolitudeLevel) {
        double multiplier = chanceMultiplier(totalLevel);
        var named = new java.util.HashMap<net.minecraft.resources.Identifier, Double>();
        if (totalLevel != 0) named.put(ModEnchantments.LUCK.identifier(), multiplier);
        if (activeSolitudeLevel > 0)
            named.put(ModEnchantments.SOLITUDE.identifier(), chanceMultiplier(activeSolitudeLevel));
        return new ProcModifiers(
                List.of(multiplier), List.of(1.0),
                Map.copyOf(named));
    }

    public static double chanceMultiplier(int totalLevel) {
        if (totalLevel < 0) {
            throw new IllegalArgumentException("Luck level total cannot be negative");
        }
        return 1.0 + totalLevel * 0.01;
    }
}
