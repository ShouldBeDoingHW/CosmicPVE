package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;

/** Additive projectile-weapon damage at an inclusive 18-block entity distance. */
public final class EagleEyeBehavior implements OutgoingDamageContributor {
    public static final double MINIMUM_DISTANCE = 18.0;

    public static double bonus(int level, double distance) {
        return level > 0 && Double.isFinite(distance) && distance >= MINIMUM_DISTANCE ? 0.03 * level : 0.0;
    }

    @Override
    public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.attacker() == null || context.target() == null) {
            return List.of();
        }
        int level = context.effectiveEnchantments().level(ModEnchantments.EAGLE_EYE.identifier());
        double value = bonus(level, context.attacker().distanceTo(context.target()));
        return value > 0.0
                ? List.of(new OutgoingDamageContribution(ModEnchantments.EAGLE_EYE.identifier(), value))
                : List.of();
    }
}
