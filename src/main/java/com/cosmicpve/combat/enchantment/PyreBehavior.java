package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.proc.ProcEvent;
import net.minecraft.tags.ItemTags;

public final class PyreBehavior {
    public static final int FIRE_TICKS = 60;
    private PyreBehavior() {}

    public static double chance(int level) { return .05 * Math.max(0, Math.min(3, level)); }

    public static boolean eligible(ProcEvent event) {
        return event.attacker() != null && event.target() != null
                && event.combatResult().filter(hit -> hit.isCommittedDamagingHit()
                    && hit.context().parentSequenceId().isEmpty()
                    && hit.context().channel() == DamageChannel.ORDINARY
                    && hit.context().category() == AttackCategory.MELEE
                    && hit.context().weaponSnapshot().stack().is(ItemTags.AXES)).isPresent();
    }

    public static void activate(ProcEvent event) {
        if (eligible(event)) event.target().setRemainingFireTicks(
                Math.max(event.target().getRemainingFireTicks(), FIRE_TICKS));
    }
}
