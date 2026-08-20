package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.action.ChildCombatActionService;
import com.cosmicpve.combat.api.CombatResult;
import com.cosmicpve.registry.ModEnchantments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class DoublestrikeBehavior {
    private DoublestrikeBehavior() {}

    public static double chance(int level) {
        return Math.min(1.0, 0.01 * Math.max(0, level));
    }

    /** Halves the parent's final ordinary amount before vanilla target mitigation and before separate true packets. */
    public static double childOrdinaryDamage(CombatResult parent) {
        return parent.breakdown().finalOrdinaryDamage() * 0.5;
    }

    public static void activate(CombatResult parent, ChildCombatActionService childActions) {
        var target = parent.context().target();
        if (target == null || target.isDeadOrDying()) {
            return;
        }
        double damage = childOrdinaryDamage(parent);
        if (!(damage > 0.0)) {
            return;
        }
        var outcome = childActions.deliverDoublestrike(
                parent.context(), target, damage, ModEnchantments.DOUBLESTRIKE.identifier());
        if (outcome.accepted() && target.level() instanceof ServerLevel level) {
            level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG,
                    SoundSource.PLAYERS, 1.0F, 1.15F);
        }
    }
}
