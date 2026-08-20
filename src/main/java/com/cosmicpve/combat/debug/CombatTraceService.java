package com.cosmicpve.combat.debug;

import com.cosmicpve.combat.api.CombatResult;
import com.cosmicpve.combat.execution.ExecutionResult;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatTraceService {
    private final Set<UUID> enabledPlayers = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<UUID, CombatTraceEntry> lastResults = new ConcurrentHashMap<>();

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
                .ifPresent(playerId -> lastResults.put(playerId, new DamageTraceEntry(result)));
    }

    public void recordExecution(ExecutionResult result) {
        result.attributedPlayerId().filter(enabledPlayers::contains)
                .ifPresent(playerId -> lastResults.put(playerId, new ExecutionTraceEntry(result)));
    }

    public Optional<CombatTraceEntry> last(UUID playerId) {
        return Optional.ofNullable(lastResults.get(playerId));
    }

    public String format(CombatTraceEntry entry) {
        return switch (entry) {
            case DamageTraceEntry damage -> formatDamage(damage.result());
            case ExecutionTraceEntry execution -> formatExecution(execution.result());
        };
    }

    private String formatDamage(CombatResult result) {
        var context = result.context();
        var breakdown = result.breakdown();
        String parent = context.parentSequenceId().isPresent()
                ? Long.toString(context.parentSequenceId().getAsLong())
                : "none";
        String enchantments = context.effectiveEnchantments().entries().stream()
                .map(enchantment -> enchantment.id() + "=" + enchantment.level() + enchantment.provenance())
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));
        return "type=" + context.channel()
                + " sequence=" + context.attackSequenceId()
                + " parent=" + parent
                + " category=" + context.category()
                + " policy=" + context.recursionPolicy()
                + " excludedProcs=" + context.excludedProcEffectIds()
                + " flags=" + context.flags()
                + " base=" + formatNumber(breakdown.baseOrdinaryDamage())
                + " additive=" + formatNumber(breakdown.additiveOutgoingBonus())
                + " contributions=" + breakdown.additiveContributions().stream()
                        .map(contribution -> contribution.sourceId() + "=" + formatNumber(contribution.bonus()))
                        .collect(java.util.stream.Collectors.joining(",", "[", "]"))
                + " outgoingProduct=" + formatNumber(breakdown.outgoingMultiplierProduct())
                + " incomingProduct=" + formatNumber(breakdown.incomingMultiplierProduct())
                + " ordinaryBeforeVanilla=" + formatNumber(breakdown.finalOrdinaryDamage())
                + " committedHealth=" + formatNumber(result.committedHealthDamage())
                + " queuedTrue=" + formatNumber(result.totalQueuedTrueDamage())
                + " enchantments=" + enchantments;
    }

    private String formatExecution(ExecutionResult result) {
        String parent = result.parentSequenceId().isPresent()
                ? Long.toString(result.parentSequenceId().getAsLong()) : "none";
        return "type=EXECUTION sequence=" + result.sequenceId()
                + " parent=" + parent
                + " cause=" + result.cause().id()
                + " target=" + result.targetId()
                + " executed=" + result.executed();
    }

    private static String formatNumber(double value) {
        return String.format(java.util.Locale.ROOT, "%.3f", value);
    }
}
