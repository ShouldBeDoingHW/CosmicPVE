package com.cosmicpve.combat.pipeline;

import net.minecraft.resources.Identifier;

public record IncomingDamageContribution(Identifier sourceId, double multiplier) {
    public IncomingDamageContribution {
        if (!Double.isFinite(multiplier) || multiplier < 0.0) {
            throw new IllegalArgumentException("Incoming multiplier must be finite and non-negative");
        }
    }
}
