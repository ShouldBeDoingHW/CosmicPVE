package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ImplantsBehaviorTest {
    @Test void levelsUseCanonicalIntervals() {
        assertEquals(85, ImplantsBehavior.intervalTicks(1));
        assertEquals(70, ImplantsBehavior.intervalTicks(2));
        assertEquals(55, ImplantsBehavior.intervalTicks(3));
    }
    @Test void eachActivationHealsExactlyOneHp() { assertEquals(1.0F, ImplantsBehavior.HEAL_AMOUNT); }
    @Test void schedulingStartsFromCurrentTimeAndDoesNotCatchUp() {
        assertEquals(1_055, ImplantsBehavior.nextHealTick(1_000, 3));
        assertEquals(1_110, ImplantsBehavior.nextHealTick(1_055, 3));
    }
    @Test void invalidLevelsReject() {
        assertThrows(IllegalArgumentException.class, () -> ImplantsBehavior.intervalTicks(0));
        assertThrows(IllegalArgumentException.class, () -> ImplantsBehavior.intervalTicks(4));
    }
}
