package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.IncomingDamageContribution;
import com.cosmicpve.combat.pipeline.IncomingDamageContributor;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.combat.stack.CombatStackService;
import java.util.List;
import net.minecraft.resources.Identifier;

/** Independent five-second negative stacks; deliberately no gameplay cap. */
public final class HexBehavior implements OutgoingDamageContributor, IncomingDamageContributor {
    public static final Identifier STACK_ID = CosmicPVE.id("hex");
    public static final double PER_STACK = 0.02;
    private final CombatStackService stacks;

    public HexBehavior(CombatStackService stacks) { this.stacks = stacks; }
    public static double chance(int level) { return .01 * Math.max(0, Math.min(5, level)); }
    public static double outgoingPenalty(int count) { return -PER_STACK * Math.max(0, count); }
    public static double incomingMultiplier(int count) { return 1.0 + PER_STACK * Math.max(0, count); }

    private int count(net.minecraft.world.entity.LivingEntity entity) {
        var server = entity.level().getServer();
        return server == null ? 0 : stacks.count(entity, STACK_ID, server.getTickCount());
    }

    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.attacker() == null) return List.of();
        int count = count(context.attacker());
        return count == 0 ? List.of() : List.of(new OutgoingDamageContribution(STACK_ID, outgoingPenalty(count)));
    }

    @Override public List<IncomingDamageContribution> resolveIncoming(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.target() == null) return List.of();
        int count = count(context.target());
        return count == 0 ? List.of() : List.of(new IncomingDamageContribution(STACK_ID, incomingMultiplier(count)));
    }
}
