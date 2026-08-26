package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.cosmicpve.trial.room.HiddenGraveyardService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.junit.jupiter.api.Test;

class HiddenGraveyardStructureAuditTest {
    @Test void auditCanonicalAuthoredGeometry() throws Exception {
        try (var stream = getClass().getClassLoader().getResourceAsStream(
                "data/cosmicpve/structure/trial/hidden_graveyard.nbt")) {
            assertNotNull(stream);
            CompoundTag tag = NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
            var palette = tag.getListOrEmpty("palette");
            var names = new ArrayList<String>();
            for (var value : palette) names.add(((CompoundTag) value).getStringOr("Name", ""));
            Map<String, List<BlockPos>> positions = new HashMap<>();
            for (var value : tag.getListOrEmpty("blocks")) {
                CompoundTag block = (CompoundTag) value;
                var pos = block.getListOrEmpty("pos");
                BlockPos p = new BlockPos(pos.getIntOr(0, -1), pos.getIntOr(1, -1), pos.getIntOr(2, -1));
                positions.computeIfAbsent(names.get(block.getIntOr("state", -1)), ignored -> new ArrayList<>()).add(p);
            }
            positions.values().forEach(list -> list.sort(BlockPos::compareTo));
            var size = tag.getListOrEmpty("size");
            assertEquals(List.of(40, 18, 43), List.of(size.getIntOr(0, -1), size.getIntOr(1, -1), size.getIntOr(2, -1)));
            assertEquals(List.of(HiddenGraveyardService.SPAWN_MARKER_LOCAL), positions.get("minecraft:emerald_block"));
            assertEquals(HiddenGraveyardService.CHEST_MARKERS_LOCAL, positions.get("minecraft:coal_block"));
            assertEquals(List.of(new BlockPos(15,5,13), new BlockPos(15,6,13), HiddenGraveyardService.WELL_LOCAL),
                    positions.get("minecraft:water"));
            assertEquals(6, HiddenGraveyardService.authoredGraves(1).size());
            assertEquals(6, HiddenGraveyardService.authoredGraves(2).size());
            assertEquals(6, HiddenGraveyardService.authoredGraves(3).size());
            assertEquals(10, HiddenGraveyardService.authoredGraves(1).getFirst().size());
            for (BlockPos pos : HiddenGraveyardService.authoredGraves(1).stream().flatMap(List::stream).toList())
                assertTrue(positions.getOrDefault("minecraft:cobblestone_slab", List.of()).contains(pos)
                        || positions.getOrDefault("minecraft:cobblestone_stairs", List.of()).contains(pos), pos.toString());
            for (BlockPos pos : HiddenGraveyardService.authoredGraves(2).stream().flatMap(List::stream).toList())
                assertTrue(positions.getOrDefault("minecraft:andesite_slab", List.of()).contains(pos)
                        || positions.getOrDefault("minecraft:andesite_stairs", List.of()).contains(pos), pos.toString());
            for (BlockPos pos : HiddenGraveyardService.authoredGraves(3).stream().flatMap(List::stream).toList())
                assertTrue(positions.getOrDefault("minecraft:stone_slab", List.of()).contains(pos)
                        || positions.getOrDefault("minecraft:stone_stairs", List.of()).contains(pos), pos.toString());
        }
    }
}
