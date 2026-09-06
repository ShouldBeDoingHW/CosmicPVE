package com.cosmicpve.equipment.armor;

import com.cosmicpve.combat.proc.ProcModifierResolver;
import com.cosmicpve.combat.proc.ProcModifiers;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import com.cosmicpve.activity.ActivityContextService;

public final class ArmorSetProcModifierResolver implements ProcModifierResolver {
    private final ArmorSetResolver sets;
    private final ActivityContextService activities;
    public ArmorSetProcModifierResolver(ArmorSetResolver sets) { this(sets, new ActivityContextService()); }
    public ArmorSetProcModifierResolver(ArmorSetResolver sets, ActivityContextService activities) { this.sets = sets; this.activities = activities; }

    @Override public ProcModifiers resolve(LivingEntity owner) {
        return sets.resolve(owner)
                .map(definition -> new ProcModifiers(List.of(1.0),
                        definition.id().equals(ArmorSetIds.DIMENSIONAL_TRAVELER)
                                ? List.of(activities.isAdventure(owner) ? .70 : .85) : List.of(1.0),
                        definition.procChanceMultipliers()))
                .orElse(ProcModifiers.NONE);
    }
}
