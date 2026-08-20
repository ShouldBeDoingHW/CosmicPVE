package com.cosmicpve.combat.proc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class ProcModifierSourceRegistryTest {
    @Test
    void combinesIndependentModifierSourcesWithoutEnchantSpecificEngineLogic() {
        var registry = new ProcModifierSourceRegistry();
        registry.register(owner -> new ProcModifiers(List.of(1.20), List.of(1.0)));
        registry.register(owner -> new ProcModifiers(List.of(1.10), List.of(0.80)));

        var combined = registry.resolve(null);

        assertEquals(List.of(1.20, 1.10), combined.chanceMultipliers());
        assertEquals(0.264, ProcChance.calculate(0.20, combined.chanceMultipliers()), 1.0E-12);
        assertEquals(List.of(1.0, 0.80), combined.cooldownDurationMultipliers());
    }
}
