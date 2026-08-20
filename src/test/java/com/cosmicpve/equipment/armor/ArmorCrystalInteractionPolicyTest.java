package com.cosmicpve.equipment.armor;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ArmorCrystalInteractionPolicyTest {
    @Test void emptySlotUsesVanillaPlacement() {
        assertEquals(ArmorCrystalInteractionPolicy.Decision.VANILLA,
                ArmorCrystalInteractionPolicy.decide(true, true, false));
    }

    @Test void ordinaryItemUsesVanillaHandlingWithoutAttempt() {
        var decision = ArmorCrystalInteractionPolicy.decide(true, true, false);
        var calls = new AtomicInteger();
        assertTrue(ArmorCrystalInteractionPolicy.invokeAuthoritative(
                decision, true, calls::incrementAndGet).isEmpty());
        assertEquals(0, calls.get());
    }

    @Test void armorTargetInterceptsAndServerInvokesExactlyOnce() {
        var decision = ArmorCrystalInteractionPolicy.decide(true, true, true);
        var calls = new AtomicInteger();
        assertTrue(ArmorCrystalInteractionPolicy.invokeAuthoritative(
                decision, false, calls::incrementAndGet).isEmpty());
        assertEquals(1, ArmorCrystalInteractionPolicy.invokeAuthoritative(
                decision, true, calls::incrementAndGet).orElseThrow());
        assertEquals(1, calls.get());
    }

    @Test void secondaryClickAndNonCrystalNeverIntercept() {
        assertEquals(ArmorCrystalInteractionPolicy.Decision.VANILLA,
                ArmorCrystalInteractionPolicy.decide(true, false, true));
        assertEquals(ArmorCrystalInteractionPolicy.Decision.VANILLA,
                ArmorCrystalInteractionPolicy.decide(false, true, true));
    }
}
