package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

public final class PhoenixBehavior {
    public static final Identifier COOLDOWN_KEY = CosmicPVE.id("phoenix");
    public static final long COOLDOWN_TICKS = 1_800L;
    public static final double SURVIVAL_HEALTH_FRACTION = 0.40;

    private PhoenixBehavior() {}

    public static float survivalHealth(float maximumHealth) {
        return (float) (Math.max(0.0F, maximumHealth) * SURVIVAL_HEALTH_FRACTION);
    }

    public static void activate(LivingEntity wearer) {
        if (wearer != null) wearer.setHealth(survivalHealth(wearer.getMaxHealth()));
    }
}
