package com.cosmicpve.equipment.armor;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

public final class ArmorSetImmunityResolver {
    private final ArmorSetResolver sets;
    public ArmorSetImmunityResolver(ArmorSetResolver sets) { this.sets = sets; }

    public boolean isImmune(LivingEntity entity, Identifier immunity) {
        return sets.resolve(entity).map(definition -> definition.immunities().contains(immunity)).orElse(false);
    }
}
