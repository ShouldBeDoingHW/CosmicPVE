package com.cosmicpve.instance.structure;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.content.definition.trial.TrialRoomDefinition;
import com.cosmicpve.instance.InstanceBounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;

public final class InstanceStructureService {
    public InstanceStructurePlacement place(ServerLevel level, TrialRoomDefinition definition, BlockPos origin) {
        BoundingBox combined = null;
        var markers = new ArrayList<BlockPos>();
        for (var piece : definition.pieces()) {
            var template = level.getStructureManager().get(piece.structure()).orElseThrow(
                    () -> new IllegalStateException("Missing Trial structure " + piece.structure()));
            BlockPos pieceOrigin = origin.offset(piece.offset());
            var settings = new StructurePlaceSettings().setRotation(piece.rotation().minecraft());
            BoundingBox pieceBounds = template.getBoundingBox(settings, pieceOrigin);
            if (!template.placeInWorld(level, pieceOrigin, pieceOrigin, settings, RandomSource.create(), 2)) {
                throw new IllegalStateException("Could not place Trial structure " + piece.structure());
            }
            markers.addAll(template.filterBlocks(pieceOrigin, settings, Blocks.EMERALD_BLOCK).stream()
                    .map(info -> info.pos()).toList());
            if (combined == null) combined = pieceBounds;
            else combined.encapsulate(pieceBounds);
        }
        BlockPos marker = definition.spawnMarkerPosition().map(origin::offset).orElseGet(() -> {
            if (markers.size() != 1) throw new IllegalStateException("Trial room " + definition.id()
                    + " requires one resolved Emerald spawn marker, found " + markers.size());
            return markers.getFirst();
        });
        if (!level.getBlockState(marker).is(Blocks.EMERALD_BLOCK)) {
            throw new IllegalStateException("Resolved spawn marker for " + definition.id()
                    + " is not an Emerald Block: " + marker);
        }
        level.setBlock(marker, replacement(level, marker, definition), 3);
        InstanceBounds declared = InstanceBounds.from(definition.bounds().at(origin));
        return new InstanceStructurePlacement(declared, marker.above());
    }

    private static net.minecraft.world.level.block.state.BlockState replacement(
            ServerLevel level, BlockPos marker, TrialRoomDefinition definition) {
        if (definition.spawnMarkerReplacement().isPresent()) {
            return BuiltInRegistries.BLOCK.get(definition.spawnMarkerReplacement().orElseThrow())
                    .orElseThrow(() -> new IllegalStateException("Unknown spawn marker replacement for " + definition.id()))
                    .value().defaultBlockState();
        }
        return inferredFloorReplacement(level, marker, definition.id().toString());
    }

    /** Resolves the same deterministic floor used by consumed structure markers in bespoke multi-marker rooms. */
    public static net.minecraft.world.level.block.state.BlockState inferredFloorReplacement(
            ServerLevel level, BlockPos marker, String context) {
        var candidates = new ArrayList<net.minecraft.world.level.block.state.BlockState>();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = marker.relative(direction);
            var state = level.getBlockState(neighbor);
            if (suitableFloor(level, neighbor, state)) candidates.add(state);
        }
        var inferred = inferCandidate(candidates);
        if (inferred.isPresent()) return inferred.orElseThrow();
        var below = level.getBlockState(marker.below());
        if (suitableFloor(level, marker.below(), below)) return below;
        CosmicPVE.LOGGER.warn("Using smooth-stone fallback for unresolved Trial marker floor in {} at {}",
                context, marker);
        return Blocks.SMOOTH_STONE.defaultBlockState();
    }

    public static java.util.Optional<net.minecraft.world.level.block.state.BlockState> inferCandidate(
            java.util.List<net.minecraft.world.level.block.state.BlockState> candidates) {
        Map<net.minecraft.world.level.block.state.BlockState, Integer> counts = new HashMap<>();
        candidates.stream().filter(state -> !state.isAir() && state.getFluidState().isEmpty()
                && !state.is(Blocks.EMERALD_BLOCK)).forEach(state -> counts.merge(state, 1, Integer::sum));
        return counts.entrySet().stream()
                .sorted(Map.Entry.<net.minecraft.world.level.block.state.BlockState, Integer>comparingByValue().reversed()
                        .thenComparing(entry -> entry.getKey().toString()))
                .map(Map.Entry::getKey).findFirst();
    }

    private static boolean suitableFloor(ServerLevel level, BlockPos pos,
            net.minecraft.world.level.block.state.BlockState state) {
        return !state.isAir() && state.getFluidState().isEmpty() && !state.is(Blocks.EMERALD_BLOCK)
                && !state.getCollisionShape(level, pos).isEmpty();
    }

    public void cleanup(ServerLevel level, InstanceBounds bounds) {
        AABB area = new AABB(bounds.min().getX(), bounds.min().getY(), bounds.min().getZ(),
                bounds.max().getX() + 1.0, bounds.max().getY() + 1.0, bounds.max().getZ() + 1.0);
        level.getEntities((Entity)null, area, entity -> !(entity instanceof Player)).forEach(Entity::discard);
        for (BlockPos pos : BlockPos.betweenClosed(bounds.min(), bounds.max())) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }
}
