package com.cosmicpve.equipment.armor;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.enchantment.EnchantmentLevels;
import com.cosmicpve.combat.enchantment.EnderWalkerBehavior;
import com.cosmicpve.combat.pipeline.IncomingDamageContribution;
import com.cosmicpve.combat.pipeline.IncomingDamageContributor;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.common.NeoForgeMod;

/** One flat-stacking category bucket, clamped before normal incoming multipliers. */
public final class CategoryDamageReductionBehavior implements IncomingDamageContributor {
    private final ArmorSetResolver sets;
    private final EffectiveEnchantmentsResolver enchantments;
    public CategoryDamageReductionBehavior(ArmorSetResolver sets, EffectiveEnchantmentsResolver enchantments) {
        this.sets = sets; this.enchantments = enchantments;
    }
    @Override public List<IncomingDamageContribution> resolveIncoming(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.target() == null || context.damageSource() == null)
            return List.of();
        boolean fire = context.damageSource().is(DamageTypeTags.IS_FIRE);
        boolean poisonWither = context.damageSource().is(DamageTypes.WITHER)
                || context.damageSource().is(NeoForgeMod.POISON_DAMAGE);
        if (!fire && !poisonWither) return List.of();
        double reduction = sets.resolve(context.target()).filter(d -> d.id().equals(ArmorSetIds.DRAGONSLAYER))
                .isPresent() ? .75 : 0.0;
        if (fire) {
            int level = enchantments.resolve(context.target(), context.target().getItemBySlot(EquipmentSlot.LEGS), List.of())
                    .level(ModEnchantments.OBSIDIANSHIELD.identifier());
            reduction += .25 * Math.min(2, Math.max(0, level));
        } else {
            int level = EnchantmentLevels.onStack(context.target(), context.target().getItemBySlot(EquipmentSlot.FEET), ModEnchantments.ENDER_WALKER);
            reduction += .10 * Math.min(5, Math.max(0, level));
        }
        double multiplier = multiplier(reduction);
        return multiplier == 1.0 ? List.of() : List.of(new IncomingDamageContribution(
                com.cosmicpve.CosmicPVE.id("flat_category_reduction"), multiplier));
    }
    public static double multiplier(double reduction) { return 1.0 - Math.max(0.0, Math.min(1.0, reduction)); }
}
