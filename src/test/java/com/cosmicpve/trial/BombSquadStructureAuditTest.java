package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.trial.room.BombSquadService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.junit.jupiter.api.Test;

class BombSquadStructureAuditTest {
    @Test void canonicalDimensionsMarkersAndPhysicalBlastBoundaryArePresent() throws Exception {
        Structure structure = structure();
        assertEquals(List.of(45, 21, 45), structure.size());
        assertEquals(BombSquadService.SPAWN_MARKERS_LOCAL, structure.sorted("minecraft:emerald_block"));
        assertEquals(BombSquadService.SUPPLY_PLATES_LOCAL, structure.sorted("minecraft:light_weighted_pressure_plate"));
        assertEquals(BombSquadService.EXIT_MARKERS_LOCAL, structure.sorted("minecraft:diamond_block"));
        assertEquals(1200, structure.count("minecraft:stone"));
        assertEquals(1936, structure.count("minecraft:obsidian"));
        assertEquals(2008, structure.count("minecraft:bedrock"));
        assertEquals(3698, structure.count("minecraft:gray_stained_glass"));
        assertEquals(3698, structure.count("minecraft:light_gray_stained_glass"));
        assertEquals(5547, structure.count("minecraft:black_stained_glass"));
        assertEquals(4313, structure.count("minecraft:sea_lantern"));
    }

    @Test void allFourStartsHaveAtLeastTwoDistantExitCandidates() {
        for (BlockPos spawn : BombSquadService.SPAWN_MARKERS_LOCAL) {
            var start = BombSquadService.gridCell(spawn);
            long eligible = BombSquadService.EXIT_MARKERS_LOCAL.stream().map(BombSquadService::gridCell)
                    .filter(cell -> Math.max(Math.abs(cell.column() - start.column()),
                            Math.abs(cell.row() - start.row())) > 1).count();
            assertTrue(eligible >= 2, spawn + " has only " + eligible + " distant exits");
        }
    }

    private Structure structure() throws Exception {
        try (var stream = getClass().getClassLoader().getResourceAsStream(
                "data/cosmicpve/structure/trial/bomb_squad.nbt")) {
            assertNotNull(stream);
            CompoundTag tag = NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
            var palette = tag.getListOrEmpty("palette"); var names = new ArrayList<String>();
            for (var value : palette) names.add(((CompoundTag) value).getStringOr("Name", ""));
            Map<String,List<BlockPos>> positions = new HashMap<>();
            for (var value : tag.getListOrEmpty("blocks")) {
                CompoundTag block=(CompoundTag)value; var pos=block.getListOrEmpty("pos");
                BlockPos p=new BlockPos(pos.getIntOr(0,-1),pos.getIntOr(1,-1),pos.getIntOr(2,-1));
                positions.computeIfAbsent(names.get(block.getIntOr("state",-1)), ignored->new ArrayList<>()).add(p);
            }
            var sizeTag=tag.getListOrEmpty("size");
            return new Structure(List.of(sizeTag.getIntOr(0,-1),sizeTag.getIntOr(1,-1),sizeTag.getIntOr(2,-1)),positions);
        }
    }

    private record Structure(List<Integer> size, Map<String,List<BlockPos>> positions) {
        int count(String name) { return positions.getOrDefault(name,List.of()).size(); }
        List<BlockPos> sorted(String name) { return positions.getOrDefault(name,List.of()).stream().sorted(
                java.util.Comparator.comparingInt((BlockPos pos) -> pos.getX()).thenComparingInt(BlockPos::getY)
                        .thenComparingInt(BlockPos::getZ)).toList(); }
    }
}
