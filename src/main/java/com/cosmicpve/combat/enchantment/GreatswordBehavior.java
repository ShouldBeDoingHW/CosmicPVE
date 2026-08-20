package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;

/** Additive ordinary damage for attacks whose entity-to-entity distance is at least 2.5 blocks. */
public final class GreatswordBehavior implements OutgoingDamageContributor {
    public static final double MINIMUM_DISTANCE = 2.5;

    public static double bonus(int level, double distance) {
        return level > 0 && Double.isFinite(distance) && distance >= MINIMUM_DISTANCE ? 0.05 * level : 0.0;
    }

    @Override
    public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.attacker() == null || context.target() == null) {
            return List.of();
        }
        int level = context.effectiveEnchantments().level(ModEnchantments.GREATSWORD.identifier());
        double value = bonus(level, context.attacker().distanceTo(context.target()));
        return value > 0.0
                ? List.of(new OutgoingDamageContribution(ModEnchantments.GREATSWORD.identifier(), value))
                : List.of();
    }
}
