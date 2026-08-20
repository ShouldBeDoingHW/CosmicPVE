package com.cosmicpve.combat.execution;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.debug.CombatTraceService;
import com.cosmicpve.combat.pipeline.AttackSequenceService;
import java.util.HashSet;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/** A terminal kill path: no hurt call, damage packet, mitigation, absorption, or totem check. */
public final class ExecutionService {
    private final AttackSequenceService sequences;
    private final CombatTraceService traces;
    private final ThreadLocal<Set<UUID>> activeExecutions = ThreadLocal.withInitial(HashSet::new);

    public ExecutionService(AttackSequenceService sequences, CombatTraceService traces) {
        this.sequences = sequences;
        this.traces = traces;
    }

    public ExecutionResult execute(
            LivingEntity target,
            ExecutionCause cause,
            @Nullable LivingEntity actor,
            @Nullable CombatContext parent) {
        var sequence = parent == null
                ? sequences.nextRoot()
                : sequences.nextChild(parent.attackSequenceId());
        Optional<UUID> playerId = actor instanceof Player player
                ? Optional.of(player.getUUID())
                : parent == null ? Optional.empty() : parent.attributedPlayerId();
        OptionalLong parentId = sequence.parentId();

        if (!(target.level() instanceof ServerLevel level) || target.isDeadOrDying()) {
            return new ExecutionResult(cause, target.getUUID(), playerId, sequence.id(), parentId, false);
        }

        Set<UUID> active = activeExecutions.get();
        if (!active.add(target.getUUID())) {
            throw new IllegalStateException("Execution is already active for " + target.getUUID());
        }
        try {
            var source = level.damageSources().source(DamageTypes.GENERIC_KILL, actor, actor);
            target.setHealth(0.0F);
            target.die(source);
            target.setHealth(0.0F);
        } finally {
            active.remove(target.getUUID());
            if (active.isEmpty()) {
                activeExecutions.remove();
            }
        }

        var result = new ExecutionResult(cause, target.getUUID(), playerId, sequence.id(), parentId, true);
        CosmicPVE.LOGGER.info(
                "Combat execution cause={} target={} sequence={} parent={} actor={}",
                cause.id(), target.getUUID(), sequence.id(),
                parentId.isPresent() ? parentId.getAsLong() : "none",
                actor == null ? "none" : actor.getUUID());
        traces.recordExecution(result);
        return result;
    }

    /** Future Cosmic death-prevention hooks must return immediately when this is true. */
    public boolean isExecuting(LivingEntity target) {
        return activeExecutions.get().contains(target.getUUID());
    }
}
