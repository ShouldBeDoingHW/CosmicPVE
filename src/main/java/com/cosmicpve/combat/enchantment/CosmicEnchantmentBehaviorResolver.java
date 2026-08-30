package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.action.ChildCombatActionService;
import com.cosmicpve.combat.cooldown.CooldownScope;
import com.cosmicpve.combat.cooldown.CooldownService;
import com.cosmicpve.combat.proc.ChildProcEligibility;
import com.cosmicpve.combat.proc.ProcCandidate;
import com.cosmicpve.combat.proc.ProcCandidateResolver;
import com.cosmicpve.combat.proc.ProcEvent;
import com.cosmicpve.combat.proc.ProcHook;
import com.cosmicpve.combat.proc.ProcEngine;
import com.cosmicpve.combat.proc.ProcProvenance;
import com.cosmicpve.combat.proc.ProcSourceKind;
import com.cosmicpve.combat.stack.BleedRuntimeService;
import com.cosmicpve.combat.stack.CombatStackService;
import com.cosmicpve.combat.stack.StackApplication;
import com.cosmicpve.equipment.enchantment.EnchantmentSourceKind;
import com.cosmicpve.equipment.armor.ArmorSetImmunityResolver;
import com.cosmicpve.registry.ModEnchantments;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;

/** The deliberately small Java behavior-composition layer for the first five real enchantments. */
public final class CosmicEnchantmentBehaviorResolver implements ProcCandidateResolver {
    public static final Identifier ANGELIC_ONCE_KEY = CosmicPVE.id("angelic_once_per_damage");
    public static final Identifier ENDER_SHIFT_COOLDOWN = CosmicPVE.id("ender_shift");
    public static final Identifier MOLTEN_ONCE_KEY = CosmicPVE.id("molten_once_per_damage");
    public static final Identifier CACTUS_ONCE_KEY = CosmicPVE.id("cactus_once_per_damage");
    public static final Identifier PERMAFROST_ONCE_KEY = CosmicPVE.id("permafrost_once_per_damage");
    public static final Identifier MORTAL_COIL_ONCE_KEY = CosmicPVE.id("mortal_coil_once_per_damage");
    public static final Identifier SELF_DESTRUCT_ONCE_KEY = CosmicPVE.id("self_destruct_once_per_damage");
    public static final Identifier UNDEAD_RUSE_ONCE_KEY = CosmicPVE.id("undead_ruse_once_per_damage");

    private final ChildCombatActionService childActions;
    private final CombatStackService stacks;
    private final BleedRuntimeService bleedRuntime;
    private final ArmorSetImmunityResolver immunities;
    private final CooldownService cooldowns;
    private final SoulTetherService soulTethers;
    private final com.cosmicpve.equipment.armor.ArmorSetResolver armorSets;
    private final com.cosmicpve.equipment.armor.ArmorSetSuppressionService armorSetSuppression;
    private final SnareRootService snareRoots;

    public CosmicEnchantmentBehaviorResolver(
            ChildCombatActionService childActions,
            CombatStackService stacks,
            BleedRuntimeService bleedRuntime,
            ArmorSetImmunityResolver immunities,
            CooldownService cooldowns,
            SoulTetherService soulTethers,
            com.cosmicpve.equipment.armor.ArmorSetResolver armorSets,
            com.cosmicpve.equipment.armor.ArmorSetSuppressionService armorSetSuppression) {
        this(childActions, stacks, bleedRuntime, immunities, cooldowns, soulTethers, armorSets,
                armorSetSuppression, new SnareRootService());
    }

    public CosmicEnchantmentBehaviorResolver(
            ChildCombatActionService childActions,
            CombatStackService stacks,
            BleedRuntimeService bleedRuntime,
            ArmorSetImmunityResolver immunities,
            CooldownService cooldowns,
            SoulTetherService soulTethers,
            com.cosmicpve.equipment.armor.ArmorSetResolver armorSets,
            com.cosmicpve.equipment.armor.ArmorSetSuppressionService armorSetSuppression,
            SnareRootService snareRoots) {
        this.childActions = childActions;
        this.stacks = stacks;
        this.bleedRuntime = bleedRuntime;
        this.immunities = immunities;
        this.cooldowns = cooldowns;
        this.soulTethers = soulTethers;
        this.armorSets = armorSets;
        this.armorSetSuppression = armorSetSuppression;
        this.snareRoots = snareRoots;
    }

