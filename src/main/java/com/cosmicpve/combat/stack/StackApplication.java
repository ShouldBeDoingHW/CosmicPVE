package com.cosmicpve.combat.stack;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.Identifier;

/** Attribution and requested runtime scope supplied by a proc/action or development command. */
public record StackApplication(
        Optional<UUID> sourceEntityId,
        Optional<UUID> creditedPlayerId,
        CombatStackScope scope,
        Optional<Identifier> scopeId) {
    public StackApplication {
        sourceEntityId = sourceEntityId == null ? Optional.empty() : sourceEntityId;
        creditedPlayerId = creditedPlayerId == null ? Optional.empty() : creditedPlayerId;
        scope = Objects.requireNonNull(scope);
        scopeId = scopeId == null ? Optional.empty() : scopeId;
        if (scope == CombatStackScope.INSTANCE_SESSION && scopeId.isEmpty()) {
            throw new IllegalArgumentException("Instance stack applications require a scope ID");
        }
        if (scope != CombatStackScope.INSTANCE_SESSION && scopeId.isPresent()) {
            throw new IllegalArgumentException("Only instance stack applications may carry a scope ID");
        }
    }

    public static StackApplication ephemeral(Optional<UUID> sourceEntityId, Optional<UUID> creditedPlayerId) {
        return new StackApplication(sourceEntityId, creditedPlayerId, CombatStackScope.EPHEMERAL_COMBAT, Optional.empty());
    }

    public static StackApplication unattributed() {
        return ephemeral(Optional.empty(), Optional.empty());
    }
}
