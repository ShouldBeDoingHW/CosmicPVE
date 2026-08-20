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

    public static void heal(ServerPlayer player) { player.setHealth(player.getMaxHealth()); }

    public static void restore(ServerPlayer player) { heal(player); feed(player); }
}
