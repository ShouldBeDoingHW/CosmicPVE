package com.cosmicpve.combat.proc;

import java.util.List;

public record ProcDispatchResult(ProcEvent event, List<ProcEvaluation> evaluations) {
    public ProcDispatchResult {
        evaluations = List.copyOf(evaluations);
    }

    public long activationCount() {
        return evaluations.stream().filter(ProcEvaluation::activated).count();
    }
}
