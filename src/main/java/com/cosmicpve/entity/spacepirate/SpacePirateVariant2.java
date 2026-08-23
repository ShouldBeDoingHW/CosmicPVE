package com.cosmicpve.entity.spacepirate;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public final class SpacePirateVariant2 extends SpacePirateEntity {
    public SpacePirateVariant2(EntityType<? extends SpacePirateVariant2> type, Level level) { super(type, level); }
    @Override public SpacePirateVariant variant() { return SpacePirateVariant.VARIANT_2; }
    public static AttributeSupplier.Builder createAttributes() {
        return SpacePirateEntity.createAttributes(SpacePirateDefinition.VARIANT_2.health());
    }
}
