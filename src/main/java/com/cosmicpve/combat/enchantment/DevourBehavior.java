package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.CombatResult;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/** Pre-calculation proc math with side effects committed only after positive red-health loss. */
public final class DevourBehavior {
    public static final double PROC_CHANCE = 0.05;
    public static final int HUNGER_COST = 1;
    public static final float HEAL_HP = 1.0F;

    private DevourBehavior() {}

    public static double damageBonus(int level) {
        return 0.05 * Math.max(0, Math.min(4, level));
    }

    public static boolean eligible(CombatResult provisional) {
        var context = provisional.context();
        return context.channel() == DamageChannel.ORDINARY && context.category() == AttackCategory.MELEE
                && context.attacker() instanceof Player player && player.getFoodData().getFoodLevel() >= HUNGER_COST;
    }

    public static void commit(Player player) {
        var food = player.getFoodData();
        if (food.getFoodLevel() < HUNGER_COST) return;
        food.setFoodLevel(food.getFoodLevel() - HUNGER_COST);
        player.heal(HEAL_HP);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
