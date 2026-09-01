package com.cosmicpve.combat.enchantment;

public final class DeepBleedBehavior {
    public static final int STACK_DURATION_TICKS = 140;
    private DeepBleedBehavior() {}
    public static double chance(int level) { return level <= 0 ? 0.0 : (6.0 + Math.min(6, level)) / 100.0; }
}
