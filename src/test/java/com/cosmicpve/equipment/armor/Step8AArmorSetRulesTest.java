package com.cosmicpve.equipment.armor;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.combat.proc.ProcModifiers;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class Step8AArmorSetRulesTest {
    @Test void ancientCanonicalThresholdValuesAreStable() {
        assertEquals(.05, AncientArmorSetBehavior.outgoing(10, 20));
        assertEquals(.10, AncientArmorSetBehavior.outgoing(9.99, 20));
        assertEquals(.95, AncientArmorSetBehavior.incoming(10, 20));
        assertEquals(.90, AncientArmorSetBehavior.incoming(9.99, 20));
        assertEquals(.90, AncientArmorSetBehavior.knockback(10, 20));
        assertEquals(.80, AncientArmorSetBehavior.knockback(9.99, 20));
    }

    @Test void flatCategoryReductionAddsThenClampsWithoutInversion() {
        assertEquals(0.0, CategoryDamageReductionBehavior.multiplier(.75 + .25));
        assertEquals(0.0, CategoryDamageReductionBehavior.multiplier(.75 + .50));
        assertEquals(.05, CategoryDamageReductionBehavior.multiplier(.75 + .20), 1e-12);
        assertEquals(0.0, CategoryDamageReductionBehavior.multiplier(.75 + .50));
        assertEquals(1.0, CategoryDamageReductionBehavior.multiplier(0));
    }

    @Test void cosmicMovementCapAppliesOnlyAfterAggregation() {
        assertEquals(.40, CosmicMovementBonusService.capped(.40));
        assertEquals(.50, CosmicMovementBonusService.capped(.65));
        assertEquals(.50, CosmicMovementBonusService.MAX_BONUS);
    }

    @Test void travelerAndPhantomUseCentralProcModifierShapes() {
        var traveler = new ProcModifiers(List.of(1.0), List.of(.8), Map.of());
        var phantom = new ProcModifiers(List.of(1.0), List.of(1.0), Map.of(ArmorSetIds.MASTERY_PROC, 1.25));
        assertEquals(List.of(.8), traveler.cooldownDurationMultipliers());
        assertEquals(1.25, phantom.namedChanceMultipliers().get(ArmorSetIds.MASTERY_PROC));
        assertFalse(phantom.namedChanceMultipliers().containsKey(com.cosmicpve.CosmicPVE.id("non_mastery")));
    }
}
