package com.cosmicpve.trial.room;

import com.cosmicpve.instance.InstanceBounds;
import com.cosmicpve.trial.TrialEncounterState;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class CircuitCircusService {
    public static final BlockPos LEVER_LOCAL = new BlockPos(12, 5, 6);
    private static final int TARGET_MIN_LOCAL_Y = 15;

    public TrialEncounterState initialize(ServerLevel level, BlockPos origin, InstanceBounds bounds, RandomSource random) {
        List<BlockPos> bases = pillarBases(level, bounds);
        if (bases.size() != 8) throw new IllegalStateException("Circuit Circus requires eight pillar bases, found " + bases.size());
        var assignments = new ArrayList<>(assignmentPool());
        for (int i = assignments.size() - 1; i > 0; i--) java.util.Collections.swap(assignments, i, random.nextInt(i + 1));
        for (int i = 0; i < bases.size(); i++) {
            var state = assignments.get(i).pillar().defaultBlockState();
            level.setBlock(bases.get(i), state, 3); level.setBlock(bases.get(i).above(), state, 3);
        }
        for (BlockPos pos : BlockPos.betweenClosed(bounds.min(), bounds.max())) {
            if (pos.getY() < origin.getY() + TARGET_MIN_LOCAL_Y
                    && CircuitMaterial.fromGlass(level.getBlockState(pos).getBlock()) != null) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        return new TrialEncounterState(List.of(), 0,
                assignments.stream().map(CircuitMaterial::serialized).toList(), List.of(), List.of());
    }

    public TargetResult hitTarget(ServerLevel level, ServerPlayer player, BlockPos origin, BlockPos target,
            TrialEncounterState state, RandomSource random) {
        CircuitMaterial material = CircuitMaterial.fromGlass(level.getBlockState(target).getBlock());
        BlockPos local = target.subtract(origin);
        if (material == null || local.getY() < TARGET_MIN_LOCAL_Y || state.claimedTargets().contains(local))
            return new TargetResult(false, state, 0, null);
        level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
        int quantity = random.nextBoolean() ? 2 : 1;
        player.getInventory().placeItemBackInInventory(new ItemStack(material.glassItem(), quantity));
        var claimed = new ArrayList<>(state.claimedTargets()); claimed.add(local.immutable());
        return new TargetResult(true, new TrialEncounterState(state.hiddenSequence(), state.sequenceProgress(),
                state.pillarMaterials(), claimed, state.completedCircuits()), quantity, material);
    }

    public ValidationResult validateAfterPlacement(ServerLevel level, InstanceBounds bounds,
            TrialEncounterState state, Block placed) {
        CircuitMaterial material = CircuitMaterial.fromGlass(placed);
        if (material == null || state.completedCircuits().contains(material.serialized()) || !connected(level, bounds, material))
            return new ValidationResult(false, state, material);
        var completed = new ArrayList<>(state.completedCircuits()); completed.add(material.serialized());
        return new ValidationResult(true, new TrialEncounterState(state.hiddenSequence(), state.sequenceProgress(),
                state.pillarMaterials(), state.claimedTargets(), completed), material);
    }

    public static boolean allowsPlacement(InstanceBounds bounds, BlockPos pos, BlockState state) {
        return bounds.contains(pos) && pos.getY() >= bounds.min().getY() + 3
                && pos.getY() < bounds.min().getY() + TARGET_MIN_LOCAL_Y
                && CircuitMaterial.fromGlass(state.getBlock()) != null;
    }

    public boolean allComplete(TrialEncounterState state) { return state.completedCircuits().size() == 4; }

    public static List<CircuitMaterial> assignmentPool() {
        var result = new ArrayList<CircuitMaterial>();
        for (var material : CircuitMaterial.values()) { result.add(material); result.add(material); }
        return List.copyOf(result);
    }

    private boolean connected(ServerLevel level, InstanceBounds bounds, CircuitMaterial material) {
        List<BlockPos> bases = pillarBases(level, bounds).stream()
                .filter(pos -> level.getBlockState(pos).is(material.pillar())).toList();
        if (bases.size() != 2) return false;
        Set<BlockPos> glass = new HashSet<>();
        for (BlockPos pos : BlockPos.betweenClosed(bounds.min(), bounds.max()))
            if (level.getBlockState(pos).is(material.glass())) glass.add(pos.immutable());
        return orthogonallyConnected(glass, adjacentToPillar(bases.get(0)), adjacentToPillar(bases.get(1)), bounds);
    }

    public static boolean orthogonallyConnected(Set<BlockPos> glass, Set<BlockPos> starts,
            Set<BlockPos> goals, InstanceBounds bounds) {
        var queue = new ArrayDeque<BlockPos>(); var visited = new HashSet<BlockPos>();
        for (BlockPos start : starts)
            if (bounds.contains(start) && glass.contains(start)) { queue.add(start); visited.add(start); }
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst(); if (goals.contains(current)) return true;
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (bounds.contains(next) && visited.add(next) && glass.contains(next)) queue.add(next);
            }
        }
        return false;
    }

    private static Set<BlockPos> adjacentToPillar(BlockPos base) {
        Set<BlockPos> result = new HashSet<>();
        for (int y = 0; y <= 1; y++) for (Direction direction : Direction.values())
            result.add(base.above(y).relative(direction));
        return result;
    }

    private static List<BlockPos> pillarBases(ServerLevel level, InstanceBounds bounds) {
        var result = new ArrayList<BlockPos>();
        for (BlockPos mutable : BlockPos.betweenClosed(bounds.min(), bounds.max())) {
            BlockPos pos = mutable.immutable(); Block block = level.getBlockState(pos).getBlock();
            boolean pillar = java.util.Arrays.stream(CircuitMaterial.values()).anyMatch(value -> value.pillar() == block);
            if (pillar && level.getBlockState(pos.above()).is(block) && !level.getBlockState(pos.below()).is(block)) result.add(pos);
        }
        result.sort(BlockPos::compareTo); return result;
    }

    public record TargetResult(boolean accepted, TrialEncounterState state, int quantity, CircuitMaterial material) {}
    public record ValidationResult(boolean newlyCompleted, TrialEncounterState state, CircuitMaterial material) {}
}
