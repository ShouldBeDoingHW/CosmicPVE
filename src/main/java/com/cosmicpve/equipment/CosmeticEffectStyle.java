package com.cosmicpve.equipment;

import net.minecraft.network.chat.Component;

/** Shared presentation for gameplay effects on equippable cosmetics. */
public final class CosmeticEffectStyle {
    public static final int COLOR = 0xFF5555;

    private CosmeticEffectStyle() {}

    public static Component apply(Component description) {
        return description.copy().withColor(COLOR);
    }
}
