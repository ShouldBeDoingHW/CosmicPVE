package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.trial.room.DeadeyeService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.junit.jupiter.api.Test;

class DeadeyeStructureAuditTest {
    @Test void authoredHalvesComposeWestThenEastWithStableMarkers() throws Exception {
        var west = structure("deadeye_west");
        var east = structure("deadeye_east");
        assertEquals(new BlockPos(41, 31, 43), west.size());
        assertEquals(new BlockPos(30, 31, 43), east.size());
        assertEquals("minecraft:emerald_block", west.states().get(DeadeyeService.WEST_SPAWN_MARKER));
        assertEquals("minecraft:lever", east.states().get(new BlockPos(27, 23, 21)));
        assertFalse(east.states().containsValue("minecraft:emerald_block"));

        Map<BlockPos, String> combined = new HashMap<>(west.states());
        east.states().forEach((pos, state) -> combined.put(pos.offset(DeadeyeService.EAST_OFFSET_X, 0, 0), state));
        assertEquals(4, DeadeyeService.targets().size());
        DeadeyeService.targets().forEach(pos -> assertEquals("minecraft:target", combined.get(pos), "target " + pos));
        assertEquals("minecraft:lever", combined.get(DeadeyeService.FINAL_LEVER_LOCAL));
        assertEquals(70, combined.keySet().stream().mapToInt(BlockPos::getX).max().orElseThrow());
    }

    @Test void everyExactRevealGroupResolvesToAuthoredNonAirBlocks() throws Exception {
        var west = structure("deadeye_west");
        var east = structure("deadeye_east");
        Map<BlockPos, String> combined = new HashMap<>(west.states());
        east.states().forEach((pos, state) -> combined.put(pos.offset(DeadeyeService.EAST_OFFSET_X, 0, 0), state));
        assertEquals(DeadeyeService.SECTION_COUNT, DeadeyeService.revealGroups().size());
        DeadeyeService.revealGroups().forEach((section, positions) -> {
            assertFalse(positions.isEmpty(), section + " must have authored reveal blocks");
            assertEquals(positions.size(), positions.stream().distinct().count(), section + " has duplicate coordinates");
            positions.forEach(pos -> {
                String state = combined.get(pos);
                assertNotNull(state, section + " missing " + pos);
                assertTrue(allowed(section, state), section + " has unexpected " + state + " at " + pos);
            });
        });
    }

    private static boolean allowed(DeadeyeService.Section section, String block) {
        return switch (section) {
            case DIAMOND -> block.equals("minecraft:diamond_block")
                    || block.equals("minecraft:prismarine_wall") || block.equals("minecraft:ladder");
            case GOLD -> block.equals("minecraft:gold_block") || block.equals("minecraft:bamboo_fence")
                    || block.equals("minecraft:bamboo_trapdoor");
            case RESIN_PURPUR -> block.equals("minecraft:resin_bricks")
                    || block.equals("minecraft:resin_brick_wall") || block.equals("minecraft:purpur_block")
                    || block.equals("minecraft:purpur_slab");
            case CRIMSON -> block.equals("minecraft:stripped_crimson_hyphae")
                    || block.equals("minecraft:crimson_fence");
        };
    }

    private StructureData structure(String name) throws Exception {
        try (var stream = getClass().getClassLoader().getResourceAsStream(
                "data/cosmicpve/structure/trial/" + name + ".nbt")) {
            assertNotNull(stream);
            var tag = NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
            var size = tag.getListOrEmpty("size");
            BlockPos dimensions = new BlockPos(size.getIntOr(0, -1), size.getIntOr(1, -1), size.getIntOr(2, -1));
            var palette = tag.getListOrEmpty("palette");
            var names = new ArrayList<String>();
            for (var value : palette)
                names.add(((net.minecraft.nbt.CompoundTag) value).getStringOr("Name", ""));
            Map<BlockPos, String> states = new HashMap<>();
            for (var value : tag.getListOrEmpty("blocks")) {
                var block = (net.minecraft.nbt.CompoundTag) value;
                var pos = block.getListOrEmpty("pos");
                states.put(new BlockPos(pos.getIntOr(0, -1), pos.getIntOr(1, -1), pos.getIntOr(2, -1)),
                        names.get(block.getIntOr("state", -1)));
            }
            return new StructureData(dimensions, states);
        }
    }

    private record StructureData(BlockPos size, Map<BlockPos, String> states) {}
}
