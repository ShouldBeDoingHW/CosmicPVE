package com.cosmicpve.equipment.armor;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private static CombatCalculationRequest request(OutgoingDamageContribution outgoing, IncomingDamageContribution incoming) {
        return new CombatCalculationRequest(10, 0, List.of(), DamageBounds.UNBOUNDED, List.of(),
                DamageBounds.UNBOUNDED, List.of(), List.of(outgoing), List.of(incoming));
    }
}
