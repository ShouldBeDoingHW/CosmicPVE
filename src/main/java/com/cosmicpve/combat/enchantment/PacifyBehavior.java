package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.ownership.GeneralAllyResolver;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.combat.proc.ProcEvent;
import com.cosmicpve.combat.stack.CombatStackService;
import com.cosmicpve.combat.stack.StackApplication;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Items;

/** Bow-snapshot proc and one-stack ordinary outgoing penalty. */
public final class PacifyBehavior implements OutgoingDamageContributor {
    public static final Identifier STACK_ID = CosmicPVE.id("pacify");
    public static final int DURATION_TICKS = 60;
    private final CombatStackService stacks;

    public PacifyBehavior(CombatStackService stacks) { this.stacks = stacks; }

    public static double chance(int level) { return Math.max(0, Math.min(4, level)) * .02; }
    public static double penalty(int level) { return -Math.max(0, Math.min(4, level)) * .01; }

    public static boolean eligible(ProcEvent event) {
        return event.attacker() != null && event.target() != null
                && event.combatResult().filter(hit -> hit.isCommittedDamagingHit()
                        && hit.context().parentSequenceId().isEmpty()
                        && hit.context().category() == AttackCategory.PROJECTILE
                        && hit.context().directSource() instanceof Projectile
                        && hit.context().weaponSnapshot().stack().is(Items.BOW)
                        && !GeneralAllyResolver.production().isAlly(event.attacker(), event.target())).isPresent();
    }

    public void activate(ProcEvent event, int level) {
        if (!eligible(event)) return;
        var attacker = event.attacker();
        var target = event.target();
        stacks.addStack(target, STACK_ID, 1,
                StackApplication.ephemeral(Optional.of(attacker.getUUID()),
                        attacker instanceof net.minecraft.server.level.ServerPlayer
                                ? Optional.of(attacker.getUUID()) : Optional.empty()).withPotency(level),
                target.level().getServer().getTickCount(), DURATION_TICKS);
    }

    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.attacker() == null) return List.of();
        var server = context.attacker().level().getServer();
        if (server == null) return List.of();
        var active = stacks.activeStacks(context.attacker(), server.getTickCount()).stream()
                .filter(stack -> stack.definitionId().equals(STACK_ID)).findFirst();
        if (active.isEmpty() || active.orElseThrow().instances().isEmpty()) return List.of();
        int level = active.orElseThrow().instances().getFirst().potency();
        return level <= 0 ? List.of() : List.of(new OutgoingDamageContribution(STACK_ID, penalty(level)));
    }
}
