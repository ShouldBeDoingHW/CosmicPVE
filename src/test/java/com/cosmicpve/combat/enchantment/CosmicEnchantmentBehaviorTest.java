package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.action.ChildCombatActionService;
import com.cosmicpve.combat.action.TrueDamageDeliveryService;
import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.AttackSequence;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.TrueDamagePacket;
import com.cosmicpve.combat.api.WeaponSnapshot;
import com.cosmicpve.combat.cooldown.CooldownScope;
import com.cosmicpve.combat.cooldown.CooldownService;
import com.cosmicpve.combat.pipeline.AttackSequenceService;
import com.cosmicpve.combat.pipeline.CombatCalculationRequest;
import com.cosmicpve.combat.pipeline.CombatEngine;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.proc.ProcEvent;
import com.cosmicpve.combat.proc.ProcHook;
import com.cosmicpve.combat.proc.ProcSourceKind;
import com.cosmicpve.combat.proc.ChildProcEligibility;
import com.cosmicpve.combat.stack.BleedRuntimeService;
import com.cosmicpve.combat.stack.CombatStackService;
import com.cosmicpve.content.CosmicContentRepository;
import com.cosmicpve.equipment.enchantment.ActualEnchantmentGrant;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantments;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import com.cosmicpve.equipment.enchantment.VirtualEnchantmentGrant;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CosmicEnchantmentBehaviorTest {
    @Test
    void executeUsesStrictlyBelowHalfAndScalesInSharedAdditiveBucket() {
        assertEquals(0.0, ExecuteBehavior.bonus(5, 10.0, 20.0));
        assertEquals(0.02, ExecuteBehavior.bonus(1, 9.99, 20.0), 1.0E-12);
        assertEquals(0.10, ExecuteBehavior.bonus(5, 9.99, 20.0), 1.0E-12);

        var request = new CombatCalculationRequest(
                10.0, 0.05, List.of(), com.cosmicpve.combat.api.DamageBounds.UNBOUNDED,
                List.of(), com.cosmicpve.combat.api.DamageBounds.UNBOUNDED, List.of(),
                List.of(new OutgoingDamageContribution(ModEnchantments.EXECUTE.identifier(), 0.10)));
        var result = new CombatEngine().calculate(context(EffectiveEnchantments.EMPTY), request);
        assertEquals(0.15, result.breakdown().additiveOutgoingBonus(), 1.0E-12);
        assertEquals(11.5, result.breakdown().finalOrdinaryDamage(), 1.0E-12);
    }

    @Test
    void angelicAggregatesArmorIntoOnePlanAndHealsExactlyOneHpWithCap() {
        var onePiece = AngelicBehavior.planForLevels(5).orElseThrow();
        var fourPieces = AngelicBehavior.planForLevels(5, 5, 5, 5).orElseThrow();
        assertEquals(0.05, onePiece.chance(), 1.0E-12);
        assertEquals(20, fourPieces.totalLevel());
        assertEquals(0.20, fourPieces.chance(), 1.0E-12);
        assertEquals(1.0F, fourPieces.healAmount());
        assertEquals(11.0F, AngelicBehavior.healedHealth(10.0F, 20.0F));
        assertEquals(20.0F, AngelicBehavior.healedHealth(19.75F, 20.0F));
        assertTrue(AngelicBehavior.planForLevels(0, 0, 0, 0).isEmpty());
    }

    @Test
    void lightningChancePacketAndAttributionUseStandardTrueDamage() {
        assertEquals(0.05, LightningBehavior.chance(1), 1.0E-12);
        assertEquals(0.20, LightningBehavior.chance(4), 1.0E-12);
        var packet = LightningBehavior.packet();
        assertEquals(2.0, packet.amount());
        assertTrue(packet.bypassesArmor());
        assertFalse(packet.bypassesAbsorption());
        assertTrue(packet.bypassesCustomReduction());

        UUID owner = UUID.randomUUID();
        var parent = context(EffectiveEnchantments.EMPTY, owner);
        var child = parent.child(null, DamageChannel.TRUE, RecursionPolicy.NO_PROCS,
                new AttackSequence(2L, OptionalLong.of(1L)));
        assertEquals(Optional.of(owner), child.attributedPlayerId());
        assertEquals(1L, child.parentSequenceId().orElseThrow());
    }

    @Test
    void enderShiftUsesBelowQuarterConditionDurationsAndSharedCooldown() {
        assertFalse(EnderShiftBehavior.shouldTrigger(5.0, 20.0));
        assertTrue(EnderShiftBehavior.shouldTrigger(4.99, 20.0));
        assertFalse(EnderShiftBehavior.shouldTrigger(0.0, 20.0));
        assertEquals(60, EnderShiftBehavior.durationTicks(1));
        assertEquals(120, EnderShiftBehavior.durationTicks(2));
        assertEquals(180, EnderShiftBehavior.durationTicks(3));
        assertEquals(600L, EnderShiftBehavior.BASE_COOLDOWN_TICKS);

        var cooldowns = new CooldownService();
        UUID owner = UUID.randomUUID();
        cooldowns.start(owner, CosmicPVE.id("ender_shift"), 600L, List.of(1.0), 100L,
                CooldownScope.EPHEMERAL_COMBAT, Optional.empty());
        assertFalse(cooldowns.isReady(owner, CosmicPVE.id("ender_shift"), 699L));
        assertTrue(cooldowns.isReady(owner, CosmicPVE.id("ender_shift"), 700L));
        assertEquals(480L, CooldownService.effectiveDuration(600L, List.of(0.8)));
    }

    @Test
    void doublestrikeHalvesOnlyParentOrdinaryAndLinksNonRecursiveChild() {
        assertEquals(0.01, DoublestrikeBehavior.chance(1), 1.0E-12);
        assertEquals(0.03, DoublestrikeBehavior.chance(3), 1.0E-12);
        var parent = new CombatEngine().calculate(
                context(EffectiveEnchantments.EMPTY),
                new CombatCalculationRequest(
                        10.0, 0.0, List.of(), com.cosmicpve.combat.api.DamageBounds.UNBOUNDED,
                        List.of(), com.cosmicpve.combat.api.DamageBounds.UNBOUNDED,
                        List.of(TrueDamagePacket.standard(CosmicPVE.id("separate_true"), 8.0))));
        assertEquals(5.0, DoublestrikeBehavior.childOrdinaryDamage(parent), 1.0E-12);

        var child = parent.context().child(
                null, DamageChannel.ORDINARY, RecursionPolicy.LIMITED_OFFENSIVE_REROLL,
                new AttackSequence(2L, OptionalLong.of(1L)), Set.of(ModEnchantments.DOUBLESTRIKE.identifier()));
        assertEquals(1L, child.parentSequenceId().orElseThrow());
        assertEquals(RecursionPolicy.LIMITED_OFFENSIVE_REROLL, child.recursionPolicy());
        assertTrue(child.excludedProcEffectIds().contains(ModEnchantments.DOUBLESTRIKE.identifier()));
    }

    @Test
    void virtualAndActualDoublestrikeResolveToIdenticalBehaviorLevelAndChance() {
        var enchantments = new EffectiveEnchantmentsResolver();
        var actual = enchantments.resolveSources(
                List.of(new ActualEnchantmentGrant(
                        ModEnchantments.DOUBLESTRIKE.identifier(), 3, CosmicPVE.id("actual_sword"))), List.of());
        var virtual = enchantments.resolveSources(List.of(), List.of(new VirtualEnchantmentGrant(
                ModEnchantments.DOUBLESTRIKE.identifier(), 3, CosmicPVE.id("boosted_chainsaw"))));
        var behavior = behaviorResolver();

        var actualCandidate = behavior.resolve(procEvent(actual)).getFirst();
        var virtualCandidate = behavior.resolve(procEvent(virtual)).getFirst();
        assertEquals(3, actual.level(ModEnchantments.DOUBLESTRIKE.identifier()));
        assertEquals(actualCandidate.baseProbability(), virtualCandidate.baseProbability());
        assertEquals(ProcSourceKind.ACTUAL_ENCHANTMENT, actualCandidate.provenance().kind());
        assertEquals(ProcSourceKind.VIRTUAL_ENCHANTMENT, virtualCandidate.provenance().kind());
    }

    @Test
    void bleedUsesIndependentThirtyTickSchedulesStandardTrueDamageAndMovementPenalty() {
        assertEquals(0.01, BleedBehavior.chance(1), 1.0E-12);
        assertEquals(0.06, BleedBehavior.chance(6), 1.0E-12);
        assertFalse(BleedBehavior.isTickDue(10, 110, 39));
        assertTrue(BleedBehavior.isTickDue(10, 110, 40));
        assertTrue(BleedBehavior.isTickDue(10, 110, 70));
        assertTrue(BleedBehavior.isTickDue(10, 110, 100));
        assertFalse(BleedBehavior.isTickDue(10, 110, 110));
        assertEquals(-0.05, BleedBehavior.movementMultiplierAmount(5), 1.0E-12);
        var packet = BleedBehavior.tickPacket();
        assertEquals(1.0, packet.amount());
        assertTrue(packet.bypassesArmor());
        assertFalse(packet.bypassesAbsorption());
        assertTrue(packet.bypassesCustomReduction());
    }

    @Test
    void luckIsRelativeAndComposesThroughCentralProcChanceMultipliers() {
        assertEquals(1.0, LuckBehavior.chanceMultiplier(0), 1.0E-12);
        assertEquals(1.2, LuckBehavior.chanceMultiplier(20), 1.0E-12);
        assertEquals(0.012, com.cosmicpve.combat.proc.ProcChance.calculate(
                0.01, List.of(LuckBehavior.chanceMultiplier(20))), 1.0E-12);
        assertEquals(0.24, com.cosmicpve.combat.proc.ProcChance.calculate(
                0.20, List.of(LuckBehavior.chanceMultiplier(20))), 1.0E-12);
    }

    @Test
    void newOffensiveEnchantmentsHavePinnedChancesEffectsAndLimitedChildEligibility() {
        assertEquals(0.15, PoisonBehavior.chance(3), 1.0E-12);
        assertEquals(60, PoisonBehavior.DURATION_TICKS);
        assertEquals(0, PoisonBehavior.AMPLIFIER);
        assertEquals(0.06, PummelBehavior.chance(3), 1.0E-12);
        assertEquals(50, PummelBehavior.DURATION_TICKS);
        assertEquals(2, PummelBehavior.AMPLIFIER);

        var grants = List.of(
                new ActualEnchantmentGrant(ModEnchantments.BLEED.identifier(), 6, CosmicPVE.id("actual_axe")),
                new ActualEnchantmentGrant(ModEnchantments.POISON.identifier(), 3, CosmicPVE.id("actual_sword")),
                new ActualEnchantmentGrant(ModEnchantments.PUMMEL.identifier(), 3, CosmicPVE.id("actual_axe")));
        var effective = new EffectiveEnchantmentsResolver().resolveSources(grants, List.of());
        var candidates = behaviorResolver().resolve(procEvent(effective));
        assertEquals(3, candidates.stream()
                .filter(candidate -> candidate.childEligibility() == ChildProcEligibility.LIMITED_OFFENSIVE_REROLL)
                .count());
    }

    private static ProcEvent procEvent(EffectiveEnchantments enchantments) {
        UUID owner = UUID.randomUUID();
        return new ProcEvent(
                ProcHook.ON_VALID_HIT, 1L, OptionalLong.empty(), RecursionPolicy.NORMAL, owner, Optional.of(owner),
                0L, List.of(1.0), Set.of(), enchantments, null, null, () -> 0.5);
    }

    private static CosmicEnchantmentBehaviorResolver behaviorResolver() {
        var childActions = new ChildCombatActionService(new AttackSequenceService(), new TrueDamageDeliveryService());
        var stacks = new CombatStackService(new CosmicContentRepository());
        return new CosmicEnchantmentBehaviorResolver(childActions, stacks, new BleedRuntimeService(childActions));
    }

    private static CombatContext context(EffectiveEnchantments enchantments) {
        return context(enchantments, null);
    }

    private static CombatContext context(EffectiveEnchantments enchantments, UUID attributed) {
        return new CombatContext(
                null, null, null, null, Optional.ofNullable(attributed), null, AttackCategory.MELEE,
                DamageChannel.ORDINARY, Set.of(), WeaponSnapshot.empty(), enchantments,
                1L, OptionalLong.empty(), RecursionPolicy.NORMAL);
    }
}
