package com.cosmicpve.combat.enchantment;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/** Exact target/source receipt for pre-death hooks that occur before the damage Post event. */
public final class DefensiveSuppressionContext {
    public static final DefensiveSuppressionContext GLOBAL = new DefensiveSuppressionContext();
    private final Map<LivingEntity, DamageSource> active = Collections.synchronizedMap(new WeakHashMap<>());
    private DefensiveSuppressionContext() {}
    public void mark(LivingEntity target, DamageSource source) { active.put(target, source); }
    public boolean active(LivingEntity target, DamageSource source) { return active.get(target) == source; }
    public void clear(LivingEntity target, DamageSource source) { active.remove(target, source); }
}
