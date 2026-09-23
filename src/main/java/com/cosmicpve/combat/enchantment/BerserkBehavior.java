package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.proc.ProcEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Strength is granted after the triggering hit has committed, for later attacks. */
public final class BerserkBehavior {
    private BerserkBehavior() {}
    public static double chance(int level) { return .01 * Math.max(0, Math.min(5, level)); }
    public static int durationTicks(int level) { return 20 * Math.max(0, Math.min(5, level)); }
    public static boolean eligible(ProcEvent event) {
        return event.attacker() != null && event.target() != null
                && event.combatResult().filter(hit -> hit.isCommittedDamagingHit()
                        && hit.context().category() == AttackCategory.MELEE
                        && hit.context().weaponSnapshot().stack().is(ItemTags.AXES)).isPresent();
    }
    public static void activate(ProcEvent event, int level) {
        if (eligible(event)) event.attacker().addEffect(new MobEffectInstance(
                MobEffects.STRENGTH, durationTicks(level), 0), event.attacker());
    }
}
