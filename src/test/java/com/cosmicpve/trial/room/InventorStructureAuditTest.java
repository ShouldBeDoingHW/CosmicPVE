package com.cosmicpve.trial.room;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InventorStructureAuditTest {
    @Test
    void canonicalStructureExposesAuthoredEncounterContract() throws Exception {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(
                "data/cosmicpve/structure/trial/the_inventor.nbt")) {
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
            assertEquals(List.of(43, 23, 43), List.of(size.getIntOr(0,-1), size.getIntOr(1,-1), size.getIntOr(2,-1)));
            assertEquals(List.of(InventorService.PLAYER_MARKER_LOCAL), positions.get("minecraft:emerald_block"));
            assertEquals(List.of(InventorService.BOSS_MARKER_LOCAL), positions.get("minecraft:diamond_block"));
            assertEquals(List.of(new BlockPos(7,4,7), new BlockPos(35,4,7), new BlockPos(7,4,35), new BlockPos(35,4,35)),
                    positions.get("minecraft:beacon"));
            for (InventorService.Station station : InventorService.Station.values()) {
                assertEquals(List.of(station.controlLocal), positions.get("minecraft:" + switch (station) {
                    case BELL -> "bell";
                    case BUTTON -> "polished_blackstone_button";
                    case PLATE -> "light_weighted_pressure_plate";
                    case LEVER -> "lever";
                }));
                assertEquals(true, positions.get("minecraft:beacon").contains(station.beaconLocal));
            }
        }
    }
}
