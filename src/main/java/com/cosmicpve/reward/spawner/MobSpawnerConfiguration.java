package com.cosmicpve.reward.spawner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

/** Owns the complete vanilla spawner configuration/update boundary used after placement. */
public final class MobSpawnerConfiguration {
    private MobSpawnerConfiguration() {}

    public static void configure(SpawnerBlockEntity spawner, EntityType<?> entityType,
            ServerLevel level, BlockPos position) {
        configureSpawnData(spawner.getSpawner(), entityType, level, level.getRandom(), position);
        spawner.setChanged();
        level.blockEntityChanged(position);
        level.sendBlockUpdated(position, spawner.getBlockState(), spawner.getBlockState(), 3);
    }

    static void configureSpawnData(BaseSpawner spawner, EntityType<?> entityType,
            Level level, RandomSource random, BlockPos position) {
        spawner.setEntityId(entityType, level, random, position);
    }
}
