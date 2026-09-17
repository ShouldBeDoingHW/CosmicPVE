package com.cosmicpve.trial.madness;

import com.cosmicpve.combat.api.*;
import com.cosmicpve.combat.pipeline.*;
import com.cosmicpve.trial.TrialRuntime;
import com.cosmicpve.trial.TrialSession;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import java.util.List;

/** Live Trial state contributes to ordinary combat, never a second damage event. */
public final class MadnessCombatContributor implements OutgoingDamageContributor, IncomingDamageContributor {
    public static boolean ordinary(CombatContext context) {
        return context.channel()==DamageChannel.ORDINARY && context.category()!=AttackCategory.ENVIRONMENTAL
                && (context.damageSource()==null || !context.damageSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                    && !context.damageSource().is(DamageTypeTags.BYPASSES_EFFECTS));
    }
    private static TrialSession session(LivingEntity entity) {
        if(!(entity instanceof ServerPlayer player)) return null;
        return TrialRuntime.sessions().active(player.level().getServer())
                .filter(s -> s.activeParticipant(player.getUUID())).orElse(null);
    }
    public static List<OutgoingDamageContribution> outgoing(CombatContext context,TrialSession session) {
        if(!ordinary(context) || session==null || context.attacker()==null
                || !session.activeParticipant(context.attacker().getUUID())) return List.of();
        return session.progress().madness().active().stream().filter(d -> d.handler().getPath().equals("wet_noodle"))
                .map(d -> new OutgoingDamageContribution(d.id(),-0.15)).toList();
    }
    public static List<IncomingDamageContribution> incoming(CombatContext context,TrialSession session) {
        if(!ordinary(context) || session==null || context.target()==null
                || !session.activeParticipant(context.target().getUUID())) return List.of();
        return session.progress().madness().active().stream().filter(d -> d.handler().getPath().equals("thin_skin"))
                .map(d -> new IncomingDamageContribution(d.id(),1.15)).toList();
    }
    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) { return outgoing(context,session(context.attacker())); }
    @Override public List<IncomingDamageContribution> resolveIncoming(CombatContext context) { return incoming(context,session(context.target())); }
}
