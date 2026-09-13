package com.cosmicpve.entity.woodlands;

import com.cosmicpve.adventure.DenseWoodlandsSessionService;
import com.cosmicpve.registry.ModEntities;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

public final class DenseWoodlandsMobSpawns {
    public static final int FANATIC_WEIGHT = 70, FANATIC_MIN = 1, FANATIC_MAX = 3;
    public static final int DREADMANE_WEIGHT = 30, DREADMANE_MIN = 1, DREADMANE_MAX = 2;

    private DenseWoodlandsMobSpawns() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(DenseWoodlandsMobSpawns::registerPlacements);
        NeoForge.EVENT_BUS.addListener(DenseWoodlandsMobSpawns::suppressVanillaNaturalHostiles);
    }

    private static void registerPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.FOREST_FANATIC.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (type, level, reason, pos, random) -> true,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.DREADMANE.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (type, level, reason, pos, random) -> true,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    public static boolean shouldSuppress(net.minecraft.world.entity.EntityType<?> type,
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension, EntitySpawnReason reason) {
        return reason == EntitySpawnReason.NATURAL && dimension.equals(DenseWoodlandsSessionService.DIMENSION)
                && type.getCategory() == MobCategory.MONSTER
                && type != ModEntities.FOREST_FANATIC.get() && type != ModEntities.DREADMANE.get();
    }

    private static void suppressVanillaNaturalHostiles(MobSpawnEvent.SpawnPlacementCheck event) {
        var level = event.getLevel().getLevel();
        if (shouldSuppress(event.getEntityType(), level.dimension(), event.getSpawnType()))
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);
    }
}
