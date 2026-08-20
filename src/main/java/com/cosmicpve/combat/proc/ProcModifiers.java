package com.cosmicpve.combat.proc;

import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;

/** Generic equipment/event modifiers applied centrally to proc chance and cooldown duration. */
public record ProcModifiers(
        List<Double> chanceMultipliers,
        List<Double> cooldownDurationMultipliers,
        Map<Identifier, Double> namedChanceMultipliers) {
    public static final ProcModifiers NONE = new ProcModifiers(List.of(1.0), List.of(1.0), Map.of());

    public ProcModifiers {
        chanceMultipliers = List.copyOf(chanceMultipliers);
        cooldownDurationMultipliers = List.copyOf(cooldownDurationMultipliers);
        namedChanceMultipliers = Map.copyOf(namedChanceMultipliers);
    }

    public ProcModifiers(List<Double> chanceMultipliers, List<Double> cooldownDurationMultipliers) {
        this(chanceMultipliers, cooldownDurationMultipliers, Map.of());
    }
}
