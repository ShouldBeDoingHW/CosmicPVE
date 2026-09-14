package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import com.cosmicpve.equipment.enchantment.VirtualEnchantmentGrant;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import java.util.function.Function;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/** Stable max-health reconciliation for ordinary and Heroic Overload. */
public final class OverloadBehavior {
    public static final Identifier MODIFIER_ID = CosmicPVE.id("overload_max_health");

    private OverloadBehavior() {}

    public static double ordinaryBonus(int level) {
        return Math.max(0, Math.min(3, level));
    }

    public static double godlyBonus(int level) {
        return level <= 0 ? 0.0 : 3.0 + Math.min(3, level);
    }

    public static double effectiveBonus(LivingEntity entity, EffectiveEnchantmentsResolver resolver,
            Function<ItemStack, List<VirtualEnchantmentGrant>> virtualGrants) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        var effective = resolver.resolve(entity, chest, virtualGrants.apply(chest));
        int godly = effective.level(ModEnchantments.GODLY_OVERLOAD.identifier());
        return godly > 0 ? godlyBonus(godly)
                : ordinaryBonus(effective.level(ModEnchantments.OVERLOAD.identifier()));
    }

    public static void reconcile(LivingEntity entity, double amount) {
        var attribute = entity.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) return;
        var existing = attribute.getModifier(MODIFIER_ID);
        if (amount <= 0.0) {
            if (existing != null) attribute.removeModifier(MODIFIER_ID);
        } else if (existing == null || Double.compare(existing.amount(), amount) != 0) {
            if (existing != null) attribute.removeModifier(MODIFIER_ID);
            attribute.addTransientModifier(new AttributeModifier(
                    MODIFIER_ID, amount, AttributeModifier.Operation.ADD_VALUE));
        }
        if (entity.getHealth() > entity.getMaxHealth()) entity.setHealth(entity.getMaxHealth());
    }
}
