package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.pipeline.IncomingDamageContribution;
import com.cosmicpve.combat.pipeline.IncomingDamageContributor;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;

/** Fire-category mitigation without granting vanilla Fire Resistance. */
public final class ObsidianshieldBehavior implements IncomingDamageContributor {
    private final EffectiveEnchantmentsResolver enchantments;

    public ObsidianshieldBehavior(EffectiveEnchantmentsResolver enchantments) {
        this.enchantments = enchantments;
    }

    public static double incomingMultiplier(int level) {
        if (level < 0 || level > 2) throw new IllegalArgumentException("Obsidianshield level must be in [0,2]");
        return 1.0 - 0.25 * level;
    }

    @Override
    public List<IncomingDamageContribution> resolveIncoming(CombatContext context) {
        if (context.target() == null || context.damageSource() == null
                || !context.damageSource().is(DamageTypeTags.IS_FIRE)) return List.of();
        int level = enchantments.resolve(context.target().getItemBySlot(EquipmentSlot.LEGS), List.of())
                .level(ModEnchantments.OBSIDIANSHIELD.identifier());
        level = Math.min(2, Math.max(0, level));
        return level == 0 ? List.of() : List.of(new IncomingDamageContribution(
                ModEnchantments.OBSIDIANSHIELD.identifier(), incomingMultiplier(level)));
    }
}
