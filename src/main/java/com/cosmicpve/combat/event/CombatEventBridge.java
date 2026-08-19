package com.cosmicpve.combat.event;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.CombatResult;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.attribution.DamageAttributionService;
import com.cosmicpve.combat.debug.CombatTraceService;
import com.cosmicpve.combat.pipeline.AttackSequenceService;
import com.cosmicpve.combat.pipeline.CombatCalculationRequest;
import com.cosmicpve.combat.pipeline.CombatEngine;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.WeakHashMap;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** Thin NeoForge adapter; all damage arithmetic remains in {@link CombatEngine}. */
public final class CombatEventBridge {
    private final CombatEngine engine;
    private final DamageAttributionService attribution;
    private final AttackSequenceService sequences;
    private final CombatTraceService traces;
    private final Map<DamageContainer, CombatResult> incomingCandidates =
            Collections.synchronizedMap(new WeakHashMap<>());
    private final ThreadLocal<Deque<CombatResult>> acceptedDamageStack =
            ThreadLocal.withInitial(ArrayDeque::new);

    public CombatEventBridge(
            CombatEngine engine,
            DamageAttributionService attribution,
            AttackSequenceService sequences,
            CombatTraceService traces) {
        this.engine = engine;
        this.attribution = attribution;
        this.sequences = sequences;
        this.traces = traces;
    }

    public void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        var resolved = attribution.resolve(event.getSource());
        var sequence = sequences.nextRoot();
        var context = new CombatContext(
                resolved.directSource(),
                resolved.creditedSource(),
                resolved.attacker(),
                event.getEntity(),
                resolved.playerId(),
                event.getSource(),
                resolved.category(),
                DamageChannel.ORDINARY,
                resolved.flags(),
                resolved.weaponSnapshot(),
                sequence.id(),
                sequence.parentId(),
                RecursionPolicy.NORMAL);
        CombatResult provisional = engine.calculate(context, CombatCalculationRequest.unchanged(event.getAmount()));
        event.setAmount((float) Math.min(Float.MAX_VALUE, provisional.breakdown().finalOrdinaryDamage()));
        incomingCandidates.put(event.getContainer(), provisional);
    }

    public void onDamageAccepted(LivingDamageEvent.Pre event) {
        CombatResult result = incomingCandidates.remove(event.getContainer());
        if (result != null) {
            acceptedDamageStack.get().push(result);
        }
    }

    public void onDamageCommitted(LivingDamageEvent.Post event) {
        Deque<CombatResult> stack = acceptedDamageStack.get();
        CombatResult provisional = stack.poll();
        if (stack.isEmpty()) {
            acceptedDamageStack.remove();
        }
        if (provisional == null) {
            return;
        }
        if (provisional.context().target() != event.getEntity()
                || provisional.context().damageSource() != event.getSource()) {
            CosmicPVE.LOGGER.warn("Discarding mismatched combat trace for sequence {}", provisional.context().attackSequenceId());
            return;
        }

        traces.recordCommitted(provisional.commit(event.getNewDamage()));
    }
}
