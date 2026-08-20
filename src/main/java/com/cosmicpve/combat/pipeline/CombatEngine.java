package com.cosmicpve.combat.pipeline;

import com.cosmicpve.combat.api.CombatBreakdown;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.CombatResult;
import java.util.List;

/** Pure damage math. Event adapters are responsible for committing its provisional result. */
public final class CombatEngine {
    public CombatResult calculate(CombatContext context, CombatCalculationRequest request) {
        double contributedBonus = request.additiveContributions().stream()
                .mapToDouble(OutgoingDamageContribution::bonus).sum();
        double additiveBonus = request.additiveOutgoingBonus() + contributedBonus;
        validateBaseAndAdditive(request.baseOrdinaryDamage(), additiveBonus);
        validateMultipliers(request.separateOutgoingMultipliers());
        validateMultipliers(request.incomingMultipliers());
        validateMultipliers(request.incomingContributions().stream().map(IncomingDamageContribution::multiplier).toList());

        double base = Math.max(0.0, request.baseOrdinaryDamage());
        double afterAdditive = Math.max(0.0, base * (1.0 + additiveBonus));
        double outgoingProduct = product(request.separateOutgoingMultipliers());
        double afterOutgoing = afterAdditive * outgoingProduct;
        double afterPreDefenseBounds = request.preDefenseBounds().apply(afterOutgoing);
        double incomingProduct = product(request.incomingMultipliers())
                * product(request.incomingContributions().stream().map(IncomingDamageContribution::multiplier).toList());
        double afterIncoming = afterPreDefenseBounds * incomingProduct;
        double finalOrdinary = request.finalOrdinaryBounds().apply(afterIncoming);

        var breakdown = new CombatBreakdown(
                base,
                additiveBonus,
                request.additiveContributions(),
                afterAdditive,
                request.separateOutgoingMultipliers(),
                outgoingProduct,
                afterOutgoing,
                afterPreDefenseBounds,
                request.incomingMultipliers(),
                request.incomingContributions(),
                incomingProduct,
                afterIncoming,
                finalOrdinary);
        return new CombatResult(context, breakdown, request.trueDamagePackets(), 0.0);
    }

    private static void validateBaseAndAdditive(double base, double additive) {
        if (!Double.isFinite(base) || !Double.isFinite(additive) || additive < -1.0) {
            throw new IllegalArgumentException("Damage and additive modifiers must be finite; additive must be >= -1");
        }
    }

    private static void validateMultipliers(List<Double> multipliers) {
        if (multipliers.stream().anyMatch(value -> value == null || !Double.isFinite(value) || value < 0.0)) {
            throw new IllegalArgumentException("Damage multipliers must be finite and non-negative");
        }
    }

    private static double product(List<Double> values) {
        return values.stream().reduce(1.0, (left, right) -> left * right);
    }
}
