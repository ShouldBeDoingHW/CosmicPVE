package com.cosmicpve.trial.room;

import com.cosmicpve.instance.InstanceBounds;
import com.cosmicpve.trial.TrialSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Bounded Fire Colony objective and hazard-baseline guard; no global gamerules are changed. */
public final class FireColonyService {
    public static final BlockPos LEVER_LOCAL = new BlockPos(43, 20, 7);
    private final Map<UUID, Map<BlockPos, BlockState>> hazardBaselines = new HashMap<>();

    public void initialize(ServerLevel level, TrialSession session, InstanceBounds bounds) {
        Map<BlockPos, BlockState> baseline = new HashMap<>();
        for (BlockPos cursor : BlockPos.betweenClosed(bounds.min(), bounds.max())) {
            BlockPos pos = cursor.immutable();
            BlockState state = level.getBlockState(pos);
            if (!state.is(Blocks.FIRE) && !state.is(Blocks.SOUL_FIRE)
                    && !state.is(Blocks.LAVA) && state.getFluidState().isEmpty()) continue;
            baseline.put(pos, state);
            for (var direction : net.minecraft.core.Direction.values()) {
                BlockPos neighbor = pos.relative(direction);
                if (bounds.contains(neighbor)) baseline.putIfAbsent(neighbor, level.getBlockState(neighbor));
            }
        }
        hazardBaselines.put(session.sessionId(), Map.copyOf(baseline));
    }

    public void tick(ServerLevel level, TrialSession session) {
        Map<BlockPos, BlockState> baseline = hazardBaselines.get(session.sessionId());
        if (baseline == null) return;
        baseline.forEach((pos, expected) -> {
            if (!level.getBlockState(pos).equals(expected)) level.setBlock(pos, expected, 2);
        });
    }

    public boolean isFinalLever(BlockPos worldPos, BlockPos origin) {
        return worldPos.equals(origin.offset(LEVER_LOCAL));
    }

    public void cleanup(UUID sessionId) { hazardBaselines.remove(sessionId); }
    public int monitoredPositions(UUID sessionId) {
        return hazardBaselines.getOrDefault(sessionId, Map.of()).size();
    }
}
