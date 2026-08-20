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
    /** Internal ordinary child-hit type. Its only special property is the bypasses_cooldown tag. */
    public static final ResourceKey<DamageType> DOUBLESTRIKE =
            ResourceKey.create(Registries.DAMAGE_TYPE, CosmicPVE.id("doublestrike"));

    private ModDamageTypes() {}
}
