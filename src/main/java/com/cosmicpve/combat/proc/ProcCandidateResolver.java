package com.cosmicpve.combat.proc;

import java.util.List;

@FunctionalInterface
public interface ProcCandidateResolver {
    ProcCandidateResolver EMPTY = event -> List.of();

    List<ProcCandidate> resolve(ProcEvent event);
}
