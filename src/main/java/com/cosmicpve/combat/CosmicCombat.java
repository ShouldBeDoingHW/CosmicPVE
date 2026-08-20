package com.cosmicpve.combat;

import com.cosmicpve.combat.attribution.DamageAttributionService;
import com.cosmicpve.combat.action.ChildCombatActionService;
import com.cosmicpve.combat.action.TrueDamageDeliveryService;
import com.cosmicpve.combat.debug.CombatTraceService;
import com.cosmicpve.combat.event.CombatEventBridge;
import com.cosmicpve.combat.execution.ExecutionService;
import com.cosmicpve.combat.pipeline.AttackSequenceService;
import com.cosmicpve.combat.pipeline.CombatEngine;
import com.cosmicpve.combat.cooldown.CooldownService;
import com.cosmicpve.combat.proc.ProcCandidateSourceRegistry;
import com.cosmicpve.combat.proc.ProcEngine;
import com.cosmicpve.combat.proc.ProcEventService;
import com.cosmicpve.combat.proc.ProcHookEventBridge;
import com.cosmicpve.combat.proc.ProcTraceService;
import com.cosmicpve.combat.stack.CombatStackEventBridge;
import com.cosmicpve.combat.stack.CombatStackService;
import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;

public final class CosmicCombat {
    private static final CombatEngine ENGINE = new CombatEngine();
    private static final AttackSequenceService SEQUENCES = new AttackSequenceService();
    private static final DamageAttributionService ATTRIBUTION = new DamageAttributionService();
    private static final CombatTraceService TRACES = new CombatTraceService();
    private static final EffectiveEnchantmentsResolver ENCHANTMENTS = new EffectiveEnchantmentsResolver();
    private static final CooldownService COOLDOWNS = new CooldownService();
    private static final ProcTraceService PROC_TRACES = new ProcTraceService();
    private static final ProcCandidateSourceRegistry PROC_SOURCES = new ProcCandidateSourceRegistry();
    private static final ProcEngine PROCS = new ProcEngine(COOLDOWNS, PROC_TRACES);
    private static final ProcEventService PROC_EVENTS =
            new ProcEventService(PROCS, PROC_SOURCES, SEQUENCES, ENCHANTMENTS);
    private static final TrueDamageDeliveryService TRUE_DAMAGE = new TrueDamageDeliveryService();
    private static final ChildCombatActionService CHILD_ACTIONS = new ChildCombatActionService(SEQUENCES, TRUE_DAMAGE);
    private static final ExecutionService EXECUTIONS = new ExecutionService(SEQUENCES, TRACES);
    private static final CombatEventBridge EVENTS =
            new CombatEventBridge(ENGINE, ATTRIBUTION, SEQUENCES, TRACES, ENCHANTMENTS, PROC_EVENTS);
    private static final ProcHookEventBridge PROC_HOOKS = new ProcHookEventBridge(PROC_EVENTS, EXECUTIONS);
    private static final CombatStackService STACKS = new CombatStackService(CosmicContent.repository());
    private static final CombatStackEventBridge STACK_EVENTS = new CombatStackEventBridge(STACKS);

    private CosmicCombat() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EVENTS::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(EVENTS::onDamageAccepted);
        NeoForge.EVENT_BUS.addListener(EVENTS::onDamageCommitted);
        NeoForge.EVENT_BUS.addListener(PROC_HOOKS::onPreDeath);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, PROC_HOOKS::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(PROC_HOOKS::onFoodEaten);
        NeoForge.EVENT_BUS.addListener(PROC_HOOKS::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(PROC_HOOKS::onEntityTick);
        NeoForge.EVENT_BUS.addListener(STACK_EVENTS::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(STACK_EVENTS::onEntityTick);
        NeoForge.EVENT_BUS.addListener(STACK_EVENTS::onPlayerClone);
    }

    public static CombatTraceService traces() {
        return TRACES;
    }

    public static AttackSequenceService sequences() {
        return SEQUENCES;
    }

    public static ChildCombatActionService childActions() {
        return CHILD_ACTIONS;
    }

    public static ExecutionService executions() {
        return EXECUTIONS;
    }

    public static EffectiveEnchantmentsResolver enchantments() {
        return ENCHANTMENTS;
    }

    public static CooldownService cooldowns() {
        return COOLDOWNS;
    }

    public static ProcEngine procs() {
        return PROCS;
    }

    public static ProcEventService procEvents() {
        return PROC_EVENTS;
    }

    public static ProcCandidateSourceRegistry procSources() {
        return PROC_SOURCES;
    }

    public static ProcTraceService procTraces() {
        return PROC_TRACES;
    }

    public static CombatStackService stacks() {
        return STACKS;
    }
}
