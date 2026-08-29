package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.equipment.armor.ArmorSetResolver;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;

public final class HeroKillerBehavior implements OutgoingDamageContributor {
    private final ArmorSetResolver armorSets;
    public HeroKillerBehavior(ArmorSetResolver armorSets) { this.armorSets = armorSets; }
    public static double bonus(int level) { return Math.max(0, Math.min(3, level)) * .03; }
    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.target() == null
                || armorSets.resolve(context.target()).isEmpty()) return List.of();
        int level = context.effectiveEnchantments().level(ModEnchantments.HERO_KILLER.identifier());
        return level <= 0 ? List.of() : List.of(new OutgoingDamageContribution(
                ModEnchantments.HERO_KILLER.identifier(), bonus(level)));
    }
}
