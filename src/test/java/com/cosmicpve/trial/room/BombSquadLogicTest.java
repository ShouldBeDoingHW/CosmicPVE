package com.cosmicpve.trial.room;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class BombSquadLogicTest {
    @Test void seededLayoutSelectsOneSharedSpawnAndTwoDistinctDistantExits() {
        var service = new BombSquadService();
        var first = service.selectLayout(RandomSource.create(42));
        var repeated = service.selectLayout(RandomSource.create(42));
        assertEquals(first, repeated);
        assertEquals(2, new HashSet<>(first.activeExitLocals()).size());
        for (BlockPos exit : first.activeExitLocals()) {
            var cell=BombSquadService.gridCell(exit); var start=first.spawnCell();
            assertTrue(Math.max(Math.abs(cell.column()-start.column()),Math.abs(cell.row()-start.row()))>1);
        }
    }

    @Test void broadSeedCoverageNeverUsesLocalThreeByThreeNeighborhood() {
        var service=new BombSquadService();
        for(int seed=0;seed<1000;seed++) {
            var layout=service.selectLayout(RandomSource.create(seed));
            for(var exit:layout.activeExitLocals()) {
                var cell=BombSquadService.gridCell(exit); var start=layout.spawnCell();
                assertTrue(Math.max(Math.abs(cell.column()-start.column()),Math.abs(cell.row()-start.row()))>1);
            }
        }
    }

    @Test void onlyStoneFromValidEncounterInsideBoundsMayBypassProtection() {
        assertTrue(BombSquadService.allowsExplosionMaterial(true,true,false,Blocks.STONE.defaultBlockState()));
        assertFalse(BombSquadService.allowsExplosionMaterial(false,true,false,Blocks.STONE.defaultBlockState()));
        assertFalse(BombSquadService.allowsExplosionMaterial(true,false,false,Blocks.STONE.defaultBlockState()));
        assertFalse(BombSquadService.allowsExplosionMaterial(true,true,true,Blocks.STONE.defaultBlockState()));
        assertFalse(BombSquadService.allowsExplosionMaterial(true,true,false,Blocks.OBSIDIAN.defaultBlockState()));
        assertFalse(BombSquadService.allowsExplosionMaterial(true,true,false,Blocks.BEDROCK.defaultBlockState()));
        assertFalse(BombSquadService.allowsExplosionMaterial(true,true,false,Blocks.SEA_LANTERN.defaultBlockState()));
        assertFalse(BombSquadService.allowsExplosionMaterial(true,true,false,Blocks.GRAY_STAINED_GLASS.defaultBlockState()));
        assertFalse(BombSquadService.allowsExplosionMaterial(true,true,false,Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE.defaultBlockState()));
        assertFalse(BombSquadService.allowsExplosionMaterial(true,true,false,Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.defaultBlockState()));
    }

    @Test void issuedEggCarriesTypedCreeperIdentity() {
        var egg=BombSquadService.issuedEgg();
        assertTrue(BombSquadService.issuedEgg(egg));
        assertFalse(BombSquadService.issuedEgg(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CREEPER_SPAWN_EGG)));
    }
}
