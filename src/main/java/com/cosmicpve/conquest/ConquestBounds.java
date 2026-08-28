package com.cosmicpve.conquest;

import net.minecraft.core.BlockPos;

/** Fixed event-local 20x20 horizontal protection footprint, extending through build height. */
public record ConquestBounds(int minimumX, int maximumX, int minimumZ, int maximumZ) {
    public static ConquestBounds around(BlockPos chest) {
        return new ConquestBounds(chest.getX() - 10, chest.getX() + 9,
                chest.getZ() - 10, chest.getZ() + 9);
    }

    public boolean contains(BlockPos pos) {
        return pos.getX() >= minimumX && pos.getX() <= maximumX
                && pos.getZ() >= minimumZ && pos.getZ() <= maximumZ;
    }

    public boolean overlaps(ConquestBounds other) {
        return minimumX <= other.maximumX && maximumX >= other.minimumX
                && minimumZ <= other.maximumZ && maximumZ >= other.minimumZ;
    }
}
