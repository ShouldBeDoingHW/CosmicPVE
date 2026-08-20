package com.cosmicpve.combat.proc;

import java.util.Objects;
import java.util.OptionalDouble;
import net.minecraft.resources.Identifier;

public record ProcEvaluation(
        Identifier candidateId,
        ProcHook hook,
        double baseChance,
        double chanceMultiplier,
        double finalChance,
        OptionalDouble roll,
        ProcEvaluationStatus status,
        boolean cooldownReady,
        ProcProvenance provenance) {
    public ProcEvaluation {
        candidateId = Objects.requireNonNull(candidateId);
        hook = Objects.requireNonNull(hook);
        roll = roll == null ? OptionalDouble.empty() : roll;
        status = Objects.requireNonNull(status);
        provenance = Objects.requireNonNull(provenance);
    }

    public boolean activated() {
        return status == ProcEvaluationStatus.ACTIVATED;
    }
}
