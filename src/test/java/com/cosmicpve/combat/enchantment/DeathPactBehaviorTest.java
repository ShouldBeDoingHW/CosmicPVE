package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import org.junit.jupiter.api.Test;

class DeathPactBehaviorTest {
    @Test void outgoingAndIncomingUseCanonicalLevelFormulas() {
        assertEquals(0.0, DeathPactBehavior.outgoingBonus(0));
        assertEquals(1.0, DeathPactBehavior.incomingMultiplier(0));
        for (int level = 1; level <= 5; level++) {
            assertEquals(-(0.075 - 0.01 * level), DeathPactBehavior.outgoingBonus(level), 1e-12);
            assertEquals(1.0 - (0.01 + 0.01 * level), DeathPactBehavior.incomingMultiplier(level), 1e-12);
        }
        assertEquals(-0.065, DeathPactBehavior.outgoingBonus(1), 1e-12);
        assertEquals(-0.025, DeathPactBehavior.outgoingBonus(5), 1e-12);
        assertEquals(0.846, DeathPactBehavior.incomingMultiplier(5) * 0.90, 1e-12);
    }
    @Test void masteryRulesAndBlackScrollExclusionAreInheritedFromTier() {
        var spec = CosmicEnchantmentSpecs.DEATH_PACT;
        assertTrue(spec.tier().allowsRates(49, 51));
        assertFalse(spec.tier().allowsRates(50, 51));
        assertFalse(spec.tier().extractableByBlackScroll());
    }
}
