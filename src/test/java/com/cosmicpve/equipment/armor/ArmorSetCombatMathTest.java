package com.cosmicpve.equipment.armor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.cosmicpve.combat.api.DamageBounds;
import com.cosmicpve.combat.pipeline.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class ArmorSetCombatMathTest {
    @Test void phantomUsesAdditiveOutgoingAndMultiplicativeIncoming() {
        var request = request(new OutgoingDamageContribution(ArmorSetIds.PHANTOM, .25),
                new IncomingDamageContribution(ArmorSetIds.PHANTOM, 1.10));
        assertEquals(13.75, new CombatEngine().calculate(null, request).breakdown().finalOrdinaryDamage(), 1e-9);
    }

    @Test void yetiUsesAdditiveOutgoingAndMultiplicativeIncoming() {
        var request = request(new OutgoingDamageContribution(ArmorSetIds.YETI, .10),
                new IncomingDamageContribution(ArmorSetIds.YETI, .90));
        assertEquals(9.9, new CombatEngine().calculate(null, request).breakdown().finalOrdinaryDamage(), 1e-9);
    }

    @Test void ancientUsesStrictLowHealthThresholdAndApprovedValues() {
        assertFalse(AncientArmorSetBehavior.isLowHealth(11, 20));
        assertFalse(AncientArmorSetBehavior.isLowHealth(10, 20));
        assertTrue(AncientArmorSetBehavior.isLowHealth(9.999, 20));
        assertEquals(.075, AncientArmorSetBehavior.outgoing(20, 20));
        assertEquals(.075, AncientArmorSetBehavior.outgoing(10, 20));
        assertEquals(.15, AncientArmorSetBehavior.outgoing(9, 20));
        assertEquals(.925, AncientArmorSetBehavior.incoming(20, 20));
        assertEquals(.925, AncientArmorSetBehavior.incoming(10, 20));
        assertEquals(.85, AncientArmorSetBehavior.incoming(9, 20));
        assertEquals(9.25, new CombatEngine().calculate(null, request(
                new OutgoingDamageContribution(ArmorSetIds.ANCIENT, 0.0),
                new IncomingDamageContribution(ArmorSetIds.ANCIENT, .925)))
                .breakdown().finalOrdinaryDamage(), 1e-9);
        var truePacket = com.cosmicpve.combat.api.TrueDamagePacket.standard(
                com.cosmicpve.CosmicPVE.id("ancient_bypass"), 2.0);
        var separated = new CombatEngine().calculate(null, new CombatCalculationRequest(
                10, 0, List.of(), DamageBounds.UNBOUNDED, List.of(), DamageBounds.UNBOUNDED,
                List.of(truePacket), List.of(), List.of(new IncomingDamageContribution(ArmorSetIds.ANCIENT, .85))));
        assertEquals(8.5, separated.breakdown().finalOrdinaryDamage(), 1e-9);
        assertEquals(2.0, separated.totalQueuedTrueDamage(), 1e-9);
    }

    private static CombatCalculationRequest request(OutgoingDamageContribution outgoing, IncomingDamageContribution incoming) {
        return new CombatCalculationRequest(10, 0, List.of(), DamageBounds.UNBOUNDED, List.of(),
                DamageBounds.UNBOUNDED, List.of(), List.of(outgoing), List.of(incoming));
    }
}
