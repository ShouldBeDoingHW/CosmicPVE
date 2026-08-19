package com.cosmicpve.combat.api;

import java.util.List;

public record CombatBreakdown(
        double baseOrdinaryDamage,
        double additiveOutgoingBonus,
        double afterAdditiveOutgoing,
        List<Double> separateOutgoingMultipliers,
        double outgoingMultiplierProduct,
        double afterSeparateOutgoing,
        double afterPreDefenseBounds,
        List<Double> incomingMultipliers,
        double incomingMultiplierProduct,
        double afterIncomingMultipliers,
        double finalOrdinaryDamage) {
    public CombatBreakdown {
        separateOutgoingMultipliers = List.copyOf(separateOutgoingMultipliers);
        incomingMultipliers = List.copyOf(incomingMultipliers);
    }
}
