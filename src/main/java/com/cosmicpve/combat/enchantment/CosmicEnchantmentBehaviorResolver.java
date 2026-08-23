package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.action.ChildCombatActionService;
import com.cosmicpve.combat.cooldown.CooldownScope;
import com.cosmicpve.combat.proc.ChildProcEligibility;
import com.cosmicpve.combat.proc.ProcCandidate;
import com.cosmicpve.combat.proc.ProcCandidateResolver;
import com.cosmicpve.combat.proc.ProcEvent;
import com.cosmicpve.combat.proc.ProcHook;
import com.cosmicpve.combat.proc.ProcProvenance;
import com.cosmicpve.combat.proc.ProcSourceKind;
import com.cosmicpve.combat.stack.BleedRuntimeService;
import com.cosmicpve.combat.stack.CombatStackService;
import com.cosmicpve.combat.stack.StackApplication;
import com.cosmicpve.equipment.enchantment.EnchantmentSourceKind;
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

    private final ChildCombatActionService childActions;
    private final CombatStackService stacks;
    private final BleedRuntimeService bleedRuntime;

    public CosmicEnchantmentBehaviorResolver(
            ChildCombatActionService childActions,
            CombatStackService stacks,
            BleedRuntimeService bleedRuntime) {
        this.childActions = childActions;
        this.stacks = stacks;
        this.bleedRuntime = bleedRuntime;
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
        } else if (event.hook() == ProcHook.ON_PROJECTILE_HIT) {
            addLightning(event, result);
            addVenom(event, result);
        } else if (event.hook() == ProcHook.ON_DAMAGE_TAKEN) {
            addAngelic(event, result);
            addEnderShift(event, result);
            addMolten(event, result);
        }
        return List.copyOf(result);
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
        return new ProcCandidate(
                id, hook, chance, cooldown, cooldownTicks, CooldownScope.EPHEMERAL_COMBAT, Optional.empty(),
                List.of(), List.of(conditions), onceKey, childEligibility, id, action, provenance);
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
