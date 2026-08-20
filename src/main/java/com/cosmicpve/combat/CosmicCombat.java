package com.cosmicpve.combat;

import com.cosmicpve.combat.attribution.DamageAttributionService;
import com.cosmicpve.combat.action.ChildCombatActionService;
import com.cosmicpve.combat.action.TrueDamageDeliveryService;
import com.cosmicpve.combat.debug.CombatTraceService;
import com.cosmicpve.combat.event.CombatEventBridge;
import com.cosmicpve.combat.execution.ExecutionService;
import com.cosmicpve.combat.pipeline.AttackSequenceService;
import com.cosmicpve.combat.pipeline.CombatEngine;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributorRegistry;
import com.cosmicpve.combat.pipeline.IncomingDamageContributorRegistry;
import com.cosmicpve.equipment.armor.ArmorSetResolver;
import com.cosmicpve.equipment.armor.ArmorSetCombatContributor;
import com.cosmicpve.equipment.armor.ArmorSetProcModifierResolver;
import com.cosmicpve.equipment.armor.ArmorSetImmunityResolver;
import com.cosmicpve.equipment.armor.ArmorSetEventBridge;
import com.cosmicpve.combat.enchantment.CosmicEnchantmentBehaviorResolver;
import com.cosmicpve.combat.enchantment.ExecuteBehavior;
import com.cosmicpve.combat.enchantment.LuckBehavior;
import com.cosmicpve.combat.enchantment.GreatswordBehavior;
import com.cosmicpve.combat.enchantment.InsanityBehavior;
import com.cosmicpve.combat.cooldown.CooldownService;
import com.cosmicpve.combat.proc.ProcCandidateSourceRegistry;
import com.cosmicpve.combat.proc.ProcEngine;
import com.cosmicpve.combat.proc.ProcEventService;
import com.cosmicpve.combat.proc.ProcHookEventBridge;
import com.cosmicpve.combat.proc.ProcModifierSourceRegistry;
import com.cosmicpve.combat.proc.ProcTraceService;
import com.cosmicpve.combat.stack.CombatStackEventBridge;
import com.cosmicpve.combat.stack.CombatStackService;
import com.cosmicpve.combat.stack.BleedRuntimeService;
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
    private static final ProcModifierSourceRegistry PROC_MODIFIERS = new ProcModifierSourceRegistry();
    private static final OutgoingDamageContributorRegistry OUTGOING = new OutgoingDamageContributorRegistry();
    private static final IncomingDamageContributorRegistry INCOMING = new IncomingDamageContributorRegistry();
    private static final ArmorSetResolver ARMOR_SETS = new ArmorSetResolver(CosmicContent.repository());
    private static final ArmorSetCombatContributor ARMOR_SET_COMBAT = new ArmorSetCombatContributor(ARMOR_SETS);
    private static final ArmorSetImmunityResolver ARMOR_SET_IMMUNITIES = new ArmorSetImmunityResolver(ARMOR_SETS);
    private static final ArmorSetEventBridge ARMOR_SET_EVENTS = new ArmorSetEventBridge(ARMOR_SET_IMMUNITIES);
    private static final ProcEngine PROCS = new ProcEngine(COOLDOWNS, PROC_TRACES);
    private static final ProcEventService PROC_EVENTS =
            new ProcEventService(PROCS, PROC_SOURCES, SEQUENCES, ENCHANTMENTS, PROC_MODIFIERS);
    private static final TrueDamageDeliveryService TRUE_DAMAGE = new TrueDamageDeliveryService();
    private static final ChildCombatActionService CHILD_ACTIONS = new ChildCombatActionService(SEQUENCES, TRUE_DAMAGE);
    private static final CombatStackService STACKS = new CombatStackService(CosmicContent.repository());
    private static final BleedRuntimeService BLEED_RUNTIME = new BleedRuntimeService(CHILD_ACTIONS);
    private static final CosmicEnchantmentBehaviorResolver ENCHANTMENT_BEHAVIORS =
            new CosmicEnchantmentBehaviorResolver(CHILD_ACTIONS, STACKS, BLEED_RUNTIME);
    private static final ExecutionService EXECUTIONS = new ExecutionService(SEQUENCES, TRACES);
    private static final CombatEventBridge EVENTS =
            new CombatEventBridge(ENGINE, ATTRIBUTION, SEQUENCES, TRACES, ENCHANTMENTS, PROC_EVENTS, OUTGOING, INCOMING);
    private static final ProcHookEventBridge PROC_HOOKS = new ProcHookEventBridge(PROC_EVENTS, EXECUTIONS);
    private static final CombatStackEventBridge STACK_EVENTS = new CombatStackEventBridge(STACKS, BLEED_RUNTIME);

    private CosmicCombat() {}

    public static void register() {
        OUTGOING.register(new ExecuteBehavior());
        OUTGOING.register(new GreatswordBehavior());
        OUTGOING.register(new InsanityBehavior());
        OUTGOING.register(ARMOR_SET_COMBAT);
        INCOMING.register(ARMOR_SET_COMBAT);
        PROC_MODIFIERS.register(new LuckBehavior());
        PROC_MODIFIERS.register(new ArmorSetProcModifierResolver(ARMOR_SETS));
        PROC_SOURCES.register(ENCHANTMENT_BEHAVIORS);
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
        NeoForge.EVENT_BUS.addListener(ARMOR_SET_EVENTS::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(ARMOR_SET_EVENTS::onEntityTick);
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

    public static ArmorSetResolver armorSets() { return ARMOR_SETS; }
    public static ArmorSetImmunityResolver armorSetImmunities() { return ARMOR_SET_IMMUNITIES; }
}
