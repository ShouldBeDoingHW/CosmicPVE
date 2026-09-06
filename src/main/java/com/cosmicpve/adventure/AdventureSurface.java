package com.cosmicpve.adventure;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.Optional;

public final class AdventureSurface {
    private AdventureSurface() {}
    public static Optional<BlockPos> nearby(ServerLevel level, int x, int z, int radius) {
        for(int r=0;r<=radius;r++) for(int dx=-r;dx<=r;dx++) for(int dz=-r;dz<=r;dz++) {
            if(Math.max(Math.abs(dx),Math.abs(dz))!=r) continue;
            int px=x+dx,pz=z+dz;
            // ServerLevel.getHeight may return minY for an absent chunk: explicitly request FULL first.
            level.getChunk(px>>4,pz>>4);
            int y=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,px,pz);
            BlockPos feet=new BlockPos(px,y,pz);
            if(safe(level,feet)) return Optional.of(feet);
        }
        return Optional.empty();
    }
    public static boolean safe(ServerLevel level, BlockPos feet) {
        var floor=level.getBlockState(feet.below());
        if(feet.getY()<30 || feet.getY()>131 || !floor.is(BlockTags.DIRT)
                || !floor.isCollisionShapeFullBlock(level,feet.below())) return false;
        for(int y=0;y<=1;y++) {
            var p=feet.above(y);var state=level.getBlockState(p);
            if(!state.getCollisionShape(level,p).isEmpty() || state.is(BlockTags.LEAVES)
                    || !state.getFluidState().isEmpty()) return false;
        }
        // At least a compact walkable area, without an immediate drop off an isolated column.
        for(var direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            var p=feet.relative(direction);level.getChunk(p.getX()>>4,p.getZ()>>4);
            boolean nearbyGround=false;
            for(int dy=-2;dy<=2;dy++)if(level.getBlockState(p.below().above(dy)).is(BlockTags.DIRT))nearbyGround=true;
            if(!nearbyGround)return false;
        }
        return true;
    }
}
