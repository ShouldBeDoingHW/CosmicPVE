package com.cosmicpve.trial.room;

import com.cosmicpve.instance.InstanceBounds;
import com.cosmicpve.trial.TrialLifecycleState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Narrow authoritative exception evaluated before protected-instance default denial. */
public final class CircuitPlacementPolicy {
    private CircuitPlacementPolicy() {}

    public static boolean allows(boolean activeParticipant, boolean circuitRoom,
            TrialLifecycleState state, InstanceBounds buildBounds, BlockPos position, BlockState placedState) {
        return activeParticipant && circuitRoom && state == TrialLifecycleState.ROOM_ACTIVE
                && CircuitCircusService.allowsPlacement(buildBounds, position, placedState);
    }
}
