package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.proc.ProcEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;

/** Ranger-exclusive Bow proc; the ordinary proc engine supplies Luck-relative chance handling. */
public final class PinpointBehavior {
    public static final int DURATION_TICKS = 80;
    public static final int AMPLIFIER = 0;
    private PinpointBehavior() {}
    public static double chance(int level) { return Math.min(.18, .03 * Math.max(0, level)); }
    public static boolean eligible(ProcEvent event) {
        return event.target() != null && !event.target().isDeadOrDying()
                && event.combatResult().filter(result -> result.isCommittedDamagingHit()
                        && result.context().category() == AttackCategory.PROJECTILE
                        && result.context().parentSequenceId().isEmpty()
                        && result.context().weaponSnapshot().stack().is(Items.BOW)).isPresent();
    }
    public static void activate(ProcEvent event) {
        if (eligible(event)) event.target().addEffect(
                new MobEffectInstance(MobEffects.WEAKNESS, DURATION_TICKS, AMPLIFIER), event.attacker());
    }
}
