package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.trial.room.PitchPerfectService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.junit.jupiter.api.Test;

class PitchPerfectStructureAuditTest {
    @Test void authoredMarkerPlatesButtonsAndWaterMatchRuntimeCoordinates() throws Exception {
        Map<String, List<BlockPos>> blocks = read();
        assertEquals(List.of(PitchPerfectService.SPAWN_MARKER_LOCAL), blocks.get("minecraft:emerald_block"));
        assertEquals(4, blocks.get("minecraft:light_weighted_pressure_plate").size());
        assertTrue(blocks.get("minecraft:light_weighted_pressure_plate")
                .containsAll(PitchPerfectService.TARGET_PLATES));
        assertEquals(12, blocks.get("minecraft:bamboo_button").size());
        for (var pair : PitchPerfectService.PORTAL_BUTTONS)
            assertTrue(blocks.get("minecraft:bamboo_button").containsAll(pair));
        for (int portal = 0; portal < 6; portal++) {
            int x = portal < 3 ? 1 : 15;
            int z = new int[]{3, 10, 17}[portal % 3];
            assertTrue(blocks.get("minecraft:water").contains(new BlockPos(x, 2, z)));
            assertEquals(portal, PitchPerfectService.portalAt(new BlockPos(x, 2, z)));
        }
    }

    private static Map<String, List<BlockPos>> read() throws Exception {
        try (var stream = PitchPerfectStructureAuditTest.class.getClassLoader()
                .getResourceAsStream("data/cosmicpve/structure/trial/pitch_perfect.nbt")) {
            assertNotNull(stream);
            CompoundTag tag = NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
            var palette = tag.getListOrEmpty("palette");
            List<String> names = new ArrayList<>();
            for (var value : palette) names.add(((CompoundTag) value).getStringOr("Name", ""));
            Map<String, List<BlockPos>> result = new HashMap<>();
            for (var value : tag.getListOrEmpty("blocks")) {
                CompoundTag block = (CompoundTag) value;
                var pos = block.getListOrEmpty("pos");
                BlockPos local = new BlockPos(pos.getIntOr(0, -1), pos.getIntOr(1, -1), pos.getIntOr(2, -1));
                result.computeIfAbsent(names.get(block.getIntOr("state", -1)), ignored -> new ArrayList<>())
                        .add(local);
            }
            return result;
        }
    }
}
