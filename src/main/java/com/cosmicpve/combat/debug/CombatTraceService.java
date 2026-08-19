package com.cosmicpve.combat.debug;

import com.cosmicpve.combat.api.CombatResult;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatTraceService {
    private final Set<UUID> enabledPlayers = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<UUID, CombatResult> lastResults = new ConcurrentHashMap<>();

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

    public void recordCommitted(CombatResult result) {
        if (!result.isCommittedDamagingHit()) {
            return;
        }
        result.context().attributedPlayerId()
                .filter(enabledPlayers::contains)
                .ifPresent(playerId -> lastResults.put(playerId, result));
    }

    public Optional<CombatResult> last(UUID playerId) {
        return Optional.ofNullable(lastResults.get(playerId));
    }

    public String format(CombatResult result) {
        var context = result.context();
        var breakdown = result.breakdown();
        String parent = context.parentSequenceId().isPresent()
                ? Long.toString(context.parentSequenceId().getAsLong())
                : "none";
        return "sequence=" + context.attackSequenceId()
                + " parent=" + parent
                + " category=" + context.category()
                + " policy=" + context.recursionPolicy()
                + " flags=" + context.flags()
                + " base=" + formatNumber(breakdown.baseOrdinaryDamage())
                + " additive=" + formatNumber(breakdown.additiveOutgoingBonus())
                + " outgoingProduct=" + formatNumber(breakdown.outgoingMultiplierProduct())
                + " incomingProduct=" + formatNumber(breakdown.incomingMultiplierProduct())
                + " ordinaryBeforeVanilla=" + formatNumber(breakdown.finalOrdinaryDamage())
                + " committedHealth=" + formatNumber(result.committedHealthDamage())
                + " queuedTrue=" + formatNumber(result.totalQueuedTrueDamage());
    }

    private static String formatNumber(double value) {
        return String.format(java.util.Locale.ROOT, "%.3f", value);
    }
}
