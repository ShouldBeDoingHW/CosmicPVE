package com.cosmicpve.registry;

import com.cosmicpve.CosmicPVE;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> TRUE_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE, CosmicPVE.id("true_damage"));
    public static final ResourceKey<DamageType> MITIGATED_TRUE_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE, CosmicPVE.id("mitigated_true_damage"));

    private ModDamageTypes() {}
}
