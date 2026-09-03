package com.cosmicpve.trial.room;

import java.io.InputStream;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.junit.jupiter.api.Test;

class CaveDivingStructureProbeTest {
    @Test void canonicalStructureHasExactMarkerContract() throws Exception {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(
                "data/cosmicpve/structure/trial/cave_diving.nbt")) {
            var tag = NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
            assert tag.getListOrEmpty("size").toString().equals("[34,40,31]");
            var palette = tag.getListOrEmpty("palette");
            var blocks = tag.getListOrEmpty("blocks");
            java.util.Map<String, java.util.List<net.minecraft.core.BlockPos>> found = new java.util.HashMap<>();
            for (int i = 0; i < palette.size(); i++) {
                String name = palette.getCompoundOrEmpty(i).getStringOr("Name", "");
                if (name.equals("minecraft:emerald_block") || name.equals("minecraft:diamond_block")
                        || name.equals("minecraft:gold_block") || name.equals("minecraft:bricks")
                        || name.equals("minecraft:crafting_table")) {
                    for (var value : blocks) {
                        var block = (net.minecraft.nbt.CompoundTag) value;
                        if (block.getIntOr("state", -1) == i) {
                            var pos = block.getListOrEmpty("pos");
                            found.computeIfAbsent(name, ignored -> new java.util.ArrayList<>()).add(
                                    new net.minecraft.core.BlockPos(pos.getIntOr(0,-1),pos.getIntOr(1,-1),pos.getIntOr(2,-1)));
                        }
                    }
                }
            }
            org.junit.jupiter.api.Assertions.assertEquals(java.util.List.of(CaveDivingService.SPAWN_MARKER_LOCAL),
                    found.get("minecraft:emerald_block"));
            org.junit.jupiter.api.Assertions.assertEquals(java.util.List.of(CaveDivingService.MODEL_MARKER_LOCAL),
                    found.get("minecraft:diamond_block"));
            org.junit.jupiter.api.Assertions.assertEquals(java.util.List.of(new net.minecraft.core.BlockPos(17,30,8),
                    CaveDivingService.SOLUTION_MARKER_LOCAL), found.get("minecraft:gold_block"));
            org.junit.jupiter.api.Assertions.assertEquals(new java.util.HashSet<>(CaveDivingService.UNDERWATER_MARKERS_LOCAL),
                    new java.util.HashSet<>(found.get("minecraft:bricks")));
            org.junit.jupiter.api.Assertions.assertEquals(java.util.List.of(CaveDivingService.CRAFTING_TABLE_LOCAL),
                    found.get("minecraft:crafting_table"));
        }
    }
}
