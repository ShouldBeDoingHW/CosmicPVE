package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.instance.structure.InstanceStructureService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class TrialProductionStructureTest {
    @Test void productionSpawnMarkersAreExplicitAndMatchTheirFloors() throws Exception {
        var rainbow=states("raiding_rainbow");
        assertEquals("minecraft:emerald_block",rainbow.get(new BlockPos(20,3,20)));
        assertEquals(List.of("minecraft:stone_bricks","minecraft:stone_bricks","minecraft:stone_bricks","minecraft:stone_bricks"),
                neighbors(rainbow,new BlockPos(20,3,20)));
        assertEquals(8,rainbow.values().stream().filter("minecraft:gold_block"::equals).count());

        var circuit=states("circuit_circus");
        assertEquals("minecraft:emerald_block",circuit.get(new BlockPos(12,3,12)));
        assertEquals(List.of("minecraft:smooth_quartz","minecraft:smooth_quartz","minecraft:smooth_quartz","minecraft:smooth_quartz"),
                neighbors(circuit,new BlockPos(12,3,12)));
        assertEquals(1,circuit.values().stream().filter("minecraft:emerald_block"::equals).count(),
                "only the explicit spawn marker remains Emerald");
        assertEquals(8,verticalBases(circuit,"minecraft:obsidian").size(),
                "the eight randomized 2x1 pillar origins use obsidian placeholders");

        var fire=states("fire_colony");
        assertEquals("minecraft:emerald_block",fire.get(new BlockPos(3,1,3)));
        assertEquals(3,neighbors(fire,new BlockPos(3,1,3)).stream().filter("minecraft:netherrack"::equals).count());
        assertEquals("minecraft:lever",fire.get(new BlockPos(43,20,7)));

        var zero=states("zero_g");
        assertEquals("minecraft:emerald_block",zero.get(new BlockPos(10,0,10)));
        assertEquals(List.of("minecraft:quartz_block","minecraft:quartz_block","minecraft:quartz_block","minecraft:quartz_block"),
                neighbors(zero,new BlockPos(10,0,10)));
        assertEquals("minecraft:water",zero.get(new BlockPos(10,1,10)));
        assertEquals(10,zero.values().stream().filter("minecraft:cherry_pressure_plate"::equals).count());
    }

    @Test void floorInferenceChoosesMostCommonStateAndNeverAirOrMarker() {
        var inferred=InstanceStructureService.inferCandidate(List.of(Blocks.STONE_BRICKS.defaultBlockState(),
                Blocks.STONE_BRICKS.defaultBlockState(),Blocks.AIR.defaultBlockState(),Blocks.EMERALD_BLOCK.defaultBlockState()));
        assertTrue(inferred.orElseThrow().is(Blocks.STONE_BRICKS));
        assertTrue(InstanceStructureService.inferCandidate(List.of(Blocks.AIR.defaultBlockState(),
                Blocks.EMERALD_BLOCK.defaultBlockState())).isEmpty());
    }

    private static List<String> neighbors(Map<BlockPos,String> states,BlockPos pos) {
        return List.of(states.get(pos.east()),states.get(pos.west()),states.get(pos.south()),states.get(pos.north()));
    }
    private static List<BlockPos> verticalBases(Map<BlockPos,String> states,String block) {
        return states.entrySet().stream().filter(entry -> entry.getValue().equals(block))
                .map(Map.Entry::getKey)
                .filter(pos -> block.equals(states.get(pos.above())) && !block.equals(states.get(pos.below())))
                .sorted().toList();
    }
    private Map<BlockPos,String> states(String name) throws Exception {
        try(var stream=getClass().getClassLoader().getResourceAsStream("data/cosmicpve/structure/trial/"+name+".nbt")) {
            assertNotNull(stream); var tag=NbtIo.readCompressed(stream,NbtAccounter.unlimitedHeap());
            var palette=tag.getListOrEmpty("palette"); var names=new ArrayList<String>();
            for(var value:palette) names.add(((net.minecraft.nbt.CompoundTag)value).getStringOr("Name",""));
            Map<BlockPos,String> result=new HashMap<>();
            for(var value:tag.getListOrEmpty("blocks")) { var block=(net.minecraft.nbt.CompoundTag)value; var p=block.getListOrEmpty("pos");
                result.put(new BlockPos(p.getIntOr(0,-1),p.getIntOr(1,-1),p.getIntOr(2,-1)),names.get(block.getIntOr("state",-1))); }
            return result;
        }
    }
}
