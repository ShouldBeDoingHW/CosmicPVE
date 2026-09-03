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
import com.cosmicpve.combat.pipeline.PreDefenseBoundsContributorRegistry;
import com.cosmicpve.combat.memory.RecentCombatMemoryService;
import com.cosmicpve.combat.memory.RecentCombatMemoryEventBridge;
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
import com.cosmicpve.combat.enchantment.AegisBehavior;
import com.cosmicpve.combat.enchantment.EagleEyeBehavior;
import com.cosmicpve.combat.enchantment.RageBehavior;
import com.cosmicpve.combat.enchantment.NutritionFoodService;
import com.cosmicpve.combat.enchantment.DeathPactBehavior;
import com.cosmicpve.combat.enchantment.ObsidianshieldBehavior;
import com.cosmicpve.combat.enchantment.PermafrostBehavior;
import com.cosmicpve.combat.enchantment.DivineImmolationBehavior;
import com.cosmicpve.combat.enchantment.SelfDestructEventBridge;
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
import com.cosmicpve.equipment.skin.WeaponSkinResolver;
import com.cosmicpve.equipment.skin.WeaponSkinCombatResolver;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import com.cosmicpve.equipment.mask.MaskResolver;
import com.cosmicpve.equipment.mask.MaskCombatResolver;
import com.cosmicpve.equipment.mask.MaskRuntimeEventBridge;

