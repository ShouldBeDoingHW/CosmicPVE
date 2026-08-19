package com.cosmicpve.combat.api;

import net.minecraft.resources.Identifier;
import java.util.Objects;

/** A modeled true-damage packet. Application is deliberately deferred beyond Step 3A. */
public record TrueDamagePacket(
        Identifier sourceId,
        double amount,
        boolean bypassesArmor,
        boolean bypassesAbsorption,
        boolean bypassesCustomReduction) {
    public TrueDamagePacket {
        sourceId = Objects.requireNonNull(sourceId);
        if (!Double.isFinite(amount) || amount <= 0.0) {
            throw new IllegalArgumentException("True damage must be finite and positive");
        }
    }
}
