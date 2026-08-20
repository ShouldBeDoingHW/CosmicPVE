package com.cosmicpve.combat.action;

import com.cosmicpve.combat.api.DamageChannel;

public record CombatActionOutcome(
        long sequenceId,
        long parentSequenceId,
        DamageChannel channel,
        double requestedDamage,
        double healthDamage,
        boolean accepted) {}
