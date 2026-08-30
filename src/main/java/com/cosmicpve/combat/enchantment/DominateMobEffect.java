package com.cosmicpve.combat.enchantment;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Harmful marker effect; damage behavior remains in the centralized Cosmic pipeline. */
public final class DominateMobEffect extends MobEffect {
    public DominateMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x7A3DB8);
    }
}
