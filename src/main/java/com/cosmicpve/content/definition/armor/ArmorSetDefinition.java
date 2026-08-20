package com.cosmicpve.content.definition.armor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Immutable, validated armor-set content used by runtime resolvers. */
public record ArmorSetDefinition(
        Identifier id,
        Component displayName,
        int presentationColor,
        List<Component> fullSetBonus,
        double additiveOutgoingBonus,
        double incomingMultiplier,
        Map<Identifier, Double> procChanceMultipliers,
        Set<Identifier> immunities) {
    public ArmorSetDefinition {
        fullSetBonus = List.copyOf(fullSetBonus);
        procChanceMultipliers = Map.copyOf(procChanceMultipliers);
        immunities = Set.copyOf(immunities);
    }
}
