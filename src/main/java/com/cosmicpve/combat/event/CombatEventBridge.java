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
import com.cosmicpve.combat.pipeline.IncomingDamageContributor;
import com.cosmicpve.combat.pipeline.PreDefenseBoundsContributor;
import com.cosmicpve.combat.memory.RecentCombatMemoryService;
import com.cosmicpve.combat.proc.ProcEventService;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import com.cosmicpve.equipment.skin.WeaponSkinResolver;
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
import com.cosmicpve.combat.enchantment.DevourBehavior;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;

/** Thin NeoForge adapter; all damage arithmetic remains in {@link CombatEngine}. */
public final class CombatEventBridge {
    private final CombatEngine engine;
    private final DamageAttributionService attribution;
    private final AttackSequenceService sequences;
    private final CombatTraceService traces;
    private final EffectiveEnchantmentsResolver enchantments;
    private final ProcEventService procEvents;
    private final OutgoingDamageContributor outgoingContributors;
    private final IncomingDamageContributor incomingContributors;
    private final PreDefenseBoundsContributor preDefenseBounds;
    private final RecentCombatMemoryService recentCombatMemory;
    private final WeaponSkinResolver weaponSkins;
    private final com.cosmicpve.reward.lootbox.SignatureWeaponCombatService signatureWeapons;
    private final Map<DamageContainer, PendingCombat> incomingCandidates =
            Collections.synchronizedMap(new WeakHashMap<>());
    private final ThreadLocal<Deque<PendingCombat>> acceptedDamageStack =
            ThreadLocal.withInitial(ArrayDeque::new);

    public CombatEventBridge(
            CombatEngine engine,
            DamageAttributionService attribution,
            AttackSequenceService sequences,
            CombatTraceService traces,
            EffectiveEnchantmentsResolver enchantments,
            ProcEventService procEvents,
            OutgoingDamageContributor outgoingContributors,
            IncomingDamageContributor incomingContributors,
            PreDefenseBoundsContributor preDefenseBounds,
            RecentCombatMemoryService recentCombatMemory,
            WeaponSkinResolver weaponSkins,
            com.cosmicpve.reward.lootbox.SignatureWeaponCombatService signatureWeapons) {
        this.engine = engine;
        this.attribution = attribution;
        this.sequences = sequences;
        this.traces = traces;
        this.enchantments = enchantments;
        this.procEvents = procEvents;
        this.outgoingContributors = outgoingContributors;
        this.incomingContributors = incomingContributors;
        this.preDefenseBounds = preDefenseBounds;
        this.recentCombatMemory = recentCombatMemory;
        this.weaponSkins = weaponSkins;
        this.signatureWeapons = signatureWeapons;
    }

