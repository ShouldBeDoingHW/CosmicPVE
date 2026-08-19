package com.cosmicpve.combat;

import com.cosmicpve.combat.attribution.DamageAttributionService;
import com.cosmicpve.combat.debug.CombatTraceService;
import com.cosmicpve.combat.event.CombatEventBridge;
import com.cosmicpve.combat.pipeline.AttackSequenceService;
import com.cosmicpve.combat.pipeline.CombatEngine;
import net.neoforged.neoforge.common.NeoForge;

public final class CosmicCombat {
    private static final CombatEngine ENGINE = new CombatEngine();
    private static final AttackSequenceService SEQUENCES = new AttackSequenceService();
    private static final DamageAttributionService ATTRIBUTION = new DamageAttributionService();
    private static final CombatTraceService TRACES = new CombatTraceService();
    private static final CombatEventBridge EVENTS = new CombatEventBridge(ENGINE, ATTRIBUTION, SEQUENCES, TRACES);

    private CosmicCombat() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EVENTS::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(EVENTS::onDamageAccepted);
        NeoForge.EVENT_BUS.addListener(EVENTS::onDamageCommitted);
    }

    public static CombatTraceService traces() {
        return TRACES;
    }

    public static AttackSequenceService sequences() {
        return SEQUENCES;
    }
}