public final class CosmicCombat {
    private static final CombatEngine ENGINE = new CombatEngine();
    private static final AttackSequenceService SEQUENCES = new AttackSequenceService();
    private static final DamageAttributionService ATTRIBUTION = new DamageAttributionService();
    private static final CombatTraceService TRACES = new CombatTraceService();
    private static final EffectiveEnchantmentsResolver ENCHANTMENTS = new EffectiveEnchantmentsResolver();
    private static final com.cosmicpve.combat.enchantment.EquippedPersistentEffectEventBridge PERSISTENT_EFFECTS =
            new com.cosmicpve.combat.enchantment.EquippedPersistentEffectEventBridge(
                    new com.cosmicpve.combat.enchantment.EquippedPersistentEffectService(ENCHANTMENTS));
    private static final CooldownService COOLDOWNS = new CooldownService();
    private static final ProcTraceService PROC_TRACES = new ProcTraceService();
    private static final ProcCandidateSourceRegistry PROC_SOURCES = new ProcCandidateSourceRegistry();
    private static final ProcModifierSourceRegistry PROC_MODIFIERS = new ProcModifierSourceRegistry();
    private static final OutgoingDamageContributorRegistry OUTGOING = new OutgoingDamageContributorRegistry();
    private static final IncomingDamageContributorRegistry INCOMING = new IncomingDamageContributorRegistry();
    private static final PreDefenseBoundsContributorRegistry PRE_DEFENSE_BOUNDS =
            new PreDefenseBoundsContributorRegistry();
    private static final RecentCombatMemoryService RECENT_COMBAT_MEMORY = new RecentCombatMemoryService(200L);
    private static final RecentCombatMemoryEventBridge RECENT_COMBAT_MEMORY_EVENTS =
            new RecentCombatMemoryEventBridge(RECENT_COMBAT_MEMORY);
    private static final com.cosmicpve.equipment.armor.ArmorSetSuppressionService ARMOR_SET_SUPPRESSION =
            new com.cosmicpve.equipment.armor.ArmorSetSuppressionService();
    private static final ArmorSetResolver ARMOR_SETS =
            new ArmorSetResolver(CosmicContent.repository(), ARMOR_SET_SUPPRESSION);
    private static final com.cosmicpve.reward.lootbox.SignatureWeaponCombatService SIGNATURE_WEAPONS =
            new com.cosmicpve.reward.lootbox.SignatureWeaponCombatService(ARMOR_SETS);
    private static final com.cosmicpve.activity.ActivityContextService ACTIVITIES =
            new com.cosmicpve.activity.ActivityContextService();
    private static final ArmorSetCombatContributor ARMOR_SET_COMBAT =
            new ArmorSetCombatContributor(ARMOR_SETS, ACTIVITIES);
    private static final ArmorSetImmunityResolver ARMOR_SET_IMMUNITIES = new ArmorSetImmunityResolver(ARMOR_SETS);
    private static final MaskResolver MASKS = new MaskResolver(CosmicContent.repository());
    private static final com.cosmicpve.equipment.armor.CosmicMovementBonusService MOVEMENT =
            new com.cosmicpve.equipment.armor.CosmicMovementBonusService(ARMOR_SETS, MASKS);
    private static final ArmorSetEventBridge ARMOR_SET_EVENTS = new ArmorSetEventBridge(
            ARMOR_SET_IMMUNITIES, ARMOR_SETS,
            new com.cosmicpve.equipment.armor.ArmorSetAttributeService(ARMOR_SETS, MOVEMENT));
    private static final ProcEngine PROCS = new ProcEngine(COOLDOWNS, PROC_TRACES);
    private static final ProcEventService PROC_EVENTS =
            new ProcEventService(PROCS, PROC_SOURCES, SEQUENCES, ENCHANTMENTS, PROC_MODIFIERS);
    private static final TrueDamageDeliveryService TRUE_DAMAGE = new TrueDamageDeliveryService();
    private static final ChildCombatActionService CHILD_ACTIONS = new ChildCombatActionService(SEQUENCES, TRUE_DAMAGE);
    private static final CombatStackService STACKS = new CombatStackService(CosmicContent.repository());
    private static final BleedRuntimeService BLEED_RUNTIME = new BleedRuntimeService(CHILD_ACTIONS);
    private static final WeaponSkinResolver WEAPON_SKINS = new WeaponSkinResolver();
    private static final WeaponSkinCombatResolver WEAPON_SKIN_COMBAT =
            new WeaponSkinCombatResolver(WEAPON_SKINS, STACKS, CHILD_ACTIONS);
    private static final com.cosmicpve.combat.enchantment.SoulTetherService SOUL_TETHERS =
            new com.cosmicpve.combat.enchantment.SoulTetherService();
    private static final com.cosmicpve.combat.enchantment.SnareRootService SNARE_ROOTS =
            new com.cosmicpve.combat.enchantment.SnareRootService();
    private static final com.cosmicpve.combat.enchantment.SnareEventBridge SNARE_EVENTS =
            new com.cosmicpve.combat.enchantment.SnareEventBridge(SNARE_ROOTS);
    private static final com.cosmicpve.combat.enchantment.ProjectileImpactContextService PROJECTILE_IMPACTS =
            new com.cosmicpve.combat.enchantment.ProjectileImpactContextService();
    private static final CosmicEnchantmentBehaviorResolver ENCHANTMENT_BEHAVIORS =
            new CosmicEnchantmentBehaviorResolver(
                    CHILD_ACTIONS, STACKS, BLEED_RUNTIME, ARMOR_SET_IMMUNITIES, COOLDOWNS, SOUL_TETHERS,
                    ARMOR_SETS, ARMOR_SET_SUPPRESSION, SNARE_ROOTS);
    private static final NutritionFoodService NUTRITION = new NutritionFoodService(ENCHANTMENTS);
    private static final ExecutionService EXECUTIONS = new ExecutionService(SEQUENCES, TRACES);
    private static final CombatEventBridge EVENTS =
            new CombatEventBridge(ENGINE, ATTRIBUTION, SEQUENCES, TRACES, ENCHANTMENTS, PROC_EVENTS,
                    OUTGOING, INCOMING, PRE_DEFENSE_BOUNDS, RECENT_COMBAT_MEMORY, WEAPON_SKINS, SIGNATURE_WEAPONS);
    private static final ProcHookEventBridge PROC_HOOKS =
            new ProcHookEventBridge(PROC_EVENTS, EXECUTIONS, NUTRITION, SOUL_TETHERS);
    private static final CombatStackEventBridge STACK_EVENTS = new CombatStackEventBridge(STACKS, BLEED_RUNTIME);
    private static final MaskCombatResolver MASK_COMBAT = new MaskCombatResolver(MASKS);
    private static final MaskRuntimeEventBridge MASK_EVENTS = new MaskRuntimeEventBridge(MASKS, PROC_EVENTS, ENCHANTMENTS);
    private static final com.cosmicpve.combat.enchantment.DodgeProcResolver DODGE =
            new com.cosmicpve.combat.enchantment.DodgeProcResolver(MASKS);
    private static final SelfDestructEventBridge SELF_DESTRUCT_EVENTS = new SelfDestructEventBridge();
    private static final com.cosmicpve.combat.enchantment.LethalSniperBehavior LETHAL_SNIPER =
            new com.cosmicpve.combat.enchantment.LethalSniperBehavior(PROJECTILE_IMPACTS);
    private static final com.cosmicpve.combat.enchantment.EternalSnareBehavior ETERNAL_SNARE =
            new com.cosmicpve.combat.enchantment.EternalSnareBehavior(SNARE_ROOTS);
    private static final com.cosmicpve.combat.enchantment.SpiritLinkBehavior SPIRIT_LINK =
            new com.cosmicpve.combat.enchantment.SpiritLinkBehavior();

    private CosmicCombat() {}