    public CosmicEnchantmentBehaviorResolver(
            ChildCombatActionService childActions, CombatStackService stacks, BleedRuntimeService bleedRuntime,
            ArmorSetImmunityResolver immunities, CooldownService cooldowns) {
        this(childActions, stacks, bleedRuntime, immunities, cooldowns, new SoulTetherService(), null, null);
    }

    @Override
    public List<ProcCandidate> resolve(ProcEvent event) {
        var result = new ArrayList<ProcCandidate>();
        if (event.hook() == ProcHook.ON_VALID_HIT) {
            addDoublestrike(event, result);
            addBleed(event, result);
            addPoison(event, result);
            addPummel(event, result);
            addBlessed(event, result);
            addTrap(event, result);
            addDivineImmolation(event, result);
            addObliterate(event, result);
            addSoulTether(event, result);
            addSoulSiphon(event, result);
            addBlackout(event, result);
            addVoodoo(event, result);
        } else if (event.hook() == ProcHook.ON_PROJECTILE_HIT) {
            addLightning(event, result);
            addVenom(event, result);
            addVirus(event, result);
            addSnare(event, result);
        } else if (event.hook() == ProcHook.ON_DAMAGE_TAKEN) {
            addAngelic(event, result);
            addEnderShift(event, result);
            addMolten(event, result);
            addCactus(event, result);
            addPermafrost(event, result);
            addMortalCoil(event, result);
            addSelfDestruct(event, result);
            addUndeadRuse(event, result);
            addPlagueCarrier(event, result);
        } else if (event.hook() == ProcHook.ON_PRE_DEATH) {
            addPhoenix(event, result);
        } else if (event.hook() == ProcHook.ON_PRE_DAMAGE_CALCULATION) {
            addDevour(event, result);
        }
        return List.copyOf(result);
    }

    private void addSoulSiphon(ProcEvent event, List<ProcCandidate> result) {
        int level = Math.min(4, event.effectiveEnchantments().level(ModEnchantments.SOUL_SIPHON.identifier()));
        if (level <= 0 || event.attacker() == null || event.target() == null) return;
        result.add(deterministicCandidate(ModEnchantments.SOUL_SIPHON.identifier(), ProcHook.ON_VALID_HIT,
                1.0, Optional.of(ModEnchantments.SOUL_SIPHON.identifier()), SoulSiphonBehavior.cooldownTicks(level),
                Optional.empty(), ChildProcEligibility.LIMITED_OFFENSIVE_REROLL,
                activation -> SoulSiphonBehavior.activate(activation.event().attacker()),
                provenance(event, ModEnchantments.SOUL_SIPHON.identifier()),
                condition(CosmicPVE.id("soul_siphon_below_half"), procEvent -> procEvent.target() != null
                        && SoulSiphonBehavior.targetBelowHalf(procEvent.target()) && ordinaryAttack(procEvent))));
    }

    private void addBlackout(ProcEvent event, List<ProcCandidate> result) {
        int level = Math.min(4, event.effectiveEnchantments().level(ModEnchantments.BLACKOUT.identifier()));
        if (level <= 0 || event.target() == null || armorSets == null || armorSetSuppression == null
                || armorSets.resolve(event.target()).isEmpty()) return;
        result.add(candidate(ModEnchantments.BLACKOUT.identifier(), ProcHook.ON_VALID_HIT,
                BlackoutBehavior.chance(level), Optional.empty(), 0L, Optional.empty(),
                ChildProcEligibility.LIMITED_OFFENSIVE_REROLL,
                activation -> BlackoutBehavior.activate(armorSetSuppression, activation.event().target(), level,
                        activation.event().serverTick()), provenance(event, ModEnchantments.BLACKOUT.identifier()),
                condition(CosmicPVE.id("blackout_active_armor_set"), procEvent -> procEvent.target() != null
                        && armorSets.resolve(procEvent.target()).isPresent() && ordinaryAttack(procEvent))));
    }

