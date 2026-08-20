package com.cosmicpve.combat.debug;

import java.util.Optional;
import java.util.UUID;

public sealed interface CombatTraceEntry permits DamageTraceEntry, ExecutionTraceEntry {
    Optional<UUID> attributedPlayerId();
}
