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
    public static final double OUTGOING_BONUS = -0.03;
    private final EffectiveEnchantmentsResolver enchantments;
    public DeathPactBehavior(EffectiveEnchantmentsResolver enchantments) { this.enchantments = enchantments; }

    public static double incomingMultiplier(int level) {
        if (level < 0 || level > 5) throw new IllegalArgumentException("Death Pact level must be in [0,5]");
        return 1.0 - 0.02 * level;
    }

    private int level(net.minecraft.world.entity.LivingEntity entity) {
        return enchantments.resolve(entity.getItemBySlot(EquipmentSlot.CHEST), List.of())
                .level(ModEnchantments.DEATH_PACT.identifier());
    }

    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.attacker() == null || level(context.attacker()) <= 0) return List.of();
        return List.of(new OutgoingDamageContribution(ModEnchantments.DEATH_PACT.identifier(), OUTGOING_BONUS));
    }

    @Override public List<IncomingDamageContribution> resolveIncoming(CombatContext context) {
        if (context.target() == null) return List.of();
        int level = level(context.target());
        return level <= 0 ? List.of() : List.of(new IncomingDamageContribution(
                ModEnchantments.DEATH_PACT.identifier(), incomingMultiplier(level)));
    }
}