    private void addVoodoo(ProcEvent event, List<ProcCandidate> result) {
        if (event.attacker() == null || event.target() == null) return;
        int level = Math.min(6, EnchantmentLevels.onStack(
                event.attacker().getItemBySlot(EquipmentSlot.HEAD), ModEnchantments.VOODOO));
        if (level <= 0) return;
        result.add(candidate(ModEnchantments.VOODOO.identifier(), ProcHook.ON_VALID_HIT,
                VoodooBehavior.chance(level), Optional.empty(), 0L, Optional.empty(),
                ChildProcEligibility.LIMITED_OFFENSIVE_REROLL,
                activation -> stacks.addStack(activation.event().target(), VoodooBehavior.STACK_ID, 1,
                        StackApplication.ephemeral(Optional.of(activation.event().attacker().getUUID()),
                                activation.event().tracePlayerId()), activation.event().serverTick()),
                new ProcProvenance(ProcSourceKind.ACTUAL_ENCHANTMENT, CosmicPVE.id("actual_helmet")),
                condition(CosmicPVE.id("voodoo_ordinary_hit"), CosmicEnchantmentBehaviorResolver::ordinaryAttack)));
    }

    private void addUndeadRuse(ProcEvent event, List<ProcCandidate> result) {
        if (event.target() == null) return;
        int level = UndeadRuseBehavior.equippedLevelHighest(event.target());
        if (level <= 0 || UndeadRuseBehavior.activeCount(event.target()) >= UndeadRuseBehavior.cap(level)) return;
        result.add(candidate(ModEnchantments.UNDEAD_RUSE.identifier(), ProcHook.ON_DAMAGE_TAKEN,
                UndeadRuseBehavior.chance(level), Optional.empty(), 0L, Optional.of(UNDEAD_RUSE_ONCE_KEY),
                ChildProcEligibility.LIMITED_DEFENSIVE_REACTION,
                activation -> UndeadRuseBehavior.summon(activation.event().target(), level),
                provenance(event, ModEnchantments.UNDEAD_RUSE.identifier()),
                condition(CosmicPVE.id("undead_ruse_ordinary_attack"), CosmicEnchantmentBehaviorResolver::ordinaryAttack)));
    }

    private void addObliterate(ProcEvent event, List<ProcCandidate> result) {
        int level = Math.min(3, event.effectiveEnchantments().level(ModEnchantments.OBLITERATE.identifier()));
        if (level <= 0 || event.attacker() == null || event.target() == null) return;
        result.add(candidate(ModEnchantments.OBLITERATE.identifier(), event.hook(), ObliterateBehavior.CHANCE,
                Optional.empty(), 0L, Optional.empty(), ChildProcEligibility.LIMITED_OFFENSIVE_REROLL,
                activation -> ObliterateBehavior.activate(activation.event().attacker(), activation.event().target(), level),
                provenance(event, ModEnchantments.OBLITERATE.identifier()),
                condition(CosmicPVE.id("obliterate_low_health"), procEvent -> procEvent.attacker() != null
                        && ObliterateBehavior.belowThreshold(procEvent.attacker().getHealth(), procEvent.attacker().getMaxHealth()))));
    }

    private void addSoulTether(ProcEvent event, List<ProcCandidate> result) {
        int level = Math.min(3, event.effectiveEnchantments().level(ModEnchantments.SOUL_TETHER.identifier()));
        if (level <= 0 || event.attacker() == null || event.target() == null) return;
        result.add(candidate(ModEnchantments.SOUL_TETHER.identifier(), ProcHook.ON_VALID_HIT, .10,
                Optional.of(ModEnchantments.SOUL_TETHER.identifier()), 600L, Optional.empty(),
                ChildProcEligibility.LIMITED_OFFENSIVE_REROLL,
                activation -> soulTethers.apply(activation.event().attacker(), activation.event().target(), level,
                        activation.event().serverTick()), provenance(event, ModEnchantments.SOUL_TETHER.identifier()),
                meleeCondition(CosmicPVE.id("soul_tether_melee_hit"))));
    }

    private void addDivineImmolation(ProcEvent event, List<ProcCandidate> result) {
        int level = Math.min(4, event.effectiveEnchantments().level(ModEnchantments.DIVINE_IMMOLATION.identifier()));
        if (level <= 0 || event.attacker() == null) return;
        result.add(candidate(ModEnchantments.DIVINE_IMMOLATION.identifier(), ProcHook.ON_VALID_HIT,
                DivineImmolationBehavior.chance(level), Optional.of(DivineImmolationBehavior.COOLDOWN_KEY),
                DivineImmolationBehavior.COOLDOWN_TICKS, Optional.empty(),
                ChildProcEligibility.LIMITED_OFFENSIVE_REROLL,
                activation -> DivineImmolationBehavior.activate(activation, childActions, cooldowns),
                provenance(event, ModEnchantments.DIVINE_IMMOLATION.identifier()),
                meleeCondition(CosmicPVE.id("divine_immolation_melee_hit"))));
    }

