package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.proc.ProcEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Items;

/** Crossbow projectile impact grants a short vanilla Absorption effect to its shooter. */
public final class HealingBehavior {
    public static final int DURATION_TICKS = 80;
    private HealingBehavior() {}
    public static double chance(int level) { return level > 0 ? .10 : 0.0; }
    public static int amplifier(int level) { return Math.max(0, Math.min(2, level) - 1); }
    public static int absorptionHp(int level) { return 4 * Math.max(0, Math.min(2, level)); }
    public static boolean eligible(ProcEvent event) {
        return event.attacker() != null && event.target() != null
                && event.combatResult().filter(hit -> hit.isCommittedDamagingHit()
                        && hit.context().category() == AttackCategory.PROJECTILE
                        && hit.context().directSource() instanceof Projectile
                        && hit.context().weaponSnapshot().stack().is(Items.CROSSBOW)).isPresent();
    }
    public static void activate(ProcEvent event, int level) {
        if (eligible(event)) event.attacker().addEffect(new MobEffectInstance(
                MobEffects.ABSORPTION, DURATION_TICKS, amplifier(level)), event.attacker());
    }
}
