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
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
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
        assertTrue(MobSpawnerEligibility.isEligible(Identifier.parse("minecraft:blaze")));
        assertTrue(MobSpawnerEligibility.isEligible(Identifier.parse("minecraft:iron_golem")));
        assertFalse(MobSpawnerEligibility.isEligible(Identifier.parse("minecraft:arrow")));
        assertFalse(MobSpawnerEligibility.isEligible(Identifier.parse("minecraft:player")));
    }

    @Test void vanillaConfigurationSeamWritesCreeperSpawnDataAndKeepsNormalTimings() throws Exception {
        BlockPos position = new BlockPos(4, 5, 6);
        var blockEntity = new SpawnerBlockEntity(position, Blocks.SPAWNER.defaultBlockState());
        MobSpawnerConfiguration.configureSpawnData(blockEntity.getSpawner(), EntityType.CREEPER,
                null, RandomSource.create(42), position);

        var spawnDataField = net.minecraft.world.level.BaseSpawner.class.getDeclaredField("nextSpawnData");
        spawnDataField.setAccessible(true);
        SpawnData spawnData = (SpawnData) spawnDataField.get(blockEntity.getSpawner());
        assertEquals("minecraft:creeper", spawnData.getEntityToSpawn().getString("id").orElseThrow());
        assertEquals(20, integerField(blockEntity, "spawnDelay"));
        assertEquals(200, integerField(blockEntity, "minSpawnDelay"));
        assertEquals(800, integerField(blockEntity, "maxSpawnDelay"));
        assertEquals(4, integerField(blockEntity, "spawnCount"));
        assertEquals(16, integerField(blockEntity, "requiredPlayerRange"));
    }

    @Test void representativeSpawnerTypesAllUseTheSameCompleteVanillaConfigurationPath() throws Exception {
        for (EntityType<?> type : new EntityType<?>[]{EntityType.BLAZE, EntityType.IRON_GOLEM, EntityType.CREEPER,
                EntityType.ZOMBIE, EntityType.PIG, EntityType.SHEEP}) {
            BlockPos position = new BlockPos(4, 5, 6);
            var blockEntity = new SpawnerBlockEntity(position, Blocks.SPAWNER.defaultBlockState());
            MobSpawnerConfiguration.configureSpawnData(blockEntity.getSpawner(), type,
                    null, RandomSource.create(42), position);
            var spawnDataField = net.minecraft.world.level.BaseSpawner.class.getDeclaredField("nextSpawnData");
            spawnDataField.setAccessible(true);
            SpawnData spawnData = (SpawnData) spawnDataField.get(blockEntity.getSpawner());
            assertEquals(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString(),
                    spawnData.getEntityToSpawn().getString("id").orElseThrow());
            assertEquals(20, integerField(blockEntity, "spawnDelay"));
            assertEquals(200, integerField(blockEntity, "minSpawnDelay"));
            assertEquals(800, integerField(blockEntity, "maxSpawnDelay"));
            assertEquals(4, integerField(blockEntity, "spawnCount"));
            assertEquals(6, integerField(blockEntity, "maxNearbyEntities"));
            assertEquals(16, integerField(blockEntity, "requiredPlayerRange"));
            assertEquals(4, integerField(blockEntity, "spawnRange"));
        }
    }

    @Test void observedWorkingPatternMatchesPinnedVanillaSpawnPlacementPredicates() {
        assertTrue(net.minecraft.world.entity.SpawnPlacements.hasPlacement(EntityType.BLAZE));
        assertTrue(net.minecraft.world.entity.SpawnPlacements.hasPlacement(EntityType.IRON_GOLEM));
        assertTrue(net.minecraft.world.entity.SpawnPlacements.hasPlacement(EntityType.CREEPER));
        assertTrue(net.minecraft.world.entity.SpawnPlacements.hasPlacement(EntityType.ZOMBIE));
        assertTrue(net.minecraft.world.entity.SpawnPlacements.hasPlacement(EntityType.PIG));
        assertTrue(net.minecraft.world.entity.SpawnPlacements.hasPlacement(EntityType.SHEEP));
        assertFalse(net.minecraft.world.entity.EntitySpawnReason.ignoresLightRequirements(
                net.minecraft.world.entity.EntitySpawnReason.SPAWNER));
    }

    private static int integerField(SpawnerBlockEntity blockEntity, String name) throws Exception {
        var field = net.minecraft.world.level.BaseSpawner.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(blockEntity.getSpawner());
    }
}
