package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.proc.ProcChance;
import java.util.List;
import org.junit.jupiter.api.Test;

class CactusBehaviorTest {
    @Test void levelsProvideThreeAndSixPercentBaseChance() {
        assertEquals(0.0,CactusBehavior.chance(0),1e-12);
        assertEquals(0.03,CactusBehavior.chance(1),1e-12);
        assertEquals(0.06,CactusBehavior.chance(2),1e-12);
    }
    @Test void normalRelativeLuckMultiplierAppliesToItsCandidateChance() {
        assertEquals(0.09,ProcChance.calculate(CactusBehavior.chance(2),List.of(1.5)),1e-12);
    }
    @Test void retaliationIsStandardOnePointFiveTrueDamageWithNoProcRecursion() {
        var packet=CactusBehavior.packet();
        assertEquals(1.5,packet.amount(),1e-12); assertTrue(packet.bypassesArmor());
        assertTrue(packet.bypassesCustomReduction()); assertFalse(packet.bypassesAbsorption());
        assertEquals(RecursionPolicy.NO_PROCS,CactusBehavior.RECURSION_POLICY);
    }
}
