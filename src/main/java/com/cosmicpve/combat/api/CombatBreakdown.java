package com.cosmicpve.combat.api;

import java.util.List;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.IncomingDamageContribution;

public record CombatBreakdown(
        double baseOrdinaryDamage,
        double additiveOutgoingBonus,
        List<OutgoingDamageContribution> additiveContributions,
        double afterAdditiveOutgoing,
        List<Double> separateOutgoingMultipliers,
        double outgoingMultiplierProduct,
        double afterSeparateOutgoing,
        double afterPreDefenseBounds,
        List<Double> incomingMultipliers,
        List<IncomingDamageContribution> incomingContributions,
        double incomingMultiplierProduct,
        double afterIncomingMultipliers,
        double finalOrdinaryDamage) {
    public CombatBreakdown {
        additiveContributions = List.copyOf(additiveContributions);
        separateOutgoingMultipliers = List.copyOf(separateOutgoingMultipliers);
        incomingMultipliers = List.copyOf(incomingMultipliers);
        incomingContributions = List.copyOf(incomingContributions);
    }
}
