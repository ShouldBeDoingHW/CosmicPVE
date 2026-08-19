package com.cosmicpve.combat.api;

import java.util.List;

public record CombatResult(
        CombatContext context,
        CombatBreakdown breakdown,
        List<TrueDamagePacket> trueDamagePackets,
        double committedHealthDamage) {
    public CombatResult {
        trueDamagePackets = List.copyOf(trueDamagePackets);
        if (!Double.isFinite(committedHealthDamage) || committedHealthDamage < 0.0) {
            throw new IllegalArgumentException("Committed damage must be finite and non-negative");
        }
    }

    public CombatResult commit(double actualHealthDamage) {
        return new CombatResult(context, breakdown, trueDamagePackets, Math.max(0.0, actualHealthDamage));
    }

    public boolean isCommittedDamagingHit() {
        return committedHealthDamage > 0.0;
    }

    public double totalQueuedTrueDamage() {
        return trueDamagePackets.stream().mapToDouble(TrueDamagePacket::amount).sum();
    }
}
