package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;

public final class PermanentExecuteBehavior implements OutgoingDamageContributor {
    public static final double DAMAGE_BONUS = .12;
    public static double bonus(int level, double health, double maximum) {
        return level > 0 && maximum > 0 && health / maximum < .60 ? DAMAGE_BONUS : 0.0;
    }
    public static double blessThreshold(int level) { return (10.0 + Math.max(0, Math.min(5, level))) / 100.0; }
    public static double blessChance(int level) { return .02 * Math.max(0, Math.min(5, level)); }
    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.target() == null) return List.of();
        int level = context.effectiveEnchantments().level(ModEnchantments.PERMANENT_EXECUTE.identifier());
        double value = bonus(level, context.target().getHealth(), context.target().getMaxHealth());
        return value == 0.0 ? List.of() : List.of(new OutgoingDamageContribution(
                ModEnchantments.PERMANENT_EXECUTE.identifier(), value));
    }
}
