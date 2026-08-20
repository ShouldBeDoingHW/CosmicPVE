package com.cosmicpve.combat.debug;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.WeaponSnapshot;
import com.cosmicpve.combat.pipeline.CombatCalculationRequest;
import com.cosmicpve.combat.pipeline.CombatEngine;
import com.cosmicpve.combat.api.DamageBounds;
import com.cosmicpve.combat.api.TrueDamagePacket;
import com.cosmicpve.combat.execution.ExecutionCause;
import com.cosmicpve.combat.execution.ExecutionResult;
import com.cosmicpve.combat.pipeline.CombatCalculationRequest;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantments;
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
                EffectiveEnchantments.EMPTY,
                42L, OptionalLong.empty(), RecursionPolicy.NORMAL);
        var result = new CombatEngine().calculate(context, CombatCalculationRequest.unchanged(8.0)).commit(5.25);
        var traces = new CombatTraceService();

        traces.setEnabled(playerId, true);
        traces.recordCommitted(result);

        String formatted = traces.format(traces.last(playerId).orElseThrow());
        assertTrue(formatted.contains("type=ORDINARY"));
        assertTrue(formatted.contains("sequence=42"));
        assertTrue(formatted.contains("base=8.000"));
        assertTrue(formatted.contains("committedHealth=5.250"));
    }

    @Test
    void distinguishesTrueChildAndExecutionTraces() {
        UUID playerId = UUID.randomUUID();
        var context = new CombatContext(
                null, null, null, null, Optional.of(playerId), null,
                AttackCategory.UNKNOWN, DamageChannel.TRUE, Set.of(), WeaponSnapshot.empty(),
                EffectiveEnchantments.EMPTY, 43L, OptionalLong.of(42L), RecursionPolicy.NO_PROCS);
        var packet = TrueDamagePacket.standard(com.cosmicpve.CosmicPVE.id("test_true"), 2.0);
        var trueResult = new CombatEngine().calculate(
                context,
                new CombatCalculationRequest(
                        0.0, 0.0, java.util.List.of(), DamageBounds.UNBOUNDED,
                        java.util.List.of(), DamageBounds.UNBOUNDED, java.util.List.of(packet)))
                .commit(2.0);
        var traces = new CombatTraceService();
        traces.setEnabled(playerId, true);
        traces.recordCommitted(trueResult);

        String trueTrace = traces.format(traces.last(playerId).orElseThrow());
        assertTrue(trueTrace.contains("type=TRUE"));
        assertTrue(trueTrace.contains("sequence=43 parent=42"));
        assertTrue(trueTrace.contains("policy=NO_PROCS"));

        traces.recordExecution(new ExecutionResult(
                new ExecutionCause(com.cosmicpve.CosmicPVE.id("test_execution")),
                UUID.randomUUID(), Optional.of(playerId), 44L, OptionalLong.of(43L), true));
        String executionTrace = traces.format(traces.last(playerId).orElseThrow());
        assertTrue(executionTrace.contains("type=EXECUTION"));
        assertTrue(executionTrace.contains("cause=cosmicpve:test_execution"));
    }
}
