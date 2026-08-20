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
        double multiplier = ProcChance.multiplierProduct(event.chanceMultipliers());
        double finalChance = ProcChance.calculate(candidate.baseProbability(), event.chanceMultipliers());

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

        double roll = event.random().nextDouble();
        if (!(roll >= 0.0 && roll < 1.0)) {
            throw new IllegalStateException("Proc random sources must return values in [0, 1)");
        }
        if (roll >= finalChance) {
            return rolled(candidate, multiplier, finalChance, roll, ProcEvaluationStatus.ROLL_FAILED);
        }

        candidate.oncePerEventKey().ifPresent(claimedOnceKeys::add);
        candidate.cooldownKey().ifPresent(key -> cooldowns.start(
                event.ownerId(), key, candidate.baseCooldownTicks(), candidate.cooldownDurationMultipliers(),
                event.serverTick(), candidate.cooldownScope(), candidate.cooldownScopeId()));
        candidate.action().execute(new ProcActivation(event, candidate, finalChance, roll));
        return rolled(candidate, multiplier, finalChance, roll, ProcEvaluationStatus.ACTIVATED);
    }

    private static boolean recursionAllows(ProcEvent event, ProcCandidate candidate) {
        if (event.recursionPolicy() == RecursionPolicy.NO_PROCS) {
            return false;
        }
        if (event.recursionPolicy() == RecursionPolicy.LIMITED_OFFENSIVE_REROLL) {
            return candidate.childEligibility() == ChildProcEligibility.LIMITED_OFFENSIVE_REROLL
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
