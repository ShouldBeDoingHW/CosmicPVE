package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import org.junit.jupiter.api.Test;

class OxygenateServiceTest {
    @Test void oneDisplayedBubbleUsesPinnedMinecraftAirUnits() {
        assertEquals(300, Entity.TOTAL_AIR_SUPPLY);
        assertEquals(10, OxygenateService.DISPLAYED_AIR_BUBBLES);
        assertEquals(30, OxygenateService.AIR_PER_BUBBLE);
    }

    @Test void restoresExactlyOneBubblePerLevelAndClampsToMaximum() {
        assertEquals(130, OxygenateService.restoredAir(100, 300, 1));
        assertEquals(160, OxygenateService.restoredAir(100, 300, 2));
        assertEquals(300, OxygenateService.restoredAir(280, 300, 2));
    }

    @Test void absentEffectiveLevelIsDeterministicNoOp() {
        assertEquals(100, OxygenateService.restoredAir(100, 300, 0));
        assertEquals(0, OxygenateService.restorationAmount(false, true, 2));
        assertEquals(0, OxygenateService.restorationAmount(true, false, 2));
        assertEquals(60, OxygenateService.restorationAmount(true, true, 2));
    }

    @Test void bridgeUsesPostBreakDropsEventRatherThanAttemptOrSwingEvents() throws Exception {
        assertEquals(void.class,
                OxygenateEventBridge.class.getMethod("onBlockDrops", BlockDropsEvent.class).getReturnType());
    }
}
