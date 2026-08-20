package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;

/** One additive percentage point per missing heart, capped at two points per level. */
public final class InsanityBehavior implements OutgoingDamageContributor {
    public static double missingHearts(double currentHealth, double maximumHealth) {
        if (!Double.isFinite(currentHealth) || !Double.isFinite(maximumHealth) || maximumHealth <= 0.0) {
            return 0.0;
        }
        double saneCurrent = Math.clamp(currentHealth, 0.0, maximumHealth);
        return (maximumHealth - saneCurrent) / 2.0;
    }

    public static double bonus(int level, double currentHealth, double maximumHealth) {
        if (level <= 0) return 0.0;
        return Math.min(missingHearts(currentHealth, maximumHealth) * 0.01, level * 0.02);
    }

    @Override
    public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.attacker() == null) return List.of();
        int level = context.effectiveEnchantments().level(ModEnchantments.INSANITY.identifier());
        double value = bonus(level, context.attacker().getHealth(), context.attacker().getMaxHealth());
        return value > 0.0
                ? List.of(new OutgoingDamageContribution(ModEnchantments.INSANITY.identifier(), value))
                : List.of();
    }
}
