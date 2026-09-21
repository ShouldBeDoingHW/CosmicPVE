package com.cosmicpve.trial.room;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.instance.InstanceBounds;
import com.cosmicpve.trial.TrialRoomLoadoutService;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class CircuitCircusLogicTest {
    @Test void assignmentPoolContainsTwoOfEveryMaterial() {
        var pool=CircuitCircusService.assignmentPool(); assertEquals(8,pool.size());
        for(var material:CircuitMaterial.values()) assertEquals(2,pool.stream().filter(material::equals).count());
    }
    @Test void connectivityIsOrthogonalBoundedAndSupportsVerticalPaths() {
        var bounds=new InstanceBounds(BlockPos.ZERO,new BlockPos(4,4,4));
        var horizontal=new HashSet<BlockPos>(); horizontal.add(new BlockPos(1,1,1)); horizontal.add(new BlockPos(2,1,1)); horizontal.add(new BlockPos(3,1,1));
        assertTrue(CircuitCircusService.orthogonallyConnected(horizontal,java.util.Set.of(new BlockPos(1,1,1)),java.util.Set.of(new BlockPos(3,1,1)),bounds));
        assertFalse(CircuitCircusService.orthogonallyConnected(java.util.Set.of(new BlockPos(1,1,1),new BlockPos(2,2,1)),java.util.Set.of(new BlockPos(1,1,1)),java.util.Set.of(new BlockPos(2,2,1)),bounds));
        assertTrue(CircuitCircusService.orthogonallyConnected(java.util.Set.of(new BlockPos(1,1,1),new BlockPos(1,2,1)),java.util.Set.of(new BlockPos(1,1,1)),java.util.Set.of(new BlockPos(1,2,1)),bounds));
    }
    @Test void acceptedTargetFeedbackUsesArrowHitPlayerSound() {
        assertEquals(net.minecraft.sounds.SoundEvents.ARROW_HIT_PLAYER, CircuitCircusService.targetFeedbackSound());
    }
    @Test void canonicalLoadoutReservesSlotNineForBakedPotatoes() {
        assertEquals(16,TrialRoomLoadoutService.CIRCUIT_CIRCUS_BAKED_POTATOES);
        assertEquals(8,TrialRoomLoadoutService.CIRCUIT_CIRCUS_FOOD_SLOT);
        assertNotEquals(net.minecraft.world.item.Items.BREAD,net.minecraft.world.item.Items.BAKED_POTATO);
    }
}
