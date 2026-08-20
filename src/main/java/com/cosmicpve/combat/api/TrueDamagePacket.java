package com.cosmicpve.combat.api;

import net.minecraft.resources.Identifier;
import java.util.Objects;

/** A separately delivered true-damage packet with explicit reduction bypasses. */
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

    public static TrueDamagePacket standard(Identifier sourceId, double amount) {
        return new TrueDamagePacket(sourceId, amount, true, true, true);
    }
}
