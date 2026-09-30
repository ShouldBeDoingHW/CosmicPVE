package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrapBehaviorTest {
    @Test void chanceIsFlatLuckIsRelativeAndDurationsAreLevelScaled() {
        for(int level=1;level<=3;level++) {
            assertEquals(.03,TrapBehavior.chance(level),1e-12);
            assertEquals(.04,TrapBehavior.titanChance(level),1e-12);
        }
        assertEquals(4,TrapBehavior.durationTicks(1)); assertEquals(8,TrapBehavior.durationTicks(2)); assertEquals(12,TrapBehavior.durationTicks(3));
        assertEquals(15,TrapBehavior.TITAN_DURATION_TICKS);
        assertEquals(.036,com.cosmicpve.combat.proc.ProcChance.calculate(.03,List.of(1.2)),1e-12);
        assertEquals(.048,com.cosmicpve.combat.proc.ProcChance.calculate(.04,List.of(1.2)),1e-12);
        assertEquals(12, TrapBehavior.nonShorteningDuration(4, 12));
        assertEquals(15, TrapBehavior.nonShorteningDuration(15, 4));
    }
}
