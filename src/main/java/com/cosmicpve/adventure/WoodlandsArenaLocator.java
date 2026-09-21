package com.cosmicpve.adventure;

import com.cosmicpve.CosmicPVE;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.block.Blocks;

/**
 * Bounded arena placement lookup. Vanilla /locate may synchronously scan an
 * ineligible current dimension; this reads the Dense Woodlands random-spread grid
 * directly, resolves only nearby structure-start stages, and fully generates only
 * the bounded footprint of the first real arena start so the returned landmark is
 * guaranteed to exist in the world.
 */
public final class WoodlandsArenaLocator {
    public static final ResourceKey<StructureSet> ARENA_SET = ResourceKey.create(
            Registries.STRUCTURE_SET, CosmicPVE.id("woodlands_arena"));
    private static final int NEIGHBOR_REGIONS = 2;
    private static final int[] RECOVERY_REGION_OFFSETS = {4, 8, 32, 128};

    private WoodlandsArenaLocator() {}

    public static Optional<BlockPos> nearestCandidate(ServerLevel level, BlockPos origin) {
        var set = level.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET).getValue(ARENA_SET);
        if (set == null || !(set.placement() instanceof RandomSpreadStructurePlacement placement)) {
            return Optional.empty();
        }
        return Optional.of(nearestCandidate(placement, level.getSeed(), origin));
    }

    /** Resolves a real structure start and verifies its authored lectern after bounded chunk generation. */
    public static Optional<LocatedArena> nearestGenerated(ServerLevel level, BlockPos origin) {
        var sets = level.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET);
        var set = sets.getValue(ARENA_SET);
        var structures = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        var structure = structures.getValue(com.cosmicpve.adventure.ranger.WoodlandsArenaService.STRUCTURE);
        if (set == null || structure == null
                || !(set.placement() instanceof RandomSpreadStructurePlacement placement)) return Optional.empty();
        int checked = 0;
        for (BlockPos candidate : candidates(placement, level.getSeed(), origin)) {
            checked++;
            var chunkPos = new ChunkPos(candidate);
            var access = level.getChunkSource().getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS, true);
            if (access == null) continue;
            var start = access.getStartForStructure(structure);
            if (start == null || !start.isValid()) continue;
            var bounds = start.getBoundingBox();
            for (int chunkX = Math.floorDiv(bounds.minX(), 16); chunkX <= Math.floorDiv(bounds.maxX(), 16); chunkX++) {
                for (int chunkZ = Math.floorDiv(bounds.minZ(), 16); chunkZ <= Math.floorDiv(bounds.maxZ(), 16); chunkZ++) {
                    level.getChunk(chunkX, chunkZ);
                }
            }
            for (BlockPos position : BlockPos.betweenClosed(bounds.minX(), bounds.minY(), bounds.minZ(),
                    bounds.maxX(), bounds.maxY(), bounds.maxZ())) {
                if (level.getBlockState(position).is(Blocks.LECTERN)) {
                    if (checked > 1) CosmicPVE.LOGGER.warn(
                            "Woodlands Arena locator skipped {} finalized candidate chunks without an arena start before recovering at {}",
                            checked - 1, position);
                    return Optional.of(new LocatedArena(bounds, position.immutable(), checked));
                }
            }
        }
        return Optional.empty();
    }

    static BlockPos nearestCandidate(RandomSpreadStructurePlacement placement, long seed, BlockPos origin) {
        return nearbyCandidates(placement, seed, origin).getFirst();
    }

    static List<BlockPos> candidates(RandomSpreadStructurePlacement placement, long seed, BlockPos origin) {
        var candidates = new ArrayList<>(nearbyCandidates(placement, seed, origin));
        int originChunkX = Math.floorDiv(origin.getX(), 16);
        int originChunkZ = Math.floorDiv(origin.getZ(), 16);
        int regionX = Math.floorDiv(originChunkX, placement.spacing());
        int regionZ = Math.floorDiv(originChunkZ, placement.spacing());
        for (int offset : RECOVERY_REGION_OFFSETS) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    candidates.add(candidate(placement, seed, regionX + dx * offset, regionZ + dz * offset));
                }
            }
        }
        return List.copyOf(candidates);
    }

    static List<BlockPos> nearbyCandidates(RandomSpreadStructurePlacement placement, long seed, BlockPos origin) {
        int originChunkX = Math.floorDiv(origin.getX(), 16);
        int originChunkZ = Math.floorDiv(origin.getZ(), 16);
        int regionX = Math.floorDiv(originChunkX, placement.spacing());
        int regionZ = Math.floorDiv(originChunkZ, placement.spacing());
        var candidates = new ArrayList<BlockPos>();
        for (int dx = -NEIGHBOR_REGIONS; dx <= NEIGHBOR_REGIONS; dx++) {
            for (int dz = -NEIGHBOR_REGIONS; dz <= NEIGHBOR_REGIONS; dz++) {
                candidates.add(candidate(placement, seed, regionX + dx, regionZ + dz));
            }
        }
        candidates.sort(Comparator.comparingLong(candidate -> {
            long x = (long) candidate.getX() - origin.getX();
            long z = (long) candidate.getZ() - origin.getZ();
            return x * x + z * z;
        }));
        return List.copyOf(candidates);
    }

    private static BlockPos candidate(RandomSpreadStructurePlacement placement, long seed, int regionX, int regionZ) {
        ChunkPos chunk = placement.getPotentialStructureChunk(seed,
                regionX * placement.spacing(), regionZ * placement.spacing());
        return placement.getLocatePos(chunk);
    }

    public record LocatedArena(BoundingBox bounds, BlockPos lectern, int checkedCandidates) {
        public BlockPos center() { return bounds.getCenter(); }
    }
}
