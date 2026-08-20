package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;

public final class ExecuteBehavior implements OutgoingDamageContributor {
    public static double bonus(int level, double targetHealth, double targetMaxHealth) {
        if (level <= 0 || !(targetMaxHealth > 0.0) || targetHealth / targetMaxHealth >= 0.5) {
            return 0.0;
        }
        return 0.02 * level;
    }

    @Override
    public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.attacker() == null || context.target() == null) {
            return List.of();
        }
        int level = context.effectiveEnchantments().level(ModEnchantments.EXECUTE.identifier());
        double value = bonus(level, context.target().getHealth(), context.target().getMaxHealth());
        return value > 0.0
                ? List.of(new OutgoingDamageContribution(ModEnchantments.EXECUTE.identifier(), value))
                : List.of();
    }
}
