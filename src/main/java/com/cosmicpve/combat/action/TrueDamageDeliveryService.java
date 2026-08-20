package com.cosmicpve.combat.action;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.TrueDamagePacket;
import com.cosmicpve.registry.ModDamageTypes;
import net.minecraft.server.level.ServerLevel;

public final class TrueDamageDeliveryService {
    public CombatActionOutcome deliver(CombatContext childContext, TrueDamagePacket packet) {
        if (childContext.channel() != DamageChannel.TRUE || childContext.parentSequenceId().isEmpty()) {
            throw new IllegalArgumentException("True damage delivery requires a linked TRUE child context");
        }
        if (!(childContext.target().level() instanceof ServerLevel level) || childContext.target().isDeadOrDying()) {
            return outcome(childContext, packet.amount(), 0.0, false);
        }

        var source = level.damageSources().source(
                packet.bypassesArmor() ? ModDamageTypes.TRUE_DAMAGE : ModDamageTypes.MITIGATED_TRUE_DAMAGE,
                childContext.directSource(), childContext.creditedSource());
        var deliveredContext = childContext.withDamageSource(source);
        float before = childContext.target().getHealth();
        boolean accepted = CombatDeliveryScope.call(
                deliveredContext,
                packet,
                () -> childContext.target().hurtServer(level, source, (float) packet.amount()));
        double healthDamage = Math.max(0.0, before - childContext.target().getHealth());
        return outcome(deliveredContext, packet.amount(), healthDamage, accepted);
    }

    private static CombatActionOutcome outcome(
            CombatContext context, double requested, double healthDamage, boolean accepted) {
        return new CombatActionOutcome(
                context.attackSequenceId(), context.parentSequenceId().orElseThrow(),
                DamageChannel.TRUE, requested, healthDamage, accepted);
    }
}
