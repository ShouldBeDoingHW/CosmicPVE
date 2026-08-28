package com.cosmicpve.combat.proc;

import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.cooldown.CooldownService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import net.minecraft.resources.Identifier;

/** Performs every gameplay proc decision once on the logical server. */
public final class ProcEngine {
    public static final Identifier DETERMINISTIC_CLASSIFICATION =
            Identifier.fromNamespaceAndPath("cosmicpve", "deterministic");
    private final CooldownService cooldowns;
    private final ProcTraceService traces;

    public ProcEngine(CooldownService cooldowns, ProcTraceService traces) {
        this.cooldowns = cooldowns;
        this.traces = traces;
    }

    public ProcDispatchResult evaluate(ProcEvent event, List<ProcCandidate> candidates) {
        var evaluations = new ArrayList<ProcEvaluation>();
        Set<Identifier> claimedOnceKeys = new HashSet<>();
        for (var candidate : List.copyOf(candidates)) {
            evaluations.add(evaluateOne(event, candidate, claimedOnceKeys));
        }
        var result = new ProcDispatchResult(event, evaluations);
        traces.record(result);
        return result;
    }

    private ProcEvaluation evaluateOne(
            ProcEvent event, ProcCandidate candidate, Set<Identifier> claimedOnceKeys) {
        var chanceMultipliers = new ArrayList<Double>(event.chanceMultipliers());
        candidate.classifications().stream().map(event.namedChanceMultipliers()::get)
                .filter(java.util.Objects::nonNull).forEach(chanceMultipliers::add);
        double multiplier = ProcChance.multiplierProduct(chanceMultipliers);
        double finalChance = ProcChance.calculate(candidate.baseProbability(), chanceMultipliers);

        if (candidate.hook() != event.hook()) {
            return skipped(candidate, multiplier, finalChance, ProcEvaluationStatus.HOOK_MISMATCH, true);
        }
        if (!recursionAllows(event, candidate)) {
            return skipped(candidate, multiplier, finalChance, ProcEvaluationStatus.RECURSION_FILTERED, true);
        }
        if (candidate.oncePerEventKey().filter(claimedOnceKeys::contains).isPresent()) {
            return skipped(candidate, multiplier, finalChance, ProcEvaluationStatus.ONCE_PER_EVENT_SUPPRESSED, true);
        }
        for (var condition : candidate.conditions()) {
            if (!condition.matches(event)) {
                return skipped(candidate, multiplier, finalChance, ProcEvaluationStatus.CONDITION_FAILED, true);
            }
        }

        boolean cooldownReady = candidate.cooldownKey()
                .map(key -> cooldowns.isReady(event.ownerId(), key, event.serverTick()))
                .orElse(true);
        if (!cooldownReady) {
            return skipped(candidate, multiplier, finalChance, ProcEvaluationStatus.COOLDOWN_BLOCKED, false);
        }

        double roll = candidate.classifications().contains(DETERMINISTIC_CLASSIFICATION)
                ? 0.0 : event.random().nextDouble();
        if (!(roll >= 0.0 && roll < 1.0)) {
            throw new IllegalStateException("Proc random sources must return values in [0, 1)");
        }
        if (roll >= finalChance) {
            return rolled(candidate, multiplier, finalChance, roll, ProcEvaluationStatus.ROLL_FAILED);
        }

        candidate.oncePerEventKey().ifPresent(claimedOnceKeys::add);
        candidate.cooldownKey().ifPresent(key -> cooldowns.start(
                event.ownerId(), key, candidate.baseCooldownTicks(), combinedCooldownMultipliers(event, candidate),
                event.serverTick(), candidate.cooldownScope(), candidate.cooldownScopeId()));
        candidate.action().execute(new ProcActivation(event, candidate, finalChance, roll));
        return rolled(candidate, multiplier, finalChance, roll, ProcEvaluationStatus.ACTIVATED);
    }

    private static List<Double> combinedCooldownMultipliers(ProcEvent event, ProcCandidate candidate) {
        var result = new ArrayList<Double>(event.cooldownDurationMultipliers());
        result.addAll(candidate.cooldownDurationMultipliers());
        return List.copyOf(result);
    }

    private static boolean recursionAllows(ProcEvent event, ProcCandidate candidate) {
        if (event.recursionPolicy() == RecursionPolicy.NO_PROCS) {
            return false;
        }
        if (event.recursionPolicy() == RecursionPolicy.LIMITED_OFFENSIVE_REROLL) {
            return candidate.childEligibility() != ChildProcEligibility.ROOT_ONLY
                    && !event.excludedEffectIds().contains(candidate.effectId());
        }
        return true;
    }

    private static ProcEvaluation skipped(
            ProcCandidate candidate,
            double multiplier,
            double finalChance,
            ProcEvaluationStatus status,
            boolean cooldownReady) {
        return new ProcEvaluation(candidate.effectId(), candidate.hook(), candidate.baseProbability(), multiplier,
                finalChance, OptionalDouble.empty(), status, cooldownReady, candidate.provenance());
    }

    private static ProcEvaluation rolled(
            ProcCandidate candidate,
            double multiplier,
            double finalChance,
            double roll,
            ProcEvaluationStatus status) {
        return new ProcEvaluation(candidate.effectId(), candidate.hook(), candidate.baseProbability(), multiplier,
                finalChance, OptionalDouble.of(roll), status, true, candidate.provenance());
    }
}
