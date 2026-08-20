package com.cosmicpve.combat.proc;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ProcTraceService {
    private final Set<UUID> enabledPlayers = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<UUID, ProcDispatchResult> lastResults = new ConcurrentHashMap<>();

    public void setEnabled(UUID playerId, boolean enabled) {
        if (enabled) {
            enabledPlayers.add(playerId);
        } else {
            enabledPlayers.remove(playerId);
        }
    }

    public boolean isEnabled(UUID playerId) {
        return enabledPlayers.contains(playerId);
    }

    public void record(ProcDispatchResult result) {
        if (result.evaluations().isEmpty()) {
            return;
        }
        result.event().tracePlayerId().filter(enabledPlayers::contains)
                .ifPresent(playerId -> lastResults.put(playerId, result));
    }

    public Optional<ProcDispatchResult> last(UUID playerId) {
        return Optional.ofNullable(lastResults.get(playerId));
    }

    public String format(ProcDispatchResult result) {
        String parent = result.event().parentSequenceId().isPresent()
                ? Long.toString(result.event().parentSequenceId().getAsLong()) : "none";
        String evaluations = result.evaluations().stream().map(this::formatEvaluation)
                .collect(java.util.stream.Collectors.joining("; ", "[", "]"));
        return "sequence=" + result.event().sequenceId()
                + " parent=" + parent
                + " hook=" + result.event().hook()
                + " policy=" + result.event().recursionPolicy()
                + " evaluations=" + evaluations;
    }

    private String formatEvaluation(ProcEvaluation evaluation) {
        String roll = evaluation.roll().isPresent()
                ? String.format(Locale.ROOT, "%.6f", evaluation.roll().getAsDouble()) : "none";
        return "candidate=" + evaluation.candidateId()
                + ",base=" + String.format(Locale.ROOT, "%.6f", evaluation.baseChance())
                + ",multiplier=" + String.format(Locale.ROOT, "%.6f", evaluation.chanceMultiplier())
                + ",final=" + String.format(Locale.ROOT, "%.6f", evaluation.finalChance())
                + ",roll=" + roll
                + ",status=" + evaluation.status()
                + ",cooldownReady=" + evaluation.cooldownReady()
                + ",source=" + evaluation.provenance().kind() + ":" + evaluation.provenance().sourceId();
    }
}
