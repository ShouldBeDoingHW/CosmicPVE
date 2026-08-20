package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageBounds;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.PreDefenseBoundsContributor;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import com.cosmicpve.equipment.enchantment.VirtualEnchantmentGrant;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import java.util.function.Function;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/** Caps only the ordinary attack component before incoming defenses and vanilla mitigation. */
public final class AegisBehavior implements PreDefenseBoundsContributor {
    private final EffectiveEnchantmentsResolver enchantments;
    private final Function<LivingEntity, List<VirtualEnchantmentGrant>> virtualGrants;

    public AegisBehavior(EffectiveEnchantmentsResolver enchantments) {
        this(enchantments, ignored -> List.of());
    }

    public AegisBehavior(
            EffectiveEnchantmentsResolver enchantments,
            Function<LivingEntity, List<VirtualEnchantmentGrant>> virtualGrants) {
        this.enchantments = enchantments;
        this.virtualGrants = virtualGrants;
    }

    public static double capHp(int level) {
        return level <= 0 ? Double.POSITIVE_INFINITY : 14.0 - level;
    }

    @Override
    public DamageBounds resolvePreDefenseBounds(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.target() == null) {
            return DamageBounds.UNBOUNDED;
        }
        int level = enchantments.resolve(
                        context.target().getItemBySlot(EquipmentSlot.CHEST),
                        com.cosmicpve.CosmicPVE.id("actual_chest"), virtualGrants.apply(context.target()))
                .level(ModEnchantments.AEGIS.identifier());
        return new DamageBounds(0.0, capHp(level));
    }
}
