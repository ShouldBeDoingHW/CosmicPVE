package com.cosmicpve.economy;

import com.cosmicpve.data.attachment.PlayerProfileData;
import com.cosmicpve.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;

/** Sole server-side authority for persistent player currency. */
public final class MoneyService {
    public long balance(ServerPlayer player) { return player.getData(ModAttachments.PLAYER_PROFILE).moneyCents(); }

    public boolean set(ServerPlayer player, long cents) {
        if (cents < 0) return false;
        var old = player.getData(ModAttachments.PLAYER_PROFILE);
        player.setData(ModAttachments.PLAYER_PROFILE, new PlayerProfileData(old.dataVersion(), cents, old.fame()));
        return true;
    }

    public boolean add(ServerPlayer player, long cents) {
        if (cents <= 0) return false;
        try { return set(player, Math.addExact(balance(player), cents)); }
        catch (ArithmeticException overflow) { return false; }
    }

    public boolean subtract(ServerPlayer player, long cents) {
        long current = balance(player);
        if (cents <= 0 || cents > current) return false;
        return set(player, current - cents);
    }

    public static boolean canAdd(long balance, long cents) {
        if (balance < 0 || cents <= 0) return false;
        try { Math.addExact(balance, cents); return true; }
        catch (ArithmeticException overflow) { return false; }
    }
}
