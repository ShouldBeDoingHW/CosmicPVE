package com.cosmicpve.reward.spawner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.data.component.MobSpawnerData;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import org.junit.jupiter.api.Test;

class MobSpawnerDataTest {
    @Test void typedIdentityRoundTripsAndVersionIsChecked() {
        var original = new MobSpawnerData(MobSpawnerData.CURRENT_DATA_VERSION, Identifier.parse("minecraft:blaze"));
        var encoded = MobSpawnerData.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        assertEquals(original, MobSpawnerData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
        assertTrue(original.isCurrent());
        assertFalse(new MobSpawnerData(0, original.entityTypeId()).isCurrent());
    }

    @Test void validationBoundaryAcceptsMobsAndRejectsNonsensicalMiscEntities() {
        assertFalse(EntityType.BLAZE.getCategory() == MobCategory.MISC);
        assertTrue(EntityType.ARROW.getCategory() == MobCategory.MISC);
        assertTrue(BuiltInRegistries.ENTITY_TYPE.get(Identifier.parse("minecraft:iron_golem")).isPresent());
    }
}
