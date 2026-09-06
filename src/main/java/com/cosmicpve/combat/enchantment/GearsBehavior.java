package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.registry.ModEnchantments;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Equipped-boots movement modifier. Reconciliation is idempotent and removes stale values. */
public final class GearsBehavior {
    public static final Identifier MODIFIER_ID = CosmicPVE.id("gears_movement_speed");

    private GearsBehavior() {}

    public static double movementBonus(int level) {
        if (level < 0 || level > 3) throw new IllegalArgumentException("Gears level must be in [0,3]");
        return 0.05 * level;
    }

    public static AttributeModifier modifier(int level) {
        return new AttributeModifier(
                MODIFIER_ID, movementBonus(level), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    public static void reconcile(LivingEntity entity) {
        var movement = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement == null) return;
        int level = Math.min(3, Math.max(0,
                EnchantmentLevels.onStack(entity, entity.getItemBySlot(EquipmentSlot.FEET), ModEnchantments.GEARS)));
        double amount = movementBonus(level);
        var existing = movement.getModifier(MODIFIER_ID);
        if (amount == 0.0) {
            if (existing != null) movement.removeModifier(MODIFIER_ID);
            return;
        }
        if (existing == null || existing.amount() != amount
                || existing.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_BASE) {
            movement.addOrUpdateTransientModifier(modifier(level));
        }
    }
}
