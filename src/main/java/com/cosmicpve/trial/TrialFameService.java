package com.cosmicpve.trial;

import net.minecraft.util.RandomSource;

/** Pure rules for active-phase Fame earning and one-time cashout bonuses. */
public final class TrialFameService {
    public long roll(TrialPhase phase, RandomSource random) {
        int minimum = switch (phase) { case APPRENTICE -> 1; case HARDCORE -> 4; case IMPOSSIBLE -> 8; case DEMONIC -> 12; };
        int maximum = switch (phase) { case APPRENTICE -> 3; case HARDCORE -> 7; case IMPOSSIBLE -> 11; case DEMONIC -> 15; };
        return random.nextIntBetweenInclusive(minimum, maximum);
    }
    public long cashout(long base, com.cosmicpve.data.component.TrialPortalModifiers modifiers) {
        return modifiers.cashoutFame(base);
    }
}
