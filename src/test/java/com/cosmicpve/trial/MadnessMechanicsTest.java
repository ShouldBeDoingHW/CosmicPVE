package com.cosmicpve.trial;

import com.cosmicpve.trial.madness.MadnessRuntime;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MadnessMechanicsTest {
    @Test void stickyKeysDeterministicFailureBoundaryAndOnlyButtonsLevers() {
        assertTrue(MadnessRuntime.isControl(Blocks.STONE_BUTTON));
        assertTrue(MadnessRuntime.isControl(Blocks.LEVER));
        for(var block:new net.minecraft.world.level.block.Block[]{Blocks.BELL,Blocks.STONE_PRESSURE_PLATE,Blocks.OAK_DOOR,Blocks.CHEST})
            assertFalse(MadnessRuntime.isControl(block));
        assertTrue(MadnessRuntime.failsControl(true,.5,.499));
        assertFalse(MadnessRuntime.failsControl(true,.5,.5));
        assertFalse(MadnessRuntime.failsControl(false,.5,0));
    }
    @Test void cursedLifeScalesEveryAmountWithoutChangingMaximumHealth() {
        assertEquals(7,MadnessRuntime.scaledHealing(10,.7),0.00001);
        assertEquals(.7,MadnessRuntime.scaledHealing(1,.7),0.00001);
        assertEquals(1.4,MadnessRuntime.scaledHealing(2,.7),0.00001);
        assertEquals(0,MadnessRuntime.scaledHealing(0,.7));
    }
    @Test void statuesIgnoreJitterButNotMeaningfulXyzMovement() {
        assertFalse(MadnessRuntime.moved(Vec3.ZERO,new Vec3(.001,.001,.001),.03));
        assertTrue(MadnessRuntime.moved(Vec3.ZERO,new Vec3(.05,0,0),.03));
        assertTrue(MadnessRuntime.moved(Vec3.ZERO,new Vec3(0,.05,0),.03));
        assertTrue(MadnessRuntime.moved(Vec3.ZERO,new Vec3(0,0,.05),.03));
        assertEquals(10,MadnessRuntime.penaltyAmount(20,.5));
        assertEquals(15,MadnessRuntime.penaltyAmount(30,.5));
    }
    @Test void breezeDoesNotAccumulateRunawayAndCrouchSuppressesOnlyBreeze() {
        Vec3 velocity=Vec3.ZERO,direction=new Vec3(1,0,0);
        for(int i=0;i<10000;i++) velocity=MadnessRuntime.breezeVelocity(velocity,direction,false,.008,.15);
        assertEquals(.15,velocity.x,0.0000001); assertEquals(0,velocity.y);
        Vec3 falling=new Vec3(.02,-.5,.03);
        assertEquals(falling,MadnessRuntime.breezeVelocity(falling,direction,true,.008,.15));
        assertEquals(-.5,MadnessRuntime.breezeVelocity(falling,direction,false,.008,.15).y);
    }
    @Test void breezeOnlyRunsForNonShiftingPlayersOutsideColdSnapAndDeadeye() {
        assertTrue(MadnessRuntime.breezeApplies(TrialSessionService.CINDER_WOLF, false));
        assertFalse(MadnessRuntime.breezeApplies(TrialSessionService.CINDER_WOLF, true));
        assertFalse(MadnessRuntime.breezeApplies(TrialSessionService.COLD_SNAP, false));
        assertFalse(MadnessRuntime.breezeApplies(TrialSessionService.DEADEYE, false));
        assertFalse(MadnessRuntime.breezeApplies(null, false));
    }
    @Test void rocketVelocityHasEightBlockPotentialUnderVanillaGravity() {
        double y=0,velocity=1.2;
        while(velocity>0) { y+=velocity; velocity=(velocity-.08)*.98; }
        assertEquals(8,y,.3);
    }
}
