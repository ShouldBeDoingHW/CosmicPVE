package com.cosmicpve.combat.cooldown;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/** Immutable persistence-ready representation of one active cooldown. */
public record CooldownEntry(long expiresAtTick, CooldownScope scope, Optional<Identifier> scopeId) {
    public CooldownEntry {
        if (expiresAtTick < 0) {
            throw new IllegalArgumentException("Cooldown expiry cannot be negative");
        }
        scope = Objects.requireNonNull(scope);
        scopeId = scopeId == null ? Optional.empty() : scopeId;
        if (scope == CooldownScope.INSTANCE_SESSION && scopeId.isEmpty()) {
            throw new IllegalArgumentException("Instance cooldowns require a stable scope ID");
        }
        if (scope != CooldownScope.INSTANCE_SESSION && scopeId.isPresent()) {
            throw new IllegalArgumentException("Only instance cooldowns may carry a scope ID");
        }
    }
}
