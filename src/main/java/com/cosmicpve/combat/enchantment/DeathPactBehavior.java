package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.pipeline.IncomingDamageContribution;
import com.cosmicpve.combat.pipeline.IncomingDamageContributor;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;

public final class DeathPactBehavior implements OutgoingDamageContributor, IncomingDamageContributor {
    private final EffectiveEnchantmentsResolver enchantments;
    public DeathPactBehavior(EffectiveEnchantmentsResolver enchantments) { this.enchantments = enchantments; }

    public static double incomingMultiplier(int level) {
        if (level < 0 || level > 5) throw new IllegalArgumentException("Death Pact level must be in [0,5]");
        return level == 0 ? 1.0 : 1.0 - (0.01 + 0.01 * level);
    }

    public static double outgoingBonus(int level) {
        if (level < 0 || level > 5) throw new IllegalArgumentException("Death Pact level must be in [0,5]");
        return level == 0 ? 0.0 : -(0.075 - 0.01 * level);
    }

    private int level(net.minecraft.world.entity.LivingEntity entity) {
        return enchantments.resolve(entity, entity.getItemBySlot(EquipmentSlot.CHEST), List.of())
                .level(ModEnchantments.DEATH_PACT.identifier());
    }

    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.attacker() == null) return List.of();
        int level = level(context.attacker());
        return level <= 0 ? List.of() : List.of(new OutgoingDamageContribution(
                ModEnchantments.DEATH_PACT.identifier(), outgoingBonus(level)));
    }

    @Override public List<IncomingDamageContribution> resolveIncoming(CombatContext context) {
        if (context.target() == null) return List.of();
        int level = level(context.target());
        return level <= 0 ? List.of() : List.of(new IncomingDamageContribution(
                ModEnchantments.DEATH_PACT.identifier(), incomingMultiplier(level)));
    }
}
