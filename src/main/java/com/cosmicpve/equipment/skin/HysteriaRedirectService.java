package com.cosmicpve.equipment.skin;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.action.ChildCombatActionService;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.cooldown.CooldownScope;
import com.cosmicpve.combat.proc.ChildProcEligibility;
import com.cosmicpve.combat.proc.ProcCandidate;
import com.cosmicpve.combat.proc.ProcEngine;
import com.cosmicpve.combat.proc.ProcEventService;
import com.cosmicpve.combat.proc.ProcHook;
import com.cosmicpve.combat.proc.ProcProvenance;
import com.cosmicpve.combat.proc.ProcSourceKind;
import com.cosmicpve.combat.stack.CombatStackService;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/** Pre-commit redirect for the negative Hysteria stack. */
public final class HysteriaRedirectService {
    public static final double CHANCE_PER_STACK = 0.005;
    public static final int MAX_STACKS = 8;
    public static final Set<Identifier> CHILD_EXCLUSIONS = Set.of(
            WeaponSkinCombatResolver.HYSTERIA, ModEnchantments.CLEAVE.identifier(),
            ModEnchantments.MIGHTY_CLEAVE.identifier(), ModEnchantments.DOUBLESTRIKE.identifier(),
            ModEnchantments.INVERSION.identifier(), CosmicPVE.id("mighty"));

    private final CombatStackService stacks;
    private final ProcEventService procs;
    private final ChildCombatActionService children;

    public HysteriaRedirectService(CombatStackService stacks, ProcEventService procs,
                                   ChildCombatActionService children) {
        this.stacks = stacks;
        this.procs = procs;
        this.children = children;
    }

    public boolean redirect(com.cosmicpve.combat.api.CombatResult provisional, LivingDamageEvent.Pre event) {
        var context = provisional.context();
        if (context.channel() != DamageChannel.ORDINARY || context.recursionPolicy() != RecursionPolicy.NORMAL
                || context.parentSequenceId().isPresent() || context.attacker() == null
                || context.attacker() == context.target() || context.attacker().level().getServer() == null) return false;
        int count = Math.min(MAX_STACKS, stacks.count(context.attacker(), WeaponSkinCombatResolver.HYSTERIA,
                context.attacker().level().getServer().getTickCount()));
        if (count == 0) return false;
        var activated = new boolean[1];
        var candidate = new ProcCandidate(WeaponSkinCombatResolver.HYSTERIA, ProcHook.ON_TARGETED,
                chance(count), Optional.empty(), 0L, CooldownScope.EPHEMERAL_COMBAT, Optional.empty(),
                List.of(), List.of(), Optional.of(CosmicPVE.id("hysteria_once")), ChildProcEligibility.ROOT_ONLY,
                Set.of(ProcEngine.UNMODIFIED_CHANCE_CLASSIFICATION), WeaponSkinCombatResolver.HYSTERIA,
                activation -> activated[0] = true,
                new ProcProvenance(ProcSourceKind.OTHER, WeaponSkinCombatResolver.HYSTERIA));
        procs.dispatchTargetedCandidate(context.attacker(), context.attacker(), context.target(), candidate);
        if (!activated[0]) return false;

        // Snapshot is the engine-resolved ordinary packet before vanilla mitigation; the self-hit then
        // traverses vanilla mitigation for the attacker exactly once.
        double snapshot = redirectSnapshot(provisional.breakdown().finalOrdinaryDamage());
        event.setNewDamage(0.0F);
        children.deliverOrdinary(context, context.attacker(), snapshot, RecursionPolicy.NO_PROCS, CHILD_EXCLUSIONS);
        return true;
    }

    public static double chance(int stacks) {
        return Math.min(MAX_STACKS, Math.max(0, stacks)) * CHANCE_PER_STACK;
    }

    public static double redirectSnapshot(double resolvedPreMitigationOrdinaryDamage) {
        if (!Double.isFinite(resolvedPreMitigationOrdinaryDamage) || resolvedPreMitigationOrdinaryDamage <= 0.0)
            throw new IllegalArgumentException("Hysteria requires positive resolved ordinary damage");
        return resolvedPreMitigationOrdinaryDamage;
    }
}
