package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.LinkedHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.junit.jupiter.api.Test;

final class Step7GStructureAuditTest {
    @Test
    void auditSuppliedStructures() throws IOException {
        var haze = audit("haze_seek");
        assertEquals(1, haze.get("minecraft:emerald_block"));
        assertEquals(9, haze.get("minecraft:gold_block"));
        assertEquals(1, haze.get("minecraft:diamond_block"));
        var west = audit("warzone_giants_west");
        var east = audit("warzone_giants_east");
        assertEquals(1, west.get("minecraft:emerald_block"));
        for (String color : java.util.List.of("red", "orange", "yellow", "lime", "cyan", "purple")) {
            assertTrue(west.getOrDefault("minecraft:" + color + "_wool", 0) > 0);
            assertTrue(east.getOrDefault("minecraft:" + color + "_wool", 0) > 0);
        }
    }

    private java.util.Map<String, Integer> audit(String name) throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream(
                "data/cosmicpve/structure/trial/" + name + ".nbt")) {
            assertNotNull(stream, name);
            CompoundTag root = NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
            ListTag palette = root.getListOrEmpty("palette");
            ListTag blocks = root.getListOrEmpty("blocks");
            var counts = new LinkedHashMap<String, Integer>();
            for (int i = 0; i < blocks.size(); i++) {
                CompoundTag block = blocks.getCompoundOrEmpty(i);
                int state = block.getIntOr("state", -1);
                String id = state >= 0 && state < palette.size()
                        ? palette.getCompoundOrEmpty(state).getStringOr("Name", "unknown") : "unknown";
                counts.merge(id, 1, Integer::sum);
            }
            return counts;
        }
    }
}
