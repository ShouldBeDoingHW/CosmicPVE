package com.cosmicpve.equipment.armor;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.IncomingDamageContribution;
import com.cosmicpve.combat.pipeline.IncomingDamageContributor;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import com.cosmicpve.content.definition.mask.MaskBehavior;
import com.cosmicpve.equipment.mask.MaskResolver;
import net.minecraft.world.entity.EquipmentSlot;

/** One flat-stacking category bucket, clamped before normal incoming multipliers. */
public final class CategoryDamageReductionBehavior implements IncomingDamageContributor {
    private final ArmorSetResolver sets;
    private final EffectiveEnchantmentsResolver enchantments;
    private final MaskResolver masks;
    public CategoryDamageReductionBehavior(ArmorSetResolver sets, EffectiveEnchantmentsResolver enchantments,
            MaskResolver masks) {
        this.sets = sets; this.enchantments = enchantments; this.masks = masks;
    }
    @Override public List<IncomingDamageContribution> resolveIncoming(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.target() == null || context.damageSource() == null)
            return List.of();
        var category = DamageCategoryClassifier.classify(context.damageSource());
        boolean dragonCategory = category == DamageCategoryClassifier.Category.FIRE
                || category == DamageCategoryClassifier.Category.LAVA
                || category == DamageCategoryClassifier.Category.POISON;
        if (!dragonCategory) return List.of();
        double reduction = dragonCategory && sets.resolve(context.target())
                .filter(d -> d.id().equals(ArmorSetIds.DRAGONSLAYER)).isPresent() ? .75 : 0.0;
        if (dragonCategory && masks.resolve(context.target()).stream()
                .anyMatch(definition -> definition.behavior() == MaskBehavior.DRAGON)) reduction += .50;
        if ((category == DamageCategoryClassifier.Category.FIRE
                || category == DamageCategoryClassifier.Category.LAVA) && !context.defensiveCosmicSuppressed()) {
            int level = enchantments.resolve(context.target(), context.target().getItemBySlot(EquipmentSlot.LEGS), List.of())
                    .level(ModEnchantments.OBSIDIANSHIELD.identifier());
            reduction += .25 * Math.min(2, Math.max(0, level));
        }
        double multiplier = multiplier(reduction);
        return multiplier == 1.0 ? List.of() : List.of(new IncomingDamageContribution(
                com.cosmicpve.CosmicPVE.id("flat_category_reduction"), multiplier));
    }
    public static double multiplier(double reduction) { return 1.0 - Math.max(0.0, Math.min(1.0, reduction)); }
}
