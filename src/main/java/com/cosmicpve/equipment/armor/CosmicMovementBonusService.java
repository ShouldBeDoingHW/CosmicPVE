package com.cosmicpve.equipment.armor;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.enchantment.EnchantmentLevels;
import com.cosmicpve.equipment.mask.MaskResolver;
import com.cosmicpve.content.definition.mask.MaskBehavior;
import com.cosmicpve.registry.ModEnchantments;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Aggregates every Cosmic percentage movement source before applying the shared +50% cap. */
public final class CosmicMovementBonusService {
    public static final double MAX_BONUS = .50;
    public static final Identifier MODIFIER_ID = CosmicPVE.id("cosmic_movement_speed");
    private final ArmorSetResolver sets;
    private final MaskResolver masks;

    public CosmicMovementBonusService(ArmorSetResolver sets, MaskResolver masks) { this.sets = sets; this.masks = masks; }

    public double rawBonus(LivingEntity entity) {
        int gears = Math.min(3, Math.max(0, EnchantmentLevels.onStack(
                entity.getItemBySlot(EquipmentSlot.FEET), ModEnchantments.GEARS)));
        double mask = masks.resolve(entity).stream().mapToDouble(definition -> switch (definition.behavior()) {
            case REINDEER -> .05; case PARTY -> .01; default -> 0.0;
        }).sum();
        double set = sets.resolve(entity).map(definition -> switch (definition.id().getPath()) {
            case "dimensional_traveler", "ranger" -> .10;
            case "engineer" -> .15;
            default -> 0.0;
        }).orElse(0.0);
        return .05 * gears + mask + set;
    }

    public static double capped(double raw) { return Math.max(0.0, Math.min(MAX_BONUS, raw)); }

    public void reconcile(LivingEntity entity) {
        var movement = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement == null) return;
        // Remove pre-cap legacy source modifiers during migration.
        movement.removeModifier(com.cosmicpve.combat.enchantment.GearsBehavior.MODIFIER_ID);
        movement.removeModifier(com.cosmicpve.equipment.mask.MaskRuntimeEventBridge.MOVEMENT_ID);
        double amount = capped(rawBonus(entity));
        var existing = movement.getModifier(MODIFIER_ID);
        if (amount == 0.0) { if (existing != null) movement.removeModifier(MODIFIER_ID); return; }
        if (existing == null || existing.amount() != amount)
            movement.addOrUpdateTransientModifier(new AttributeModifier(
                    MODIFIER_ID, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }
}
