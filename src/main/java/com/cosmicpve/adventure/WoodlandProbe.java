package com.cosmicpve.adventure;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.*;

/** Permissioned runtime probe: measures generated ground, not generator estimates or tree canopy. */
public final class WoodlandProbe {
    private WoodlandProbe() {}
    public record Result(int columns,int min,int max,double mean,int median,double softPercent,int maxNeighborSlope,double meanNeighborSlope,
            Map<String,Long> placements) {}
    public static Result sample(ServerLevel level,int chunkX,int chunkZ,int width) {
        var before=WoodlandTemplateFeature.counts();var heights=new int[width*16][width*16];var values=new ArrayList<Integer>();
        for(int cx=0;cx<width;cx++)for(int cz=0;cz<width;cz++)level.getChunk(chunkX+cx,chunkZ+cz);
        for(int x=0;x<width*16;x++)for(int z=0;z<width*16;z++) {
            int wx=chunkX*16+x,wz=chunkZ*16+z;
            int h=level.getHeight(Heightmap.Types.WORLD_SURFACE,wx,wz)-1;
            // Terrain surface excludes authored logs/leaves/props; scan to the actual soil layer.
            while(h>level.getMinY() && !level.getBlockState(new BlockPos(wx,h,wz)).is(BlockTags.DIRT))h--;
            heights[x][z]=h;values.add(h);
        }
        long slopeTotal=0;int slopes=0,maxSlope=0;
        for(int x=0;x<width*16;x++)for(int z=0;z<width*16;z++) {
            if(x>0){int d=Math.abs(heights[x][z]-heights[x-1][z]);slopeTotal+=d;slopes++;maxSlope=Math.max(maxSlope,d);}
            if(z>0){int d=Math.abs(heights[x][z]-heights[x][z-1]);slopeTotal+=d;slopes++;maxSlope=Math.max(maxSlope,d);}
        }
        Collections.sort(values);var counts=new TreeMap<String,Long>();
        for(var placement:WoodlandTemplateFeature.recent()) {
            var p=placement.origin();
            if(p.getX()>=chunkX*16 && p.getX()<(chunkX+width)*16 && p.getZ()>=chunkZ*16 && p.getZ()<(chunkZ+width)*16)
                counts.merge(placement.variant(),1L,Long::sum);
        }
        return new Result(values.size(),values.getFirst(),values.getLast(),values.stream().mapToInt(i->i).average().orElseThrow(),
                values.get(values.size()/2),100D*values.stream().filter(y->y>=50&&y<=110).count()/values.size(),maxSlope,1D*slopeTotal/slopes,counts);
    }
}
