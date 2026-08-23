package com.cosmicpve.trial.room;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.instance.InstanceBounds;
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
}
