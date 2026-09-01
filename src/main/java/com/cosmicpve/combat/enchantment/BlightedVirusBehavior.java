package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.action.ChildCombatActionService;
import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.TrueDamagePacket;
import com.cosmicpve.combat.proc.ProcActivation;
import com.cosmicpve.registry.ModEnchantments;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class BlightedVirusBehavior {
    public static final int REGENERATION_TICKS = 100;
    private BlightedVirusBehavior() {}
    public static double trueDamage(int level) { return level <= 0 ? 0.0 : 1.0 + .25 * Math.min(3, level); }
    public static boolean eligible(ProcActivation activation) {
        var result = activation.event().combatResult().orElse(null);
        return result != null && result.context().channel() == DamageChannel.ORDINARY
                && result.context().category() == AttackCategory.PROJECTILE
                && activation.event().attacker() != null && activation.event().target() != null
                && activation.event().target().hasEffect(MobEffects.POISON);
    }
    public static void activate(ProcActivation activation, int level, ChildCombatActionService children) {
        if (!eligible(activation)) return;
        var parent = activation.event().combatResult().orElseThrow();
        var outcome = children.deliverTrue(parent.context(), activation.event().target(),
                TrueDamagePacket.standard(ModEnchantments.BLIGHTED_VIRUS.identifier(), trueDamage(level)),
                RecursionPolicy.NO_PROCS);
        if (outcome.accepted()) activation.event().attacker().addEffect(
                new MobEffectInstance(MobEffects.REGENERATION, REGENERATION_TICKS, 0));
    }
}
