package com.cosmicpve.trial.room;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.instance.InstanceBounds;
import com.cosmicpve.instance.protection.InstanceProtectionEventBridge;
import com.cosmicpve.trial.TrialLifecycleState;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class CircuitPlacementPolicyTest {
    private static final InstanceBounds BOUNDS=new InstanceBounds(new BlockPos(128,64,0),new BlockPos(152,88,30));
    private static final BlockPos INSIDE=new BlockPos(140,68,12);

    @Test void allFourCanonicalCircuitColorsOverrideDefaultUseAndPlaceDenial() {
        for(var block:List.of(Blocks.RED_STAINED_GLASS,Blocks.YELLOW_STAINED_GLASS,
                Blocks.GREEN_STAINED_GLASS,Blocks.BLUE_STAINED_GLASS)) {
            assertTrue(CircuitPlacementPolicy.allows(true,true,TrialLifecycleState.ROOM_ACTIVE,
                    BOUNDS,INSIDE,block.defaultBlockState()));
            assertTrue(InstanceProtectionEventBridge.explicitAllowOverridesDefaultDeny(true,false));
        }
    }
    @Test void exceptionRemainsNarrowForActorRoomStatePositionAndMaterial() {
        assertFalse(allows(false,true,TrialLifecycleState.ROOM_ACTIVE,INSIDE,Blocks.RED_STAINED_GLASS));
        assertFalse(allows(true,false,TrialLifecycleState.ROOM_ACTIVE,INSIDE,Blocks.RED_STAINED_GLASS));
        assertFalse(allows(true,true,TrialLifecycleState.ROOM_INTRO,INSIDE,Blocks.RED_STAINED_GLASS));
        assertFalse(allows(true,true,TrialLifecycleState.ROOM_ACTIVE,new BlockPos(200,68,12),Blocks.RED_STAINED_GLASS));
        for(var block:List.of(Blocks.DIRT,Blocks.COBBLESTONE,Blocks.PURPLE_STAINED_GLASS))
            assertFalse(allows(true,true,TrialLifecycleState.ROOM_ACTIVE,INSIDE,block));
        assertFalse(InstanceProtectionEventBridge.explicitAllowOverridesDefaultDeny(false,false));
    }
    private static boolean allows(boolean participant,boolean room,TrialLifecycleState state,BlockPos pos,net.minecraft.world.level.block.Block block) {
        return CircuitPlacementPolicy.allows(participant,room,state,BOUNDS,pos,block.defaultBlockState());
    }
}
