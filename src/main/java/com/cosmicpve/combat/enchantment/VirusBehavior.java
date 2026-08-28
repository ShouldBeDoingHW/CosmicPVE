package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.action.ChildCombatActionService;
import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.TrueDamagePacket;
import com.cosmicpve.combat.proc.ProcActivation;
import net.minecraft.world.effect.MobEffects;

public final class VirusBehavior {
    public static final float HEAL_HP = 1.0F;

    private VirusBehavior() {}

    public static double trueDamage(int level) {
        return 0.4 * Math.max(0, Math.min(3, level));
    }

    public static boolean eligible(ProcActivation activation) {
        var event = activation.event();
        var parent = event.combatResult().orElse(null);
        return parent != null && parent.context().channel() == DamageChannel.ORDINARY
                && parent.context().category() == AttackCategory.PROJECTILE
                && event.attacker() != null && !event.attacker().isDeadOrDying()
                && event.target() != null && !event.target().isDeadOrDying()
                && event.target().hasEffect(MobEffects.POISON);
    }

    public static TrueDamagePacket packet(int level) {
        return TrueDamagePacket.standard(CosmicPVE.id("virus"), trueDamage(level));
    }

    public static void activate(ProcActivation activation, int level, ChildCombatActionService childActions) {
        if (!eligible(activation)) return;
        var parent = activation.event().combatResult().orElseThrow();
        var outcome = childActions.deliverTrue(
                parent.context(), activation.event().target(), packet(level), RecursionPolicy.NO_PROCS);
        if (outcome.accepted()) activation.event().attacker().heal(HEAL_HP);
    }
}
