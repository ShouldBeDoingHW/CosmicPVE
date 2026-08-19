package com.cosmicpve.combat.debug;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.WeaponSnapshot;
import com.cosmicpve.combat.pipeline.CombatCalculationRequest;
import com.cosmicpve.combat.pipeline.CombatEngine;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CombatTraceServiceTest {
    @Test
    void retainsOnlyEnabledPlayersCommittedHit() {
        UUID playerId = UUID.randomUUID();
        var context = new CombatContext(
                null, null, null, null, Optional.of(playerId), null,
                AttackCategory.MELEE, DamageChannel.ORDINARY, Set.of(), WeaponSnapshot.empty(),
                42L, OptionalLong.empty(), RecursionPolicy.NORMAL);
        var result = new CombatEngine().calculate(context, CombatCalculationRequest.unchanged(8.0)).commit(5.25);
        var traces = new CombatTraceService();

        traces.setEnabled(playerId, true);
        traces.recordCommitted(result);

        String formatted = traces.format(traces.last(playerId).orElseThrow());
        assertTrue(formatted.contains("sequence=42"));
        assertTrue(formatted.contains("base=8.000"));
        assertTrue(formatted.contains("committedHealth=5.250"));
    }
}