    private void addVirus(ProcEvent event, List<ProcCandidate> result) {
        int level = Math.min(3, event.effectiveEnchantments().level(ModEnchantments.VIRUS.identifier()));
        if (level <= 0 || event.target() == null || event.target().isDeadOrDying()
                || !event.target().hasEffect(net.minecraft.world.effect.MobEffects.POISON)) return;
        result.add(deterministicCandidate(ModEnchantments.VIRUS.identifier(), ProcHook.ON_PROJECTILE_HIT,
                1.0, Optional.empty(), 0L, Optional.empty(), ChildProcEligibility.ROOT_ONLY,
                activation -> VirusBehavior.activate(activation, level, childActions),
                provenance(event, ModEnchantments.VIRUS.identifier()),
                condition(CosmicPVE.id("virus_poisoned_projectile_target"), procEvent ->
                        procEvent.combatResult().map(resultHit -> resultHit.context().category()
                                        == com.cosmicpve.combat.api.AttackCategory.PROJECTILE).orElse(false))));
    }

    private void addSnare(ProcEvent event, List<ProcCandidate> result) {
        int level = Math.min(4, event.effectiveEnchantments().level(ModEnchantments.SNARE.identifier()));
        if (level <= 0 || event.target() == null) return;
        result.add(candidate(ModEnchantments.SNARE.identifier(), ProcHook.ON_PROJECTILE_HIT,
                SnareBehavior.chance(level), Optional.empty(), 0L, Optional.empty(), ChildProcEligibility.ROOT_ONLY,
                activation -> SnareBehavior.activate(activation.event(), snareRoots),
                provenance(event, ModEnchantments.SNARE.identifier()),
                condition(CosmicPVE.id("snare_crossbow_projectile"), SnareBehavior::eligible)));
    }

    private void addPlagueCarrier(ProcEvent event, List<ProcCandidate> result) {
        int level = Math.min(7, event.effectiveEnchantments().level(ModEnchantments.PLAGUE_CARRIER.identifier()));
        if (level <= 0 || event.target() == null || event.attacker() == null
                || event.attacker() == event.target() || event.attacker().isDeadOrDying()) return;
        result.add(deterministicCandidate(ModEnchantments.PLAGUE_CARRIER.identifier(), ProcHook.ON_DAMAGE_TAKEN,
                1.0, Optional.of(PlagueCarrierBehavior.COOLDOWN_KEY), PlagueCarrierBehavior.COOLDOWN_TICKS,
                Optional.empty(), ChildProcEligibility.LIMITED_DEFENSIVE_REACTION,
                activation -> PlagueCarrierBehavior.activate(activation.event().attacker(), level),
                provenance(event, ModEnchantments.PLAGUE_CARRIER.identifier()),
                condition(CosmicPVE.id("plague_carrier_below_quarter"), procEvent ->
                        procEvent.target() != null && PlagueCarrierBehavior.belowThreshold(
                                procEvent.target().getHealth(), procEvent.target().getMaxHealth()))));
    }

    private void addSelfDestruct(ProcEvent event, List<ProcCandidate> result) {
        int level = Math.min(3, event.effectiveEnchantments().level(ModEnchantments.SELF_DESTRUCT.identifier()));
        if (level <= 0 || event.target() == null) return;
        result.add(deterministicCandidate(ModEnchantments.SELF_DESTRUCT.identifier(), ProcHook.ON_DAMAGE_TAKEN,
                1.0, Optional.of(ModEnchantments.SELF_DESTRUCT.identifier()), SelfDestructBehavior.COOLDOWN_TICKS,
                Optional.of(SELF_DESTRUCT_ONCE_KEY), ChildProcEligibility.LIMITED_DEFENSIVE_REACTION,
                activation -> SelfDestructBehavior.activate(activation.event().target()),
                provenance(event, ModEnchantments.SELF_DESTRUCT.identifier()),
                condition(CosmicPVE.id("self_destruct_low_health"), procEvent ->
                        ordinaryAttack(procEvent) && procEvent.target() != null
                                && SelfDestructBehavior.belowThreshold(
                                        procEvent.target().getHealth(), procEvent.target().getMaxHealth()))));
    }

