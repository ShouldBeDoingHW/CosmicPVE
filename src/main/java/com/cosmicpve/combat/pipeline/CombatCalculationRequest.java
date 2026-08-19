package com.cosmicpve.combat.pipeline;

import com.cosmicpve.combat.api.DamageBounds;
import com.cosmicpve.combat.api.TrueDamagePacket;
import java.util.List;

public record CombatCalculationRequest(
        double baseOrdinaryDamage,
        double additiveOutgoingBonus,
        List<Double> separateOutgoingMultipliers,
        DamageBounds preDefenseBounds,
        List<Double> incomingMultipliers,
        DamageBounds finalOrdinaryBounds,
        List<TrueDamagePacket> trueDamagePackets) {
    public CombatCalculationRequest {
        separateOutgoingMultipliers = List.copyOf(separateOutgoingMultipliers);
        incomingMultipliers = List.copyOf(incomingMultipliers);
        trueDamagePackets = List.copyOf(trueDamagePackets);
    }

    public static CombatCalculationRequest unchanged(double ordinaryDamage) {
        return new CombatCalculationRequest(
                ordinaryDamage,
                0.0,
                List.of(),
                DamageBounds.UNBOUNDED,
                List.of(),
                DamageBounds.UNBOUNDED,
                List.of());
    }
}
