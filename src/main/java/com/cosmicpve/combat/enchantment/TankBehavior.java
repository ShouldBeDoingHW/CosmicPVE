package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.IncomingDamageContribution;
import com.cosmicpve.combat.pipeline.IncomingDamageContributor;
import com.cosmicpve.equipment.armor.ArmorSetResolver;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/** Aggregates worn Tank levels and consults the attacker's active, suppression-aware set. */
public final class TankBehavior implements IncomingDamageContributor {
    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final ArmorSetResolver sets;
    private final EffectiveEnchantmentsResolver enchantments;

    public TankBehavior(ArmorSetResolver sets, EffectiveEnchantmentsResolver enchantments) {
        this.sets = sets;
        this.enchantments = enchantments;
    }

    public static int aggregate(int... levels) {
        int total = 0;
        for (int level : levels) total += Math.max(0, Math.min(4, level));
        return Math.min(8, total);
    }

    public static double incomingMultiplier(int effectiveLevel) {
        return 1.0 - Math.min(8, Math.max(0, effectiveLevel)) * .01;
    }

    public int equippedLevel(LivingEntity target) {
        int total = 0;
        for (EquipmentSlot slot : ARMOR)
            total += enchantments.resolve(target, target.getItemBySlot(slot), List.of())
                    .level(ModEnchantments.TANK.identifier());
        return Math.min(8, total);
    }

    @Override public List<IncomingDamageContribution> resolveIncoming(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.target() == null || context.attacker() == null
                || (context.category() != AttackCategory.MELEE && context.category() != AttackCategory.PROJECTILE)
                || sets.resolve(context.attacker()).isEmpty()) return List.of();
        int level = equippedLevel(context.target());
        return level <= 0 ? List.of() : List.of(new IncomingDamageContribution(
                ModEnchantments.TANK.identifier(), incomingMultiplier(level)));
    }
}
