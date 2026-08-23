package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrapBehaviorTest {
    @Test void chanceIsFlatLuckIsRelativeAndDurationsAreLevelScaled() {
        for(int level=1;level<=3;level++) assertEquals(.04,TrapBehavior.chance(level),1e-12);
        assertEquals(25,TrapBehavior.durationTicks(1)); assertEquals(30,TrapBehavior.durationTicks(2)); assertEquals(35,TrapBehavior.durationTicks(3));
        assertEquals(.048,com.cosmicpve.combat.proc.ProcChance.calculate(.04,List.of(1.2)),1e-12);
        assertEquals(4,TrapBehavior.SLOWNESS_V_AMPLIFIER);
    }
}