    private void addPhoenix(ProcEvent event, List<ProcCandidate> result) {
        int level = Math.min(3, event.effectiveEnchantments().level(ModEnchantments.PHOENIX.identifier()));
        if (level <= 0 || event.target() == null || event.attacker() == null) return;
        result.add(deterministicCandidate(ModEnchantments.PHOENIX.identifier(), ProcHook.ON_PRE_DEATH,
                1.0, Optional.of(PhoenixBehavior.COOLDOWN_KEY), PhoenixBehavior.COOLDOWN_TICKS,
                Optional.empty(), ChildProcEligibility.ROOT_ONLY,
                activation -> PhoenixBehavior.activate(activation.event().target()),
                provenance(event, ModEnchantments.PHOENIX.identifier())));
    }

    private void addDevour(ProcEvent event, List<ProcCandidate> result) {
        int level = Math.min(4, event.effectiveEnchantments().level(ModEnchantments.DEVOUR.identifier()));
        if (level <= 0) return;
        result.add(candidate(ModEnchantments.DEVOUR.identifier(), ProcHook.ON_PRE_DAMAGE_CALCULATION,
                DevourBehavior.PROC_CHANCE, Optional.empty(), 0L, Optional.empty(), ChildProcEligibility.ROOT_ONLY,
                activation -> {}, provenance(event, ModEnchantments.DEVOUR.identifier()),
                condition(CosmicPVE.id("devour_hunger_and_melee"), procEvent ->
                        procEvent.combatResult().map(DevourBehavior::eligible).orElse(false))));
    }

    private void addBlessed(ProcEvent event, List<ProcCandidate> result) {
        int level = event.effectiveEnchantments().level(ModEnchantments.BLESSED.identifier());
        if (level <= 0 || event.attacker() == null
                || BlessedBehavior.eligible(event.attacker(), stacks, event.serverTick()).isEmpty()) return;
        result.add(candidate(ModEnchantments.BLESSED.identifier(), ProcHook.ON_VALID_HIT,
                BlessedBehavior.chance(level), Optional.empty(), 0L, Optional.empty(),
                ChildProcEligibility.LIMITED_OFFENSIVE_REROLL,
                activation -> BlessedBehavior.activate(activation.event(), stacks),
                provenance(event, ModEnchantments.BLESSED.identifier()),
                meleeCondition(CosmicPVE.id("blessed_melee_hit"))));
    }

    private void addBleed(ProcEvent event, List<ProcCandidate> result) {
        int level = event.effectiveEnchantments().level(ModEnchantments.BLEED.identifier());
        if (level <= 0) {
            return;
        }
        result.add(candidate(
                ModEnchantments.BLEED.identifier(), ProcHook.ON_VALID_HIT, BleedBehavior.chance(level),
                Optional.empty(), 0L, Optional.empty(), ChildProcEligibility.LIMITED_OFFENSIVE_REROLL,
                activation -> {
                    var target = activation.event().target();
                    var attacker = activation.event().attacker();
                    if (target == null || target.isDeadOrDying() || attacker == null) {
                        return;
                    }
                    var application = StackApplication.ephemeral(
                            Optional.of(attacker.getUUID()),
                            activation.event().combatResult()
                                    .flatMap(hit -> hit.context().attributedPlayerId()));
                    var added = stacks.addStack(
                            target, BleedBehavior.STACK_ID, 1, application, activation.event().serverTick());
                    bleedRuntime.reconcileMovement(target, added.finalCount());
                },
                provenance(event, ModEnchantments.BLEED.identifier()),
                meleeCondition(CosmicPVE.id("bleed_melee_hit"))));
    }

    private void addPoison(ProcEvent event, List<ProcCandidate> result) {
        int level = event.effectiveEnchantments().level(ModEnchantments.POISON.identifier());
        if (level <= 0) {
            return;
        }
        result.add(candidate(
                ModEnchantments.POISON.identifier(), ProcHook.ON_VALID_HIT, PoisonBehavior.chance(level),
                Optional.empty(), 0L, Optional.empty(), ChildProcEligibility.LIMITED_OFFENSIVE_REROLL,
                activation -> PoisonBehavior.activate(activation.event()),
                provenance(event, ModEnchantments.POISON.identifier()),
                meleeCondition(CosmicPVE.id("poison_melee_hit"))));
    }

