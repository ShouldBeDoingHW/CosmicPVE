package com.cosmicpve.content.definition.trial;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record TrialRoomBounds(BlockPos min, BlockPos max) {
    public static final Codec<TrialRoomBounds> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("min").forGetter(TrialRoomBounds::min),
            BlockPos.CODEC.fieldOf("max").forGetter(TrialRoomBounds::max)
    ).apply(instance, TrialRoomBounds::new));

    public boolean valid() {
        return min.getX() <= max.getX() && min.getY() <= max.getY() && min.getZ() <= max.getZ();
    }

    public BoundingBox at(BlockPos origin) {
        BlockPos absoluteMin = origin.offset(min);
        BlockPos absoluteMax = origin.offset(max);
        return BoundingBox.fromCorners(absoluteMin, absoluteMax);
    }
}
