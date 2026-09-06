package com.cosmicpve.adventure;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import com.cosmicpve.registry.ModBlocks;

public final class AdventureExit {
    private AdventureExit() {}
    public static boolean valid(ServerLevel level,BlockPos pos) {
        for(int x=-1;x<=4;x++)for(int z=-1;z<=1;z++) {
            var p=pos.offset(x,0,z);level.getChunk(p.getX()>>4,p.getZ()>>4);
            if(!level.getBlockState(p.below()).is(net.minecraft.tags.BlockTags.DIRT))return false;
            for(int y=0;y<4;y++)if(!clearable(level,p.above(y)))return false;
        }
        var beacon=pos.offset(3,0,0);
        for(int y=beacon.getY()+1;y<level.getMaxY();y++)if(!clearable(level,new BlockPos(beacon.getX(),y,beacon.getZ())))return false;
        return true;
    }
    private static boolean clearable(ServerLevel level,BlockPos pos) {
        var state=level.getBlockState(pos);
        return state.isAir() || state.is(net.minecraft.tags.BlockTags.LEAVES) || state.canBeReplaced() && state.getFluidState().isEmpty();
    }
    public static java.util.Optional<BlockPos> find(ServerLevel level,int x,int z) {
        for(int r=0;r<=12;r++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++) {
            if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;
            level.getChunk((x+dx)>>4,(z+dz)>>4);
            int y=level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x+dx,z+dz);
            var p=new BlockPos(x+dx,y,z+dz);
            if(y>=30 && y<=131 && valid(level,p))return java.util.Optional.of(p);
        }
        return java.util.Optional.empty();
    }
    public static void place(ServerLevel level,BlockPos pos) {
        level.getChunk(pos.getX()>>4,pos.getZ()>>4);
        for(int x=-1;x<=4;x++)for(int z=-1;z<=1;z++)for(int y=0;y<4;y++) {
            var p=pos.offset(x,y,z);if(clearable(level,p) && !level.getBlockState(p).isAir())level.removeBlock(p,false);
        }
        var beam=pos.offset(3,0,0);
        for(int y=beam.getY()+1;y<level.getMaxY();y++) {
            var p=new BlockPos(beam.getX(),y,beam.getZ());if(level.getBlockState(p).is(net.minecraft.tags.BlockTags.LEAVES))level.removeBlock(p,false);
        }
        level.setBlock(pos,ModBlocks.ADVENTURE_GATEWAY.get().defaultBlockState(),3);
        level.setBlock(pos.above(),ModBlocks.ADVENTURE_GATEWAY.get().defaultBlockState(),3);
        for(int x=2;x<=4;x++)for(int z=-1;z<=1;z++)level.setBlock(pos.offset(x,-1,z),Blocks.IRON_BLOCK.defaultBlockState(),3);
        level.setBlock(pos.offset(3,0,0),Blocks.BEACON.defaultBlockState(),3);
    }
    public static void remove(ServerLevel level,BlockPos pos) {
        if(level==null)return;
        level.getChunk(pos.getX()>>4,pos.getZ()>>4);
        for(int y=0;y<2;y++)if(level.getBlockState(pos.above(y)).is(ModBlocks.ADVENTURE_GATEWAY.get()))level.removeBlock(pos.above(y),false);
        if(level.getBlockState(pos.offset(3,0,0)).is(Blocks.BEACON))level.removeBlock(pos.offset(3,0,0),false);
        for(int x=2;x<=4;x++)for(int z=-1;z<=1;z++)if(level.getBlockState(pos.offset(x,-1,z)).is(Blocks.IRON_BLOCK))
            level.setBlock(pos.offset(x,-1,z),Blocks.GRASS_BLOCK.defaultBlockState(),3);
    }
}