    private void addPummel(ProcEvent event, List<ProcCandidate> result) {
        int level = event.effectiveEnchantments().level(ModEnchantments.PUMMEL.identifier());
        if (level <= 0) {
            return;
        }
        result.add(candidate(
                ModEnchantments.PUMMEL.identifier(), ProcHook.ON_VALID_HIT, PummelBehavior.chance(level),
                Optional.empty(), 0L, Optional.empty(), ChildProcEligibility.LIMITED_OFFENSIVE_REROLL,
                activation -> PummelBehavior.activate(activation.event()),
                provenance(event, ModEnchantments.PUMMEL.identifier()),
                meleeCondition(CosmicPVE.id("pummel_melee_hit"))));
    }

    private void addTrap(ProcEvent event, List<ProcCandidate> result) {
        int level = event.effectiveEnchantments().level(ModEnchantments.TRAP.identifier());
        if (level <= 0) return;
        result.add(candidate(ModEnchantments.TRAP.identifier(), ProcHook.ON_VALID_HIT,
                TrapBehavior.chance(level), Optional.empty(), 0L, Optional.empty(),
                ChildProcEligibility.LIMITED_OFFENSIVE_REROLL,
                activation -> TrapBehavior.activate(activation.event(), level),
                provenance(event, ModEnchantments.TRAP.identifier()),
                meleeCondition(CosmicPVE.id("trap_melee_hit"))));
    }

    private void addDoublestrike(ProcEvent event, List<ProcCandidate> result) {
        int level = event.effectiveEnchantments().level(ModEnchantments.DOUBLESTRIKE.identifier());
        if (level <= 0) {
            return;
        }
        result.add(candidate(
                ModEnchantments.DOUBLESTRIKE.identifier(), ProcHook.ON_VALID_HIT,
                DoublestrikeBehavior.chance(level), Optional.empty(), 0L, Optional.empty(),
                ChildProcEligibility.ROOT_ONLY,
                activation -> activation.event().combatResult()
                        .ifPresent(parent -> DoublestrikeBehavior.activate(parent, childActions)),
                provenance(event, ModEnchantments.DOUBLESTRIKE.identifier()),
                condition(CosmicPVE.id("doublestrike_ordinary_parent"), procEvent ->
                        procEvent.combatResult().filter(parentResult ->
                                parentResult.context().channel() == com.cosmicpve.combat.api.DamageChannel.ORDINARY)
                                .isPresent())));
    }

    private void addLightning(ProcEvent event, List<ProcCandidate> result) {
        int level = event.effectiveEnchantments().level(ModEnchantments.LIGHTNING.identifier());
        if (level <= 0) {
            return;
        }
        result.add(candidate(
                ModEnchantments.LIGHTNING.identifier(), ProcHook.ON_PROJECTILE_HIT,
                LightningBehavior.chance(level), Optional.empty(), 0L, Optional.empty(),
                ChildProcEligibility.LIMITED_OFFENSIVE_REROLL,
                activation -> LightningBehavior.activate(activation, childActions),
                provenance(event, ModEnchantments.LIGHTNING.identifier())));
    }

    private void addVenom(ProcEvent event, List<ProcCandidate> result) {
        int level = event.effectiveEnchantments().level(ModEnchantments.VENOM.identifier());
        if (level <= 0) return;
        result.add(candidate(
                ModEnchantments.VENOM.identifier(), ProcHook.ON_PROJECTILE_HIT,
                VenomBehavior.chance(level), Optional.empty(), 0L, Optional.empty(),
                ChildProcEligibility.ROOT_ONLY,
                activation -> VenomBehavior.activate(activation.event()),
                provenance(event, ModEnchantments.VENOM.identifier())));
    }

    private void addAngelic(ProcEvent event, List<ProcCandidate> result) {
        if (event.target() == null) {
            return;
        }
        int totalLevel = AngelicBehavior.equippedLevelTotal(event.target());
        var plan = AngelicBehavior.planForLevels(totalLevel);
        if (plan.isEmpty()) {
            return;
        }
        var angelic = plan.orElseThrow();
        result.add(candidate(
                ModEnchantments.ANGELIC.identifier(), ProcHook.ON_DAMAGE_TAKEN,
                angelic.chance(), Optional.empty(), 0L, Optional.of(ANGELIC_ONCE_KEY),
                ChildProcEligibility.LIMITED_DEFENSIVE_REACTION,
                activation -> {
                    var wearer = activation.event().target();
                    if (wearer != null && !wearer.isDeadOrDying()) {
                        wearer.heal(angelic.healAmount());
                    }
                },
                new ProcProvenance(ProcSourceKind.ACTUAL_ENCHANTMENT, CosmicPVE.id("equipped_armor"))));
    }

