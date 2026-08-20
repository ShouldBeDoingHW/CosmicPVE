package com.cosmicpve.combat.pipeline;

import java.util.Objects;
import net.minecraft.resources.Identifier;

/** One named contribution to the shared additive custom outgoing-damage bucket. */
public record OutgoingDamageContribution(Identifier sourceId, double bonus) {
    public OutgoingDamageContribution {
        sourceId = Objects.requireNonNull(sourceId);
        if (!Double.isFinite(bonus) || bonus < -1.0) {
            throw new IllegalArgumentException("Outgoing contribution must be finite and at least -1");
        }
    }
}
