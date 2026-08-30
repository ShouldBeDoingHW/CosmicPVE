package com.cosmicpve.personalvault;

import com.cosmicpve.combat.api.CombatResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/** Persistent expiry-timestamp combat restriction for Personal Vault access. */
public final class PersonalVaultCombatTagService {
    public static final long DURATION_TICKS = 200L;
    private final PersonalVaultService vaults;

    public PersonalVaultCombatTagService(PersonalVaultService vaults) { this.vaults = vaults; }

    public void onCommitted(CombatResult result) {
        LivingEntity attacker = result.context().attacker();
        LivingEntity target = result.context().target();
        if (!qualifies(result.committedHealthDamage(), attacker, target)) return;
        if (attacker instanceof ServerPlayer player) refresh(player, canonicalTime(player));
        if (target instanceof ServerPlayer player) refresh(player, canonicalTime(player));
    }

    public void refresh(ServerPlayer player, long now) {
        long expiry = expiresAt(now);
        vaults.setCombatTagExpiry(player, expiry);
        PersonalVaultRuntime.access().closeIfOpen(player);
    }

    public boolean tagged(ServerPlayer player) { return tagged(player, canonicalTime(player)); }
    public boolean tagged(ServerPlayer player, long now) {
        return taggedAt(vaults.data(player).combatTagExpiresAt(), now);
    }
    public long remainingTicks(ServerPlayer player) {
        return Math.max(0L, vaults.data(player).combatTagExpiresAt() - canonicalTime(player));
    }
    public void clear(ServerPlayer player) { vaults.setCombatTagExpiry(player, 0L); }

    public static boolean qualifies(double committedDamage, LivingEntity attacker, LivingEntity target) {
        if (committedDamage <= 0.0 || attacker == null || target == null || attacker == target) return false;
        boolean attackerPlayer = attacker instanceof net.minecraft.world.entity.player.Player;
        boolean targetPlayer = target instanceof net.minecraft.world.entity.player.Player;
        boolean attackerCombatant = attackerPlayer || attacker instanceof net.minecraft.world.entity.Mob;
        boolean targetCombatant = targetPlayer || target instanceof net.minecraft.world.entity.Mob;
        return (attackerPlayer || targetPlayer) && attackerCombatant && targetCombatant;
    }

    public static long expiresAt(long now) {
        try { return Math.addExact(now, DURATION_TICKS); }
        catch (ArithmeticException overflow) { return Long.MAX_VALUE; }
    }

    public static boolean taggedAt(long expiry, long now) { return expiry > now; }

    private static long canonicalTime(ServerPlayer player) {
        return player.level().getServer().overworld().getGameTime();
    }
}
