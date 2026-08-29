package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.combat.stack.CombatStackService;
import java.util.List;
import net.minecraft.resources.Identifier;

public final class VoodooBehavior implements OutgoingDamageContributor {
    public static final Identifier STACK_ID = CosmicPVE.id("voodoo");
    private final CombatStackService stacks;
    public VoodooBehavior(CombatStackService stacks) { this.stacks = stacks; }
    public static double chance(int level) { return Math.max(0, Math.min(6, level)) * .01; }
    public static double penalty(int stacks) { return -.03 * Math.max(0, Math.min(5, stacks)); }
    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.attacker() == null) return List.of();
        var server = context.attacker().level().getServer();
        int count = server == null ? 0 : stacks.count(context.attacker(), STACK_ID, server.getTickCount());
        return count == 0 ? List.of() : List.of(new OutgoingDamageContribution(STACK_ID, penalty(count)));
    }
}
