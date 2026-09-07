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
    private final com.cosmicpve.combat.stack.CombatStackService stacks;

    public LuckBehavior() { this(com.cosmicpve.combat.ownership.GeneralAllyResolver.production(), null); }
    public LuckBehavior(com.cosmicpve.combat.ownership.GeneralAllyResolver allies) { this(allies, null); }
    public LuckBehavior(com.cosmicpve.combat.stack.CombatStackService stacks) {
        this(com.cosmicpve.combat.ownership.GeneralAllyResolver.production(), stacks);
    }
    public LuckBehavior(com.cosmicpve.combat.ownership.GeneralAllyResolver allies,
                        com.cosmicpve.combat.stack.CombatStackService stacks) {
        this.allies = allies;
        this.stacks = stacks;
    }

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
        int frenzy = stacks == null || owner.level().getServer() == null ? 0 : Math.min(10,
                stacks.count(owner, com.cosmicpve.equipment.skin.WeaponSkinCombatResolver.FEEDING_FRENZY,
                        owner.level().getServer().getTickCount()));
        var base = modifiersForLevels(totalLevel, activeSolitude);
        if (frenzy == 0) return base;
        var chances = new java.util.ArrayList<>(base.chanceMultipliers());
        chances.add(feedingFrenzyMultiplier(frenzy));
        return new ProcModifiers(chances, base.cooldownDurationMultipliers(), base.namedChanceMultipliers());
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

    public static double feedingFrenzyMultiplier(int stacks) {
        return 1.0 + Math.min(10, Math.max(0, stacks)) * 0.01;
    }
}
