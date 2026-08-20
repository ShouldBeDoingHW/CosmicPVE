package com.cosmicpve.combat.proc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.cooldown.CooldownScope;
import com.cosmicpve.combat.cooldown.CooldownService;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantments;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ProcEngineTest {
    @Test
    void zeroAndHundredPercentBoundariesEachRollExactlyOnce() {
        var random = new CountingRandom(0.5);
        var activations = new AtomicInteger();
        var result = engine().evaluate(
                event(1, UUID.randomUUID(), 0, RecursionPolicy.NORMAL, random, List.of(1.0), Set.of()),
                List.of(candidate("guaranteed", 1.0, 0, Optional.empty(), ChildProcEligibility.ROOT_ONLY, activations),
                        candidate("never", 0.0, 0, Optional.empty(), ChildProcEligibility.ROOT_ONLY, activations)));

        assertEquals(ProcEvaluationStatus.ACTIVATED, result.evaluations().get(0).status());
        assertEquals(ProcEvaluationStatus.ROLL_FAILED, result.evaluations().get(1).status());
        assertEquals(1, activations.get());
        assertEquals(2, random.calls());
    }

    @Test
    void chanceMultiplierIsAppliedCentrally() {
        var result = engine().evaluate(
                event(2, UUID.randomUUID(), 0, RecursionPolicy.NORMAL, new CountingRandom(0.011), List.of(1.2), Set.of()),
                List.of(candidate("luck_example", 0.01, 0, Optional.empty(), ChildProcEligibility.ROOT_ONLY,
                        new AtomicInteger())));

        assertEquals(0.012, result.evaluations().getFirst().finalChance(), 1.0E-12);
        assertEquals(ProcEvaluationStatus.ACTIVATED, result.evaluations().getFirst().status());
    }

    @Test
    void cooldownBlocksImmediateRepeatWithoutAnotherRollThenExpires() {
        var cooldowns = new CooldownService();
        var engine = new ProcEngine(cooldowns, new ProcTraceService());
        UUID owner = UUID.randomUUID();
        var counter = new AtomicInteger();
        var candidate = candidate("cooled", 1.0, 20, Optional.empty(), ChildProcEligibility.ROOT_ONLY, counter);
        var random = new CountingRandom(0.0);

        var first = engine.evaluate(event(3, owner, 100, RecursionPolicy.NORMAL, random, List.of(1.0), Set.of()), List.of(candidate));
        var blocked = engine.evaluate(event(4, owner, 100, RecursionPolicy.NORMAL, random, List.of(1.0), Set.of()), List.of(candidate));
        var expired = engine.evaluate(event(5, owner, 120, RecursionPolicy.NORMAL, random, List.of(1.0), Set.of()), List.of(candidate));

        assertEquals(ProcEvaluationStatus.ACTIVATED, first.evaluations().getFirst().status());
        assertEquals(ProcEvaluationStatus.COOLDOWN_BLOCKED, blocked.evaluations().getFirst().status());
        assertEquals(ProcEvaluationStatus.ACTIVATED, expired.evaluations().getFirst().status());
        assertEquals(2, random.calls());
        assertEquals(2, counter.get());
    }

    @Test
    void oncePerEventSuppressesSecondSuccessfulSource() {
        var onceKey = Optional.of(CosmicPVE.id("test/once"));
        var random = new CountingRandom(0.0);
        var counter = new AtomicInteger();
        var result = engine().evaluate(
                event(6, UUID.randomUUID(), 0, RecursionPolicy.NORMAL, random, List.of(1.0), Set.of()),
                List.of(candidate("piece_one", 1.0, 0, onceKey, ChildProcEligibility.ROOT_ONLY, counter),
                        candidate("piece_two", 1.0, 0, onceKey, ChildProcEligibility.ROOT_ONLY, counter)));

        assertEquals(ProcEvaluationStatus.ACTIVATED, result.evaluations().get(0).status());
        assertEquals(ProcEvaluationStatus.ONCE_PER_EVENT_SUPPRESSED, result.evaluations().get(1).status());
        assertEquals(1, random.calls());
        assertEquals(1, counter.get());
    }

    @Test
    void noProcsPolicyFiltersWithoutRolling() {
        var random = new CountingRandom(0.0);
        var result = engine().evaluate(
                event(7, UUID.randomUUID(), 0, RecursionPolicy.NO_PROCS, random, List.of(1.0), Set.of()),
                List.of(candidate("blocked", 1.0, 0, Optional.empty(),
                        ChildProcEligibility.LIMITED_OFFENSIVE_REROLL, new AtomicInteger())));

        assertEquals(ProcEvaluationStatus.RECURSION_FILTERED, result.evaluations().getFirst().status());
        assertEquals(0, random.calls());
    }

    @Test
    void limitedChildExcludesParentEffectButPermitsEligiblePeer() {
        var random = new CountingRandom(0.0);
        var excluded = CosmicPVE.id("test/doublestrike");
        var result = engine().evaluate(
                event(8, UUID.randomUUID(), 0, RecursionPolicy.LIMITED_OFFENSIVE_REROLL,
                        random, List.of(1.0), Set.of(excluded)),
                List.of(candidate("doublestrike", 1.0, 0, Optional.empty(),
                                ChildProcEligibility.LIMITED_OFFENSIVE_REROLL, new AtomicInteger()),
                        candidate("lightning", 1.0, 0, Optional.empty(),
                                ChildProcEligibility.LIMITED_OFFENSIVE_REROLL, new AtomicInteger()),
                        candidate("root_only", 1.0, 0, Optional.empty(),
                                ChildProcEligibility.ROOT_ONLY, new AtomicInteger())));

        assertEquals(ProcEvaluationStatus.RECURSION_FILTERED, result.evaluations().get(0).status());
        assertEquals(ProcEvaluationStatus.ACTIVATED, result.evaluations().get(1).status());
        assertEquals(ProcEvaluationStatus.RECURSION_FILTERED, result.evaluations().get(2).status());
        assertEquals(1, random.calls());
    }

    private static ProcEngine engine() {
        return new ProcEngine(new CooldownService(), new ProcTraceService());
    }

    private static ProcEvent event(
            long sequence,
            UUID owner,
            long tick,
            RecursionPolicy policy,
            ProcRandomSource random,
            List<Double> multipliers,
            Set<net.minecraft.resources.Identifier> excluded) {
        return new ProcEvent(
                ProcHook.ON_VALID_HIT, sequence, OptionalLong.empty(), policy, owner, Optional.of(owner), tick,
                multipliers, excluded, EffectiveEnchantments.EMPTY, null, null, random);
    }

    private static ProcCandidate candidate(
            String path,
            double chance,
            long cooldownTicks,
            Optional<net.minecraft.resources.Identifier> onceKey,
            ChildProcEligibility eligibility,
            AtomicInteger activations) {
        var id = CosmicPVE.id("test/" + path);
        return new ProcCandidate(
                id, ProcHook.ON_VALID_HIT, chance,
                cooldownTicks > 0 ? Optional.of(CosmicPVE.id("test/" + path + "_cooldown")) : Optional.empty(),
                cooldownTicks, CooldownScope.EPHEMERAL_COMBAT, Optional.empty(), List.of(), List.of(), onceKey,
                eligibility, CosmicPVE.id("test/count"), ignored -> activations.incrementAndGet(),
                new ProcProvenance(ProcSourceKind.DEVELOPMENT, CosmicPVE.id("test/fixture")));
    }

    private static final class CountingRandom implements ProcRandomSource {
        private final double value;
        private int calls;

        private CountingRandom(double value) {
            this.value = value;
        }

        @Override
        public double nextDouble() {
            calls++;
            return value;
        }

        int calls() {
            return calls;
        }
    }
}
