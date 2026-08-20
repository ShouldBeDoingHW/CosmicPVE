package com.cosmicpve.combat.proc;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantments;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProcTraceServiceTest {
    @Test
    void exposesRollChanceCooldownAndFilteringDetails() {
        UUID playerId = UUID.randomUUID();
        var event = new ProcEvent(
                ProcHook.ON_VALID_HIT, 50, OptionalLong.of(49), RecursionPolicy.NORMAL,
                playerId, Optional.of(playerId), 100, List.of(1.2),
                Map.of(CosmicPVE.id("luck"), 1.2), List.of(1.0), Set.of(),
                EffectiveEnchantments.EMPTY, Optional.empty(), null, null, () -> 0.011);
        var evaluation = new ProcEvaluation(
                CosmicPVE.id("test/proc"), ProcHook.ON_VALID_HIT, 0.01, 1.2, 0.012,
                java.util.OptionalDouble.of(0.011), ProcEvaluationStatus.ACTIVATED, true,
                new ProcProvenance(ProcSourceKind.DEVELOPMENT, CosmicPVE.id("test/source")));
        var traces = new ProcTraceService();
        traces.setEnabled(playerId, true);
        traces.record(new ProcDispatchResult(event, List.of(evaluation)));

        String formatted = traces.format(traces.last(playerId).orElseThrow());
        assertTrue(formatted.contains("sequence=50 parent=49"));
        assertTrue(formatted.contains("chanceModifiers={cosmicpve:luck=1.2}"));
        assertTrue(formatted.contains("base=0.010000"));
        assertTrue(formatted.contains("multiplier=1.200000"));
        assertTrue(formatted.contains("final=0.012000"));
        assertTrue(formatted.contains("roll=0.011000"));
        assertTrue(formatted.contains("status=ACTIVATED"));
        assertTrue(formatted.contains("cooldownReady=true"));
    }
}
