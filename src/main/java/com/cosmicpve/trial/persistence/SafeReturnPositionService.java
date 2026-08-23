package com.cosmicpve.trial.persistence;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class SafeReturnPositionService {
    public Optional<Vec3> resolve(ServerLevel level, double x, double y, double z) {
        BlockPos requested = BlockPos.containing(x, y, z);
        if (safe(level, requested)) return Optional.of(new Vec3(x, y, z));
        for (int dy = 0; dy <= 4; dy++) {
            for (int radius = 0; radius <= 3; radius++) {
                for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
                    for (int sign : new int[]{1, -1}) {
                        BlockPos candidate = requested.offset(dx, dy * sign, dz);
                        if (safe(level, candidate)) return Optional.of(Vec3.atBottomCenterOf(candidate));
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static boolean safe(ServerLevel level, BlockPos feet) {
        return level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                && !level.getBlockState(feet.below()).getCollisionShape(level, feet.below()).isEmpty();
    }
}
