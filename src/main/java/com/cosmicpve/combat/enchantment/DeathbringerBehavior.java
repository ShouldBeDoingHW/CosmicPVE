package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;

/** Passive helmet bonus in the shared ordinary outgoing bucket, not a proc. */
public final class DeathbringerBehavior implements OutgoingDamageContributor {
    private final EffectiveEnchantmentsResolver enchantments;
    public DeathbringerBehavior(EffectiveEnchantmentsResolver enchantments) { this.enchantments = enchantments; }

    public static double bonus(int level, boolean heroic) {
        if (level < 0 || level > 3) throw new IllegalArgumentException("Deathbringer level must be 0-3");
        return level == 0 ? 0 : ((heroic ? 5 : 1) + level) / 100.0;
    }

    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.attacker() == null || context.channel() != DamageChannel.ORDINARY) return List.of();
        var effective = enchantments.resolve(context.attacker(),
                context.attacker().getItemBySlot(EquipmentSlot.HEAD), List.of());
        int heroic = effective.level(ModEnchantments.PLANETARY_DEATHBRINGER.identifier());
        var id = heroic > 0 ? ModEnchantments.PLANETARY_DEATHBRINGER.identifier() : ModEnchantments.DEATHBRINGER.identifier();
        int level = heroic > 0 ? heroic : effective.level(id);
        return level <= 0 ? List.of() : List.of(new OutgoingDamageContribution(id, bonus(level, heroic > 0)));
    }
}
