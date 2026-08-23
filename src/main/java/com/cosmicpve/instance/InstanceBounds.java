package com.cosmicpve.instance;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record InstanceBounds(BlockPos min, BlockPos max) {
    public static final Codec<InstanceBounds> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("min").forGetter(InstanceBounds::min),
            BlockPos.CODEC.fieldOf("max").forGetter(InstanceBounds::max)
    ).apply(instance, InstanceBounds::new));

    public InstanceBounds {
        if (min.getX() > max.getX() || min.getY() > max.getY() || min.getZ() > max.getZ()) {
            throw new IllegalArgumentException("Instance bounds are inverted");
        }
    }

    public boolean contains(BlockPos pos) {
        return pos.getX() >= min.getX() && pos.getX() <= max.getX()
                && pos.getY() >= min.getY() && pos.getY() <= max.getY()
                && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
    }

    public BoundingBox box() { return BoundingBox.fromCorners(min, max); }
    public static InstanceBounds from(BoundingBox box) {
        return new InstanceBounds(new BlockPos(box.minX(), box.minY(), box.minZ()),
                new BlockPos(box.maxX(), box.maxY(), box.maxZ()));
    }
}