    public void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.isCanceled() || event.getEntity().level().isClientSide()) {
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
                enchantments.resolve(resolved.attacker(), resolved.weaponSnapshot().stack(),
                        weaponSkins.virtualEnchantments(resolved.weaponSnapshot().stack())),
                sequence.id(),
                sequence.parentId(),
                RecursionPolicy.NORMAL,
                Set.of());
        var unchanged = CombatCalculationRequest.unchanged(event.getAmount());
        var resolvedOutgoing = outgoingContributors.resolve(context);
        var request = new CombatCalculationRequest(
                unchanged.baseOrdinaryDamage() + signatureWeapons.baseDamageBonus(context), unchanged.additiveOutgoingBonus(),
                unchanged.separateOutgoingMultipliers(), preDefenseBounds.resolvePreDefenseBounds(context),
                unchanged.incomingMultipliers(), unchanged.finalOrdinaryBounds(), unchanged.trueDamagePackets(),
                resolvedOutgoing, incomingContributors.resolveIncoming(context));
        CombatResult provisional = engine.calculate(context, request);
        boolean devourActivated = false;
        int devourLevel = Math.min(4, context.effectiveEnchantments().level(ModEnchantments.DEVOUR.identifier()));
        if (devourLevel > 0 && context.attacker() != null) {
            var preDamage = procEvents.onPreDamageCalculation(provisional);
            devourActivated = preDamage.evaluations().stream().anyMatch(evaluation ->
                    evaluation.candidateId().equals(ModEnchantments.DEVOUR.identifier()) && evaluation.activated());
            if (devourActivated) {
                var withDevour = new java.util.ArrayList<>(resolvedOutgoing);
                withDevour.add(new OutgoingDamageContribution(
                        ModEnchantments.DEVOUR.identifier(), DevourBehavior.damageBonus(devourLevel)));
                request = new CombatCalculationRequest(
                        unchanged.baseOrdinaryDamage() + signatureWeapons.baseDamageBonus(context), unchanged.additiveOutgoingBonus(),
                        unchanged.separateOutgoingMultipliers(), request.preDefenseBounds(),
                        unchanged.incomingMultipliers(), unchanged.finalOrdinaryBounds(), unchanged.trueDamagePackets(),
                        withDevour, request.incomingContributions());
                provisional = engine.calculate(context, request);
            }
        }
        event.setAmount((float) Math.min(Float.MAX_VALUE, provisional.breakdown().finalOrdinaryDamage()));
        incomingCandidates.put(event.getContainer(), new PendingCombat(provisional, devourActivated));
    }

    private void handleScopedDamage(LivingIncomingDamageEvent event, CombatDeliveryScope.State scoped) {
        var context = scoped.context();
        if (context.target() != event.getEntity() || context.damageSource() != event.getSource()) {
            throw new IllegalStateException("Scoped child damage did not match its NeoForge callback");
        }
        if (scoped.doublestrikeBypass()) {
            boolean common = context.channel() == DamageChannel.ORDINARY
                    && context.recursionPolicy() == RecursionPolicy.LIMITED_OFFENSIVE_REROLL
                    && context.parentSequenceId().isPresent();
            boolean doublestrike = event.getSource().is(ModDamageTypes.DOUBLESTRIKE)
                    && context.excludedProcEffectIds().contains(ModEnchantments.DOUBLESTRIKE.identifier());
            boolean cleave = event.getSource().is(ModDamageTypes.CLEAVE)
                    && context.excludedProcEffectIds().contains(ModEnchantments.CLEAVE.identifier())
                    && context.excludedProcEffectIds().contains(ModEnchantments.MIGHTY_CLEAVE.identifier());
            if (!common || (!doublestrike && !cleave))
                throw new IllegalStateException("Invalid linked ordinary-child hurt-immunity bypass scope");
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
            var unchanged = CombatCalculationRequest.unchanged(event.getAmount());
            provisional = engine.calculate(context, new CombatCalculationRequest(
                    unchanged.baseOrdinaryDamage(), 0.0, List.of(), preDefenseBounds.resolvePreDefenseBounds(context), List.of(),
                    unchanged.finalOrdinaryBounds(), List.of(), List.of(), incomingContributors.resolveIncoming(context)));
            event.setAmount((float) Math.min(Float.MAX_VALUE, provisional.breakdown().finalOrdinaryDamage()));
        }
        incomingCandidates.put(event.getContainer(), new PendingCombat(provisional, false));
    }

    public void onDamageAccepted(LivingDamageEvent.Pre event) {
        PendingCombat result = incomingCandidates.remove(event.getContainer());
        if (event.getNewDamage() <= 0.0F) return;
        if (result != null) {
            acceptedDamageStack.get().push(result);
        }
    }

    /** Read-only pre-commit seam for defensive reactions that need the resolved ordinary parent value. */
    public java.util.Optional<CombatResult> provisional(net.neoforged.neoforge.common.damagesource.DamageContainer container) {
        PendingCombat pending = incomingCandidates.get(container);
        return pending == null ? java.util.Optional.empty() : java.util.Optional.of(pending.result());
    }

    public void onDamageCommitted(LivingDamageEvent.Post event) {
        Deque<PendingCombat> stack = acceptedDamageStack.get();
        PendingCombat pending = stack.poll();
        if (stack.isEmpty()) {
            acceptedDamageStack.remove();
        }
        if (pending == null) {
            return;
        }
        CombatResult provisional = pending.result();
        if (provisional.context().target() != event.getEntity()
                || provisional.context().damageSource() != event.getSource()) {
            CosmicPVE.LOGGER.warn("Discarding mismatched combat trace for sequence {}", provisional.context().attackSequenceId());
            return;
        }

        var committed = provisional.commit(event.getNewDamage());
        traces.recordCommitted(committed);
        var server = event.getEntity().level().getServer();
        if (server != null) recentCombatMemory.recordCommitted(committed, server.getTickCount());
        if (pending.devourActivated() && committed.isCommittedDamagingHit()
                && committed.context().attacker() instanceof net.minecraft.world.entity.player.Player player) {
            DevourBehavior.commit(player);
        }
        com.cosmicpve.personalvault.PersonalVaultRuntime.combatTags().onCommitted(committed);
        if (committed.isCommittedDamagingHit()
                && committed.context().channel() == DamageChannel.ORDINARY
                && committed.context().parentSequenceId().isEmpty()
                && committed.context().recursionPolicy() == RecursionPolicy.NORMAL
                && committed.context().attacker() instanceof net.minecraft.world.entity.player.Player player
                && committed.context().target() instanceof com.cosmicpve.entity.inventor.InventorEntity inventor) {
            inventor.consumeStrikeCharge(player.getUUID());
        }
        procEvents.onCommittedDamage(committed);
    }

    private record PendingCombat(CombatResult result, boolean devourActivated) {}
}
