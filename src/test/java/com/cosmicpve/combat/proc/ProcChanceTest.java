package com.cosmicpve.combat.proc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class ProcChanceTest {
    @Test
    void appliesRelativeMultipliersRatherThanPercentagePoints() {
        assertEquals(0.012, ProcChance.calculate(0.01, List.of(1.2)), 1.0E-12);
        assertEquals(0.0096, ProcChance.calculate(0.01, List.of(1.2, 0.8)), 1.0E-12);
    }

    @Test
    void clampsFinalProbability() {
        assertEquals(1.0, ProcChance.calculate(0.75, List.of(2.0)));
        assertEquals(0.0, ProcChance.calculate(0.0, List.of(50.0)));
    }
}
