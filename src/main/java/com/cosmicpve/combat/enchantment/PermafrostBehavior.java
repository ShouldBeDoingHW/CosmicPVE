package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.action.ChildCombatActionService;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.TrueDamagePacket;
import com.cosmicpve.combat.pipeline.IncomingDamageContribution;
import com.cosmicpve.combat.pipeline.IncomingDamageContributor;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.combat.proc.ProcEvent;
import com.cosmicpve.combat.stack.CombatStackService;
import com.cosmicpve.combat.stack.StackApplication;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/** Permafrost's generalized-stack application, threshold burst, and binary combat penalty. */
public final class PermafrostBehavior implements OutgoingDamageContributor, IncomingDamageContributor {
    public static final Identifier STACK_ID = CosmicPVE.id("permafrost");
    public static final double OUTGOING_PENALTY = -0.02;
    public static final double INCOMING_MULTIPLIER = 1.02;
    public static final double BURST_DAMAGE_HP = 6.0;
    private final CombatStackService stacks;

    public PermafrostBehavior(CombatStackService stacks) {
        this.stacks = stacks;
    }

    public static double chance(int level) {
        return Math.min(1.0, 0.025 * Math.max(0, Math.min(6, level)));
    }

    public static int threshold(int level) {
        if (level < 1 || level > 6) throw new IllegalArgumentException("Permafrost level must be in [1,6]");
        return 10 - level;
    }

    public static TrueDamagePacket burstPacket() {
        return TrueDamagePacket.standard(STACK_ID, BURST_DAMAGE_HP);
    }

    public static void activate(
            ProcEvent event, int level, CombatStackService stacks, ChildCombatActionService childActions) {
        LivingEntity attacker = event.attacker();
        LivingEntity wearer = event.target();
        if (attacker == null || wearer == null || attacker == wearer || attacker.isDeadOrDying()) return;
        var application = StackApplication.ephemeral(Optional.of(wearer.getUUID()), event.tracePlayerId());
        var result = stacks.addStack(attacker, STACK_ID, 1, application, event.serverTick());
        if (result.finalCount() >= threshold(level)) {
            stacks.removeAll(attacker, STACK_ID, event.serverTick());
            childActions.deliverTrueRoot(attacker, wearer, burstPacket(), RecursionPolicy.NO_PROCS);
        }
    }

    private boolean active(LivingEntity entity) {
        var server = entity.level().getServer();
        return server != null && stacks.count(entity, STACK_ID, server.getTickCount()) > 0;
    }

    @Override
    public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.attacker() == null || !active(context.attacker()))
            return List.of();
        return List.of(new OutgoingDamageContribution(STACK_ID, OUTGOING_PENALTY));
    }

    @Override
    public List<IncomingDamageContribution> resolveIncoming(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.target() == null || !active(context.target()))
            return List.of();
        return List.of(new IncomingDamageContribution(STACK_ID, INCOMING_MULTIPLIER));
    }
}