    private void addEnderShift(ProcEvent event, List<ProcCandidate> result) {
        if (event.target() == null) {
            return;
        }
        int level = EnchantmentLevels.onStack(
                event.target().getItemBySlot(EquipmentSlot.HEAD), ModEnchantments.ENDER_SHIFT);
        if (level <= 0) {
            return;
        }
        result.add(candidate(
                ModEnchantments.ENDER_SHIFT.identifier(), ProcHook.ON_DAMAGE_TAKEN, 1.0,
                Optional.of(ENDER_SHIFT_COOLDOWN), EnderShiftBehavior.BASE_COOLDOWN_TICKS, Optional.empty(),
                ChildProcEligibility.LIMITED_DEFENSIVE_REACTION,
                activation -> EnderShiftBehavior.activate(activation.event().target(), level),
                new ProcProvenance(ProcSourceKind.ACTUAL_ENCHANTMENT, CosmicPVE.id("actual_head")),
                condition(CosmicPVE.id("ender_shift_below_quarter"), procEvent -> procEvent.target() != null
                        && !procEvent.target().isDeadOrDying()
                        && EnderShiftBehavior.shouldTrigger(
                                procEvent.target().getHealth(), procEvent.target().getMaxHealth()))));
    }

    private void addMolten(ProcEvent event, List<ProcCandidate> result) {
        if (event.target() == null || event.attacker() == null || event.attacker() == event.target()
                || event.attacker().isDeadOrDying()) {
            return;
        }
        int level = MoltenBehavior.equippedLevelHighest(event.target());
        if (level <= 0) return;
        result.add(candidate(
                ModEnchantments.MOLTEN.identifier(), ProcHook.ON_DAMAGE_TAKEN, MoltenBehavior.chance(level),
                Optional.empty(), 0L, Optional.of(MOLTEN_ONCE_KEY),
                ChildProcEligibility.LIMITED_DEFENSIVE_REACTION,
                activation -> {
                    var attacker = activation.event().attacker();
                    if (attacker != null && attacker != activation.event().target() && !attacker.isDeadOrDying()) {
                        MoltenBehavior.ignite(attacker);
                    }
                },
                new ProcProvenance(ProcSourceKind.ACTUAL_ENCHANTMENT, CosmicPVE.id("equipped_armor"))));
    }

    private void addCactus(ProcEvent event, List<ProcCandidate> result) {
        if (event.target() == null || event.attacker() == null || event.attacker() == event.target()
                || event.attacker().isDeadOrDying() || event.combatResult().isEmpty()) return;
        int level = CactusBehavior.equippedLevel(event.target());
        if (level <= 0) return;
        result.add(candidate(ModEnchantments.CACTUS.identifier(), ProcHook.ON_DAMAGE_TAKEN,
                CactusBehavior.chance(level), Optional.empty(), 0L, Optional.of(CACTUS_ONCE_KEY),
                ChildProcEligibility.LIMITED_DEFENSIVE_REACTION,
                activation -> {
                    var attacker = activation.event().attacker();
                    var parent = activation.event().combatResult().orElse(null);
                    if (attacker != null && parent != null && attacker != activation.event().target() && !attacker.isDeadOrDying())
                        childActions.deliverTrue(parent.context(), attacker, CactusBehavior.packet(), CactusBehavior.RECURSION_POLICY);
                }, new ProcProvenance(ProcSourceKind.ACTUAL_ENCHANTMENT, CosmicPVE.id("actual_leggings"))));
    }

    private void addPermafrost(ProcEvent event, List<ProcCandidate> result) {
        if (event.target() == null || event.attacker() == null || event.attacker() == event.target()
                || event.attacker().isDeadOrDying() || immunities.isImmune(event.attacker(), PermafrostBehavior.STACK_ID))
            return;
        int level = Math.min(6, event.effectiveEnchantments().level(ModEnchantments.PERMAFROST.identifier()));
        if (level <= 0) return;
        result.add(candidate(ModEnchantments.PERMAFROST.identifier(), ProcHook.ON_DAMAGE_TAKEN,
                PermafrostBehavior.chance(level), Optional.empty(), 0L, Optional.of(PERMAFROST_ONCE_KEY),
                ChildProcEligibility.LIMITED_DEFENSIVE_REACTION,
                activation -> PermafrostBehavior.activate(activation.event(), level, stacks, childActions),
                provenance(event, ModEnchantments.PERMAFROST.identifier()),
                condition(CosmicPVE.id("permafrost_ordinary_attack"), CosmicEnchantmentBehaviorResolver::ordinaryAttack)));
    }

