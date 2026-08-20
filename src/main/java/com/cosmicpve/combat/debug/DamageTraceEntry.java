package com.cosmicpve.combat.debug;

import com.cosmicpve.combat.api.CombatResult;
import java.util.Optional;
import java.util.UUID;

public record DamageTraceEntry(CombatResult result) implements CombatTraceEntry {
    @Override
    public Optional<UUID> attributedPlayerId() {
        return result.context().attributedPlayerId();
    }
}
