package com.cosmicpve.combat.proc;

import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface ProcModifierResolver {
    ProcModifiers resolve(LivingEntity owner);
}
