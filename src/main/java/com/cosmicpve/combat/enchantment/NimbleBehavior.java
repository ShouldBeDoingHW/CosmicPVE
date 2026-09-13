package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.cooldown.CooldownScope;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.combat.proc.ChildProcEligibility;
import com.cosmicpve.combat.proc.ProcActivation;
import com.cosmicpve.combat.proc.ProcActivationListener;
import com.cosmicpve.combat.proc.ProcCandidate;
import com.cosmicpve.combat.proc.ProcCandidateResolver;
import com.cosmicpve.combat.proc.ProcEngine;
import com.cosmicpve.combat.proc.ProcEvent;
import com.cosmicpve.combat.proc.ProcHook;
import com.cosmicpve.combat.proc.ProcProvenance;
import com.cosmicpve.combat.proc.ProcSourceKind;
import com.cosmicpve.combat.stack.CombatStackService;
import com.cosmicpve.combat.stack.StackApplication;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public final class NimbleBehavior implements OutgoingDamageContributor, ProcCandidateResolver, ProcActivationListener {
    public static final Identifier STACK_ID = CosmicPVE.id("nimble");
    private final CombatStackService stacks;
    public NimbleBehavior(CombatStackService stacks) { this.stacks = stacks; }
    public static int maximumStacks(int level) { return Math.max(0, Math.min(4, level)) * 2; }
    public static long lifetimeTicks(int level) { return (5L + Math.max(1, Math.min(4, level))) * 20L; }
    public static double outgoingBonus(int count) { return Math.max(0, Math.min(8, count)) * .01; }

    @Override public void activated(ProcActivation activation) {
        if (!DefensiveCosmicEnchantments.defensive(activation.candidate())) return;
        if (activation.event().hook() != ProcHook.ON_DAMAGE_TAKEN
                && activation.event().hook() != ProcHook.ON_TARGETED
                && activation.event().hook() != ProcHook.ON_PRE_DEATH) return;
        LivingEntity wearer = activation.event().target();
        if (wearer == null || !activation.event().ownerId().equals(wearer.getUUID())) return;
        int level = EnchantmentLevels.onStack(wearer, wearer.getItemBySlot(EquipmentSlot.FEET), ModEnchantments.NIMBLE);
        if (level <= 0) return;
        long tick = activation.event().serverTick();
        if (stacks.count(wearer, STACK_ID, tick) >= maximumStacks(level)) return;
        stacks.addStack(wearer, STACK_ID, 1,
                StackApplication.ephemeral(Optional.of(wearer.getUUID()), activation.event().tracePlayerId()),
                tick, (int) lifetimeTicks(level));
    }

    @Override public List<OutgoingDamageContribution> resolve(com.cosmicpve.combat.api.CombatContext context) {
        if (!validParent(context) || context.attacker() == null || context.attacker().level().getServer() == null) return List.of();
        int count = stacks.count(context.attacker(), STACK_ID, context.attacker().level().getServer().getTickCount());
        return count == 0 ? List.of() : List.of(new OutgoingDamageContribution(STACK_ID, outgoingBonus(count)));
    }

    @Override public List<ProcCandidate> resolve(ProcEvent event) {
        if (event.hook() != ProcHook.ON_VALID_HIT || event.attacker() == null
                || event.combatResult().filter(result -> validParent(result.context())).isEmpty()) return List.of();
        int count = stacks.count(event.attacker(), STACK_ID, event.serverTick());
        if (count == 0) return List.of();
        return List.of(new ProcCandidate(STACK_ID, ProcHook.ON_VALID_HIT, 1.0, Optional.empty(), 0,
                CooldownScope.EPHEMERAL_COMBAT, Optional.empty(), List.of(), List.of(), Optional.of(STACK_ID),
                ChildProcEligibility.ROOT_ONLY, Set.of(ProcEngine.DETERMINISTIC_CLASSIFICATION), STACK_ID,
                activation -> stacks.removeAll(activation.event().attacker(), STACK_ID, activation.event().serverTick()),
                new ProcProvenance(ProcSourceKind.OTHER, STACK_ID)));
    }

    public static boolean validParent(com.cosmicpve.combat.api.CombatContext context) {
        return context.channel() == DamageChannel.ORDINARY && context.parentSequenceId().isEmpty()
                && context.recursionPolicy() == RecursionPolicy.NORMAL;
    }
}
