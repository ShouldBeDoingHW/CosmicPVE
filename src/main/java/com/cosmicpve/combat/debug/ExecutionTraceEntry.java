package com.cosmicpve.combat.debug;

import com.cosmicpve.combat.execution.ExecutionResult;
import java.util.Optional;
import java.util.UUID;

public record ExecutionTraceEntry(ExecutionResult result) implements CombatTraceEntry {
    @Override
    public Optional<UUID> attributedPlayerId() {
        return result.attributedPlayerId();
    }
}
