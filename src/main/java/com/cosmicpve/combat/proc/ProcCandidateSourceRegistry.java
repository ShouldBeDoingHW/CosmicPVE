package com.cosmicpve.combat.proc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Composition seam for future enchantment, armor-set, mask, and skin candidate resolvers. */
public final class ProcCandidateSourceRegistry implements ProcCandidateResolver {
    private final CopyOnWriteArrayList<ProcCandidateResolver> sources = new CopyOnWriteArrayList<>();

    public void register(ProcCandidateResolver source) {
        sources.addIfAbsent(source);
    }

    public boolean isEmpty() {
        return sources.isEmpty();
    }

    @Override
    public List<ProcCandidate> resolve(ProcEvent event) {
        var candidates = new ArrayList<ProcCandidate>();
        for (var source : sources) {
            candidates.addAll(source.resolve(event));
        }
        return List.copyOf(candidates);
    }
}
