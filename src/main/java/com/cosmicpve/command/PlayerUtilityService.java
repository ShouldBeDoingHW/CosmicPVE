package com.cosmicpve.command;

import net.minecraft.server.level.ServerPlayer;

public final class PlayerUtilityService {
    public static final int FULL_FOOD = 20;
    public static final float RESTORE_SATURATION = 1.5F;
    private PlayerUtilityService() {}

    public static void feed(ServerPlayer player) {
        player.getFoodData().setFoodLevel(FULL_FOOD);
        player.getFoodData().setSaturation(RESTORE_SATURATION);
    }

    public static void heal(ServerPlayer player) {
        // Ordinary restoration must use the same healing event as potions, regeneration and Cosmic heals.
        if (player.getHealth() > 0) player.heal(Math.max(0,player.getMaxHealth()-player.getHealth()));
        else player.setHealth(player.getMaxHealth()); // Preserve the operator's existing dead-player reset behavior.
    }

    public static void restore(ServerPlayer player) { heal(player); feed(player); }
}
