package com.cosmicpve.equipment.armor;

import com.cosmicpve.combat.proc.ProcModifierResolver;
import com.cosmicpve.combat.proc.ProcModifiers;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;

public final class ArmorSetProcModifierResolver implements ProcModifierResolver {
    private final ArmorSetResolver sets;
    public ArmorSetProcModifierResolver(ArmorSetResolver sets) { this.sets = sets; }

    @Override public ProcModifiers resolve(LivingEntity owner) {
        return sets.resolve(owner)
                .map(definition -> new ProcModifiers(List.of(1.0), List.of(1.0), definition.procChanceMultipliers()))
                .orElse(ProcModifiers.NONE);
    }
}
