package com.cosmicpve.combat.proc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.world.entity.LivingEntity;

/** Composes proc modifiers from enchantments now and other stable equipment/status sources later. */
public final class ProcModifierSourceRegistry implements ProcModifierResolver {
    private final List<ProcModifierResolver> sources = new CopyOnWriteArrayList<>();

    public void register(ProcModifierResolver source) {
        sources.add(source);
    }

    @Override
    public ProcModifiers resolve(LivingEntity owner) {
        var chance = new ArrayList<Double>();
        var cooldown = new ArrayList<Double>();
        var named = new java.util.HashMap<net.minecraft.resources.Identifier, Double>();
        for (var source : sources) {
            var modifiers = source.resolve(owner);
            chance.addAll(modifiers.chanceMultipliers());
            cooldown.addAll(modifiers.cooldownDurationMultipliers());
            modifiers.namedChanceMultipliers().forEach((id, multiplier) -> named.merge(id, multiplier, (a, b) -> a * b));
        }
        return new ProcModifiers(
                chance.isEmpty() ? List.of(1.0) : chance,
                cooldown.isEmpty() ? List.of(1.0) : cooldown,
                named);
    }
}
