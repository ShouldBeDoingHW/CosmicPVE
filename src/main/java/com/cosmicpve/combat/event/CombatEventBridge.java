package com.cosmicpve.combat.event;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.CombatResult;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.action.CombatDeliveryScope;
import com.cosmicpve.combat.attribution.DamageAttributionService;
import com.cosmicpve.combat.debug.CombatTraceService;
import com.cosmicpve.combat.pipeline.AttackSequenceService;
import com.cosmicpve.combat.pipeline.CombatCalculationRequest;
import com.cosmicpve.combat.pipeline.CombatEngine;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.combat.proc.ProcEventService;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import java.util.List;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.WeakHashMap;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import com.cosmicpve.registry.ModDamageTypes;
import com.cosmicpve.registry.ModEnchantments;
import java.util.Set;

/** Thin NeoForge adapter; all damage arithmetic remains in {@link CombatEngine}. */
public final class CombatEventBridge {
    private final CombatEngine engine;
    private final DamageAttributionService attribution;
    private final AttackSequenceService sequences;
    private final CombatTraceService traces;
    private final EffectiveEnchantmentsResolver enchantments;
    private final ProcEventService procEvents;
    private final OutgoingDamageContributor outgoingContributors;
    private final Map<DamageContainer, CombatResult> incomingCandidates =
            Collections.synchronizedMap(new WeakHashMap<>());
    private final ThreadLocal<Deque<CombatResult>> acceptedDamageStack =
            ThreadLocal.withInitial(ArrayDeque::new);

    public CombatEventBridge(
            CombatEngine engine,
            DamageAttributionService attribution,
            AttackSequenceService sequences,
            CombatTraceService traces,
            EffectiveEnchantmentsResolver enchantments,
            ProcEventService procEvents,
            OutgoingDamageContributor outgoingContributors) {
        this.engine = engine;
        this.attribution = attribution;
        this.sequences = sequences;
        this.traces = traces;
        this.enchantments = enchantments;
        this.procEvents = procEvents;
        this.outgoingContributors = outgoingContributors;
    }

    public void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        var scoped = CombatDeliveryScope.current();
        if (scoped.isPresent()) {
            handleScopedDamage(event, scoped.orElseThrow());
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
                enchantments.resolve(resolved.weaponSnapshot().stack(), List.of()),
                sequence.id(),
                sequence.parentId(),
                RecursionPolicy.NORMAL,
                Set.of());
        var unchanged = CombatCalculationRequest.unchanged(event.getAmount());
        var request = new CombatCalculationRequest(
                unchanged.baseOrdinaryDamage(), unchanged.additiveOutgoingBonus(),
                unchanged.separateOutgoingMultipliers(), unchanged.preDefenseBounds(),
                unchanged.incomingMultipliers(), unchanged.finalOrdinaryBounds(), unchanged.trueDamagePackets(),
                outgoingContributors.resolve(context));
        CombatResult provisional = engine.calculate(context, request);
        event.setAmount((float) Math.min(Float.MAX_VALUE, provisional.breakdown().finalOrdinaryDamage()));
        incomingCandidates.put(event.getContainer(), provisional);
    }

    private void handleScopedDamage(LivingIncomingDamageEvent event, CombatDeliveryScope.State scoped) {
        var context = scoped.context();
        if (context.target() != event.getEntity() || context.damageSource() != event.getSource()) {
            throw new IllegalStateException("Scoped child damage did not match its NeoForge callback");
        }
        if (scoped.doublestrikeBypass()
                && (context.channel() != DamageChannel.ORDINARY
                        || context.recursionPolicy() != RecursionPolicy.LIMITED_OFFENSIVE_REROLL
                        || context.parentSequenceId().isEmpty()
                        || !context.excludedProcEffectIds().contains(ModEnchantments.DOUBLESTRIKE.identifier())
                        || !event.getSource().is(ModDamageTypes.DOUBLESTRIKE))) {
            throw new IllegalStateException("Invalid Doublestrike hurt-immunity bypass scope");
        }

        CombatResult provisional;
        if (scoped.truePacket().isPresent()) {
            var packet = scoped.truePacket().orElseThrow();
            if (packet.bypassesArmor()) {
                event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reduction) -> 0.0F);
            }
            if (packet.bypassesAbsorption()) {
                event.addReductionModifier(DamageContainer.Reduction.ABSORPTION, (container, reduction) -> 0.0F);
            }
            if (packet.bypassesCustomReduction()) {
                event.addReductionModifier(DamageContainer.Reduction.ENCHANTMENTS, (container, reduction) -> 0.0F);
                event.addReductionModifier(DamageContainer.Reduction.MOB_EFFECTS, (container, reduction) -> 0.0F);
                event.addReductionModifier(DamageContainer.Reduction.INNATE_RESISTANCE, (container, reduction) -> 0.0F);
            }
            provisional = engine.calculate(
                    context,
                    new CombatCalculationRequest(
                            0.0, 0.0, List.of(), com.cosmicpve.combat.api.DamageBounds.UNBOUNDED,
                            List.of(), com.cosmicpve.combat.api.DamageBounds.UNBOUNDED, List.of(packet)));
        } else {
            provisional = engine.calculate(context, CombatCalculationRequest.unchanged(event.getAmount()));
            event.setAmount((float) Math.min(Float.MAX_VALUE, provisional.breakdown().finalOrdinaryDamage()));
        }
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

        var committed = provisional.commit(event.getNewDamage());
        traces.recordCommitted(committed);
        procEvents.onCommittedDamage(committed);
    }
}
