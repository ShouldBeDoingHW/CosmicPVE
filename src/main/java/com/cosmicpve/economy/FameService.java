package com.cosmicpve.economy;

import com.cosmicpve.data.attachment.PlayerProfileData;
import com.cosmicpve.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;

/** Sole server-side authority for persistent, non-negative integer Fame. */
public final class FameService {
    public static final int COLOR = 0xF4FF4A;
    public long balance(ServerPlayer player) { return player.getData(ModAttachments.PLAYER_PROFILE).fame(); }
    public boolean set(ServerPlayer player, long fame) {
        if (fame < 0) return false;
        PlayerProfileData old = player.getData(ModAttachments.PLAYER_PROFILE);
        player.setData(ModAttachments.PLAYER_PROFILE, new PlayerProfileData(old.dataVersion(), old.moneyCents(), fame));
        return true;
    }
    public boolean add(ServerPlayer player, long amount) {
        if (amount <= 0) return false;
        try { return set(player, Math.addExact(balance(player), amount)); }
        catch (ArithmeticException ignored) { return false; }
    }
    public static boolean canAdd(long balance, long amount) {
        if (balance < 0 || amount <= 0) return false;
        try { Math.addExact(balance, amount); return true; }
        catch (ArithmeticException ignored) { return false; }
    }
    public boolean subtract(ServerPlayer player, long amount) {
        long current = balance(player);
        return amount > 0 && amount <= current && set(player, current - amount);
    }
}
