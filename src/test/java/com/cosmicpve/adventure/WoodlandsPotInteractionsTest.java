package com.cosmicpve.adventure;

import static org.junit.jupiter.api.Assertions.*;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class WoodlandsPotInteractionsTest {
    @Test void depositProtectionIsScopedToNonCreativeWoodlandsPlayers() {
        assertTrue(WoodlandsPotInteractions.protects(DenseWoodlandsSessionService.DIMENSION, false, false));
        assertFalse(WoodlandsPotInteractions.protects(DenseWoodlandsSessionService.DIMENSION, true, false));
        assertFalse(WoodlandsPotInteractions.protects(DenseWoodlandsSessionService.DIMENSION, false, true));
        assertFalse(WoodlandsPotInteractions.protects(Level.OVERWORLD, false, false));
        assertFalse(WoodlandsPotInteractions.protects(com.cosmicpve.trial.TrialRuntime.INSTANCE_DIMENSION, false, false));
    }
}
