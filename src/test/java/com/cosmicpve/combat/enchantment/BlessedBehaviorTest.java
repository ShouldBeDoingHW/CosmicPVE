package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class BlessedBehaviorTest {
    @Test void levelsOneThroughFourHaveCanonicalChance() {
        assertEquals(0.02, BlessedBehavior.chance(1), 1.0E-12);
        assertEquals(0.04, BlessedBehavior.chance(2), 1.0E-12);
        assertEquals(0.06, BlessedBehavior.chance(3), 1.0E-12);
        assertEquals(0.08, BlessedBehavior.chance(4), 1.0E-12);
    }
}