    public static void register() {
        var deathPact = new DeathPactBehavior(ENCHANTMENTS);
        var permafrost = new PermafrostBehavior(STACKS);
        var hex = new com.cosmicpve.combat.enchantment.HexBehavior(STACKS);
        var playerUpgrades = new com.cosmicpve.upgrade.PlayerUpgradeCombatContributor();
        OUTGOING.register(new ExecuteBehavior());
        OUTGOING.register(new com.cosmicpve.combat.enchantment.PermanentExecuteBehavior());
        OUTGOING.register(new GreatswordBehavior());
        OUTGOING.register(new InsanityBehavior());
        OUTGOING.register(new EagleEyeBehavior());
        OUTGOING.register(new com.cosmicpve.combat.enchantment.SniperBehavior(PROJECTILE_IMPACTS));
        OUTGOING.register(LETHAL_SNIPER);
        OUTGOING.register(ETERNAL_SNARE);
        OUTGOING.register(new RageBehavior(RECENT_COMBAT_MEMORY));
        OUTGOING.register(ARMOR_SET_COMBAT);
        OUTGOING.register(WEAPON_SKIN_COMBAT);
        OUTGOING.register(deathPact);
        OUTGOING.register(permafrost);
        OUTGOING.register(new DivineImmolationBehavior(COOLDOWNS));
        OUTGOING.register(SOUL_TETHERS);
        OUTGOING.register(new com.cosmicpve.combat.enchantment.LeadershipBehavior());
        OUTGOING.register(new com.cosmicpve.combat.enchantment.HeroKillerBehavior(ARMOR_SETS));
        OUTGOING.register(new com.cosmicpve.combat.enchantment.VoodooBehavior(STACKS));
        OUTGOING.register(MASK_COMBAT);
        OUTGOING.register(new com.cosmicpve.combat.enchantment.DominateBehavior());
        OUTGOING.register(hex);
        OUTGOING.register(SPIRIT_LINK);
        OUTGOING.register(playerUpgrades);
        INCOMING.register(ARMOR_SET_COMBAT);
        INCOMING.register(WEAPON_SKIN_COMBAT);
        INCOMING.register(deathPact);
        INCOMING.register(new com.cosmicpve.equipment.armor.CategoryDamageReductionBehavior(ARMOR_SETS, ENCHANTMENTS));
        INCOMING.register(permafrost);
        INCOMING.register(MASK_COMBAT);
        INCOMING.register(hex);
        INCOMING.register(playerUpgrades);
        PRE_DEFENSE_BOUNDS.register(new AegisBehavior(ENCHANTMENTS));
        PROC_MODIFIERS.register(new LuckBehavior());
        PROC_MODIFIERS.register(new ArmorSetProcModifierResolver(ARMOR_SETS));
        PROC_SOURCES.register(ENCHANTMENT_BEHAVIORS);
        PROC_SOURCES.register(WEAPON_SKIN_COMBAT);
        PROC_SOURCES.register(DODGE);
        PROC_SOURCES.register(LETHAL_SNIPER);
        PROC_SOURCES.register(SPIRIT_LINK);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, SELF_DESTRUCT_EVENTS::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(EVENTS::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(EVENTS::onDamageAccepted);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, MASK_EVENTS::onTargeted);
        NeoForge.EVENT_BUS.addListener(EVENTS::onDamageCommitted);
        NeoForge.EVENT_BUS.addListener(PROC_HOOKS::onPreDeath);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, PROC_HOOKS::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(PROC_HOOKS::onFoodEaten);
        NeoForge.EVENT_BUS.addListener(PROC_HOOKS::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(PROC_HOOKS::onEntityTick);
        NeoForge.EVENT_BUS.addListener(SNARE_EVENTS::onEntityTick);
        NeoForge.EVENT_BUS.addListener(PROJECTILE_IMPACTS::onImpact);
        NeoForge.EVENT_BUS.addListener(STACK_EVENTS::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(STACK_EVENTS::onEntityTick);
        NeoForge.EVENT_BUS.addListener(STACK_EVENTS::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(ARMOR_SET_EVENTS::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(ARMOR_SET_EVENTS::onEntityTick);
        NeoForge.EVENT_BUS.addListener(ARMOR_SET_EVENTS::onKnockback);
        NeoForge.EVENT_BUS.addListener(PERSISTENT_EFFECTS::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(PERSISTENT_EFFECTS::onEntityTick);
        NeoForge.EVENT_BUS.addListener(MASK_EVENTS::onEntityTick);
        NeoForge.EVENT_BUS.addListener(MASK_EVENTS::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(MASK_EVENTS::onEffectApplicable);
        NeoForge.EVENT_BUS.addListener(RECENT_COMBAT_MEMORY_EVENTS::onServerTick);
        NeoForge.EVENT_BUS.addListener(SELF_DESTRUCT_EVENTS::onExplosion);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, SPIRIT_LINK::onDeath);
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
    public static com.cosmicpve.activity.ActivityContextService activities() { return ACTIVITIES; }
    public static com.cosmicpve.equipment.armor.CosmicMovementBonusService movement() { return MOVEMENT; }
    public static com.cosmicpve.equipment.armor.ArmorSetSuppressionService armorSetSuppression() {
        return ARMOR_SET_SUPPRESSION;
    }
    public static ArmorSetImmunityResolver armorSetImmunities() { return ARMOR_SET_IMMUNITIES; }
    public static RecentCombatMemoryService recentCombatMemory() { return RECENT_COMBAT_MEMORY; }
    public static WeaponSkinResolver weaponSkins() { return WEAPON_SKINS; }
}
