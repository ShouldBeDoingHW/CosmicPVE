package com.cosmicpve.combat.stack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;

/** One independently attributable stack unit. No definition values are duplicated here. */
public record CombatStackInstance(
        UUID instanceId,
        Identifier definitionId,
        long definitionRevision,
        Optional<UUID> originalSourceEntityId,
        Optional<UUID> creditedPlayerId,
        long applicationTick,
        long lastRefreshTick,
        long expirationTick,
        CombatStackScope scope,
        Optional<Identifier> scopeId,
        Optional<UUID> lastTransferredBy,
        Optional<Long> lastTransferTick,
        int potency) {
    public CombatStackInstance(UUID instanceId, Identifier definitionId, long definitionRevision,
            Optional<UUID> originalSourceEntityId, Optional<UUID> creditedPlayerId,
            long applicationTick, long lastRefreshTick, long expirationTick,
            CombatStackScope scope, Optional<Identifier> scopeId,
            Optional<UUID> lastTransferredBy, Optional<Long> lastTransferTick) {
        this(instanceId, definitionId, definitionRevision, originalSourceEntityId, creditedPlayerId,
                applicationTick, lastRefreshTick, expirationTick, scope, scopeId,
                lastTransferredBy, lastTransferTick, 0);
    }
    public static final Comparator<CombatStackInstance> EXPIRATION_ORDER = Comparator
            .comparingLong(CombatStackInstance::expirationTick)
            .thenComparingLong(CombatStackInstance::applicationTick)
            .thenComparing(instance -> instance.instanceId().toString());

    public static final Codec<CombatStackInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("instance_id").forGetter(CombatStackInstance::instanceId),
            Identifier.CODEC.fieldOf("definition_id").forGetter(CombatStackInstance::definitionId),
            Codec.LONG.fieldOf("definition_revision").forGetter(CombatStackInstance::definitionRevision),
            UUIDUtil.CODEC.optionalFieldOf("original_source_entity_id").forGetter(CombatStackInstance::originalSourceEntityId),
            UUIDUtil.CODEC.optionalFieldOf("credited_player_id").forGetter(CombatStackInstance::creditedPlayerId),
            Codec.LONG.fieldOf("application_tick").forGetter(CombatStackInstance::applicationTick),
            Codec.LONG.fieldOf("last_refresh_tick").forGetter(CombatStackInstance::lastRefreshTick),
            Codec.LONG.fieldOf("expiration_tick").forGetter(CombatStackInstance::expirationTick),
            CombatStackScope.CODEC.fieldOf("scope").forGetter(CombatStackInstance::scope),
            Identifier.CODEC.optionalFieldOf("scope_id").forGetter(CombatStackInstance::scopeId),
            UUIDUtil.CODEC.optionalFieldOf("last_transferred_by").forGetter(CombatStackInstance::lastTransferredBy),
            Codec.LONG.optionalFieldOf("last_transfer_tick").forGetter(CombatStackInstance::lastTransferTick),
            Codec.INT.optionalFieldOf("potency", 0).forGetter(CombatStackInstance::potency)
    ).apply(instance, CombatStackInstance::new));

    public CombatStackInstance {
        instanceId = Objects.requireNonNull(instanceId);
        definitionId = Objects.requireNonNull(definitionId);
        if (definitionRevision < 0 || applicationTick < 0 || lastRefreshTick < applicationTick
                || expirationTick <= applicationTick) {
            throw new IllegalArgumentException("Invalid combat stack timing or definition revision");
        }
        originalSourceEntityId = originalSourceEntityId == null ? Optional.empty() : originalSourceEntityId;
        creditedPlayerId = creditedPlayerId == null ? Optional.empty() : creditedPlayerId;
        scope = Objects.requireNonNull(scope);
        scopeId = scopeId == null ? Optional.empty() : scopeId;
        lastTransferredBy = lastTransferredBy == null ? Optional.empty() : lastTransferredBy;
        lastTransferTick = lastTransferTick == null ? Optional.empty() : lastTransferTick;
        if (potency < 0) throw new IllegalArgumentException("Stack potency cannot be negative");
        if (scope == CombatStackScope.INSTANCE_SESSION && scopeId.isEmpty()) {
            throw new IllegalArgumentException("Instance stack state requires a scope ID");
        }
        if (scope != CombatStackScope.INSTANCE_SESSION && scopeId.isPresent()) {
            throw new IllegalArgumentException("Only instance stack state may carry a scope ID");
        }
    }

    CombatStackInstance refreshed(long revision, long tick, long newExpirationTick, StackApplication application) {
        return new CombatStackInstance(
                instanceId, definitionId, revision,
                application.potency() > 0 ? application.sourceEntityId() : originalSourceEntityId,
                application.potency() > 0 ? application.creditedPlayerId() : creditedPlayerId,
                application.potency() > 0 ? tick : applicationTick,
                tick, newExpirationTick, scope, scopeId, lastTransferredBy, lastTransferTick,
                application.potency() > 0 ? application.potency() : potency);
    }

    CombatStackInstance transferred(Optional<UUID> actorId, long tick, long preservedExpirationTick) {
        return new CombatStackInstance(
                instanceId, definitionId, definitionRevision, originalSourceEntityId, creditedPlayerId,
                applicationTick, lastRefreshTick, preservedExpirationTick, scope, scopeId, actorId, Optional.of(tick), potency);
    }
}
