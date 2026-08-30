package com.cosmicpve.reward.spawner;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobCategory;

public final class MobSpawnerEligibility {
    private static final TagKey<net.minecraft.world.entity.EntityType<?>> ELIGIBLE_MISC =
            TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("cosmicpve", "spawner_eligible_misc"));
    private static final Identifier IRON_GOLEM = Identifier.withDefaultNamespace("iron_golem");
    private static final Identifier SNOW_GOLEM = Identifier.withDefaultNamespace("snow_golem");

    private MobSpawnerEligibility() {}

    public static boolean isEligible(Identifier entityTypeId) {
        var holder = BuiltInRegistries.ENTITY_TYPE.get(entityTypeId);
        if (holder.isEmpty()) return false;
        if (holder.orElseThrow().value().getCategory() != MobCategory.MISC) return true;
        return holder.orElseThrow().is(ELIGIBLE_MISC)
                || entityTypeId.equals(IRON_GOLEM) || entityTypeId.equals(SNOW_GOLEM);
    }
}
