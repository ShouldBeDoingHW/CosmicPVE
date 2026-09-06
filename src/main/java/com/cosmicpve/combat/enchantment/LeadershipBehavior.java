package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.ownership.OwnedAllyResolver;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;

public final class LeadershipBehavior implements OutgoingDamageContributor {
    public static int equippedLevel(net.minecraft.world.entity.LivingEntity owner) {
        return aggregateLevels(EnchantmentLevels.onStack(owner, owner.getItemBySlot(EquipmentSlot.CHEST), ModEnchantments.LEADERSHIP),
                EnchantmentLevels.onStack(owner, owner.getItemBySlot(EquipmentSlot.LEGS), ModEnchantments.LEADERSHIP));
    }
    public static int aggregateLevels(int chest, int legs) { return Math.min(20, Math.max(0, chest) + Math.max(0, legs)); }
    public static double bonus(int totalLevel) { return Math.min(20, Math.max(0, totalLevel)) * .01; }
    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.attacker() == null) return List.of();
        var owner = OwnedAllyResolver.owner(context.attacker()).orElse(null);
        int level = owner == null ? 0 : equippedLevel(owner);
        return level <= 0 ? List.of() : List.of(new OutgoingDamageContribution(CosmicPVE.id("leadership"), bonus(level)));
    }
}