    private void addMortalCoil(ProcEvent event, List<ProcCandidate> result) {
        int level = Math.min(2, event.effectiveEnchantments().level(ModEnchantments.MORTAL_COIL.identifier()));
        if (level <= 0 || event.target() == null) return;
        result.add(candidate(ModEnchantments.MORTAL_COIL.identifier(), ProcHook.ON_DAMAGE_TAKEN,
                MortalCoilBehavior.chance(level), Optional.empty(), 0L, Optional.of(MORTAL_COIL_ONCE_KEY),
                ChildProcEligibility.LIMITED_DEFENSIVE_REACTION,
                activation -> MortalCoilBehavior.activate(activation.event()),
                provenance(event, ModEnchantments.MORTAL_COIL.identifier()),
                condition(CosmicPVE.id("mortal_coil_ordinary_attack"), CosmicEnchantmentBehaviorResolver::ordinaryAttack)));
    }

    private static boolean ordinaryAttack(ProcEvent event) {
        return event.attacker() != null && event.combatResult()
                .map(result -> result.context().channel() == com.cosmicpve.combat.api.DamageChannel.ORDINARY)
                .orElse(false);
    }

    private static ProcCandidate candidate(
            Identifier id,
            ProcHook hook,
            double chance,
            Optional<Identifier> cooldown,
            long cooldownTicks,
            Optional<Identifier> onceKey,
            ChildProcEligibility childEligibility,
            com.cosmicpve.combat.proc.ProcAction action,
            ProcProvenance provenance,
            com.cosmicpve.combat.proc.ProcCondition... conditions) {
        java.util.Set<Identifier> classifications = com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs.find(id)
                .filter(spec -> spec.tier() == com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier.MASTERY)
                .map(ignored -> java.util.Set.of(com.cosmicpve.equipment.armor.ArmorSetIds.MASTERY_PROC))
                .orElseGet(java.util.Set::of);
        return new ProcCandidate(
                id, hook, chance, cooldown, cooldownTicks, CooldownScope.EPHEMERAL_COMBAT, Optional.empty(),
                List.of(), List.of(conditions), onceKey, childEligibility, classifications, id, action, provenance);
    }

    private static ProcCandidate deterministicCandidate(
            Identifier id,
            ProcHook hook,
            double chance,
            Optional<Identifier> cooldown,
            long cooldownTicks,
            Optional<Identifier> onceKey,
            ChildProcEligibility childEligibility,
            com.cosmicpve.combat.proc.ProcAction action,
            ProcProvenance provenance,
            com.cosmicpve.combat.proc.ProcCondition... conditions) {
        return new ProcCandidate(
                id, hook, chance, cooldown, cooldownTicks, CooldownScope.EPHEMERAL_COMBAT, Optional.empty(),
                List.of(), List.of(conditions), onceKey, childEligibility,
                java.util.Set.of(ProcEngine.DETERMINISTIC_CLASSIFICATION), id, action, provenance);
    }

    private static ProcProvenance provenance(ProcEvent event, Identifier enchantmentId) {
        return event.effectiveEnchantments().get(enchantmentId)
                .flatMap(enchantment -> enchantment.provenance().stream().findFirst())
                .map(source -> new ProcProvenance(
                        source.kind() == EnchantmentSourceKind.VIRTUAL
                                ? ProcSourceKind.VIRTUAL_ENCHANTMENT : ProcSourceKind.ACTUAL_ENCHANTMENT,
                        source.sourceId()))
                .orElseGet(() -> new ProcProvenance(ProcSourceKind.OTHER, enchantmentId));
    }

    private static com.cosmicpve.combat.proc.ProcCondition condition(
            Identifier id, java.util.function.Predicate<ProcEvent> predicate) {
        return new com.cosmicpve.combat.proc.ProcCondition() {
            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public boolean matches(ProcEvent event) {
                return predicate.test(event);
            }
        };
    }

    private static com.cosmicpve.combat.proc.ProcCondition meleeCondition(Identifier id) {
        return condition(id, BleedBehavior::isEligibleMelee);
    }
}
