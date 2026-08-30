package com.cosmicpve.equipment.armor;

import com.cosmicpve.CosmicPVE;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class ArmorSetAttributeService {
    public static final Identifier ENGINEER_HEALTH_ID = CosmicPVE.id("engineer_max_health");
    private final ArmorSetResolver sets;
    private final CosmicMovementBonusService movement;
    public ArmorSetAttributeService(ArmorSetResolver sets, CosmicMovementBonusService movement) {
        this.sets = sets; this.movement = movement;
    }
    public void reconcile(LivingEntity entity) {
        movement.reconcile(entity);
        var health = entity.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) return;
        double amount = sets.resolve(entity).filter(d -> d.id().equals(ArmorSetIds.ENGINEER)).isPresent() ? 4.0 : 0.0;
        var existing = health.getModifier(ENGINEER_HEALTH_ID);
        if (amount == 0.0) { if (existing != null) health.removeModifier(ENGINEER_HEALTH_ID); }
        else if (existing == null || existing.amount() != amount)
            health.addOrUpdateTransientModifier(new AttributeModifier(
                    ENGINEER_HEALTH_ID, amount, AttributeModifier.Operation.ADD_VALUE));
        if (entity.getHealth() > entity.getMaxHealth()) entity.setHealth(entity.getMaxHealth());
    }
}
