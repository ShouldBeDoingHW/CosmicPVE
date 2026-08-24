package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.trial.room.ColdSnapService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.junit.jupiter.api.Test;

class ColdSnapStructureAuditTest {
    @Test void canonicalMarkersDoorLeverAndBoundedSecretRouteArePresent() throws Exception {
        Map<String,List<BlockPos>> positions=positions();
        assertEquals(List.of(ColdSnapService.SPAWN_MARKER_LOCAL),positions.get("minecraft:emerald_block"));
        assertEquals(List.of(ColdSnapService.GOLD_PLATE_LOCAL),positions.get("minecraft:light_weighted_pressure_plate"));
        assertEquals(List.of(ColdSnapService.IRON_PLATE_LOCAL),positions.get("minecraft:heavy_weighted_pressure_plate"));
        assertTrue(positions.get("minecraft:iron_door").containsAll(List.of(
                ColdSnapService.DOOR_LOWER_LOCAL,ColdSnapService.DOOR_UPPER_LOCAL)));
        assertEquals(List.of(ColdSnapService.FINAL_LEVER_LOCAL),positions.get("minecraft:lever"));
        var ice=positions.get("minecraft:ice");
        assertEquals(2476,ice.size());
        var secret=ice.stream().filter(ColdSnapService::isSecretRouteLocal).toList();
        assertEquals(ColdSnapService.SECRET_ICE_COUNT,secret.size());
        assertEquals(2450,ice.stream().filter(pos->!ColdSnapService.isSecretRouteLocal(pos)).count());
        assertEquals(217,positions.get("minecraft:powder_snow").size());
    }

    @Test void spawnMarkerNeighborsInferPackedIceWithoutLeavingAnAirHole() throws Exception {
        var positions=positions(); Map<BlockPos,String> blocks=new HashMap<>();
        positions.forEach((name,list)->list.forEach(pos->blocks.put(pos,name)));
        BlockPos marker=ColdSnapService.SPAWN_MARKER_LOCAL;
        long packed=List.of(marker.north(),marker.south(),marker.west(),marker.east()).stream()
                .filter(pos->"minecraft:packed_ice".equals(blocks.get(pos))).count();
        assertEquals(3,packed);
        assertEquals(1,List.of(marker.north(),marker.south(),marker.west(),marker.east()).stream()
                .filter(pos->"minecraft:clay".equals(blocks.get(pos))).count());
    }

    @Test void shortcutFlagsAreInitiallyFalseAndTheirTransitionsAreOrderedAndIdempotent() {
        assertFalse(ColdSnapService.doorUnlocked(TrialEncounterState.EMPTY));
        assertFalse(ColdSnapService.secretRevealed(TrialEncounterState.EMPTY));
        assertSame(TrialEncounterState.EMPTY,ColdSnapService.markSecretRevealed(TrialEncounterState.EMPTY));
        var door=ColdSnapService.markDoorUnlocked(TrialEncounterState.EMPTY);
        assertTrue(ColdSnapService.doorUnlocked(door)); assertFalse(ColdSnapService.secretRevealed(door));
        assertEquals(door,ColdSnapService.markDoorUnlocked(door));
        var secret=ColdSnapService.markSecretRevealed(door);
        assertTrue(ColdSnapService.secretRevealed(secret));
        assertEquals(secret,ColdSnapService.markSecretRevealed(secret));
    }

    @Test void highestStructureToLowerFloorFallIsNonlethalWithFeatherFallingFour() {
        int baseFallDamage=29-3;
        float afterFeatherFalling=net.minecraft.world.damagesource.CombatRules
                .getDamageAfterMagicAbsorb(baseFallDamage,12.0F);
        assertEquals(13.52F,afterFeatherFalling,1.0E-4F);
        assertTrue(afterFeatherFalling<20.0F);
    }

    private Map<String,List<BlockPos>> positions() throws Exception {
        try(var stream=getClass().getClassLoader().getResourceAsStream("data/cosmicpve/structure/trial/cold_snap.nbt")) {
            assertNotNull(stream); CompoundTag tag=NbtIo.readCompressed(stream,NbtAccounter.unlimitedHeap());
            var palette=tag.getListOrEmpty("palette"); var names=new ArrayList<String>();
            for(var value:palette) names.add(((CompoundTag)value).getStringOr("Name",""));
            Map<String,List<BlockPos>> result=new HashMap<>();
            for(var value:tag.getListOrEmpty("blocks")) {
                CompoundTag block=(CompoundTag)value; var pos=block.getListOrEmpty("pos");
                BlockPos p=new BlockPos(pos.getIntOr(0,-1),pos.getIntOr(1,-1),pos.getIntOr(2,-1));
                result.computeIfAbsent(names.get(block.getIntOr("state",-1)),ignored->new ArrayList<>()).add(p);
            }
            result.values().forEach(list->list.sort(java.util.Comparator.comparingInt((BlockPos pos)->pos.getX())
                    .thenComparingInt(BlockPos::getY).thenComparingInt(BlockPos::getZ)));
            return result;
        }
    }
}
