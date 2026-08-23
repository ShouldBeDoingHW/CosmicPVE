package com.cosmicpve.entity.spacepirate;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public final class SpacePirateVariant1 extends SpacePirateEntity {
    public SpacePirateVariant1(EntityType<? extends SpacePirateVariant1> type, Level level) { super(type, level); }
    @Override public SpacePirateVariant variant() { return SpacePirateVariant.VARIANT_1; }
    public static AttributeSupplier.Builder createAttributes() {
        return SpacePirateEntity.createAttributes(SpacePirateDefinition.VARIANT_1.health());
    }
}
