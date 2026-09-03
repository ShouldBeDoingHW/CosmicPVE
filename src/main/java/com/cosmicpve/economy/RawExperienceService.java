package com.cosmicpve.economy;

import net.minecraft.server.level.ServerPlayer;

/** Server-authoritative exact vanilla experience-point accounting. */
public final class RawExperienceService {
    public int balance(ServerPlayer player) {
        return Math.max(0, player.totalExperience);
    }

    public boolean subtract(ServerPlayer player, int points) {
        int before = balance(player);
        if (points <= 0 || before < points) return false;
        player.giveExperiencePoints(-points);
        return player.totalExperience == before - points;
    }

    public void add(ServerPlayer player, int points) {
        if (points < 0 || points > Integer.MAX_VALUE - balance(player))
            throw new IllegalArgumentException("Experience addition is outside the valid range");
        player.giveExperiencePoints(points);
    }

    /** Restores one authoritative raw-XP total without firing mutable XP gain/loss events. */
    public void restore(ServerPlayer player, int totalPoints) {
        ExperienceState state = stateForTotal(totalPoints);
        player.totalExperience = totalPoints;
        player.setExperienceLevels(state.level());
        player.setExperiencePoints(state.pointsIntoLevel());
    }

    static ExperienceState stateForTotal(int totalPoints) {
        if (totalPoints < 0) throw new IllegalArgumentException("Experience cannot be negative");
        int level = 0;
        int remaining = totalPoints;
        int needed = pointsForNextLevel(level);
        while (remaining >= needed) {
            remaining -= needed;
            level++;
            needed = pointsForNextLevel(level);
        }
        return new ExperienceState(level, remaining);
    }

    private static int pointsForNextLevel(int level) {
        if (level >= 30) return 112 + (level - 30) * 9;
        return level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2;
    }

    record ExperienceState(int level, int pointsIntoLevel) {}
}
