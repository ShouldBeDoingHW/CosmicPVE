package com.cosmicpve.combat.enchantment;

public final class ImplantsBehavior {
    public static final float HEAL_AMOUNT = 1.0F;
    private ImplantsBehavior() {}
    public static int intervalTicks(int level) {
        if (level < 1 || level > 3) throw new IllegalArgumentException("Implants level must be I-III");
        return 100 - 15 * level;
    }
    public static int nextHealTick(int currentTick, int level) { return currentTick + intervalTicks(level); }
}
