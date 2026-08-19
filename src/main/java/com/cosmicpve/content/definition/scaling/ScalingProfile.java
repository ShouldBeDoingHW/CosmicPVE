package com.cosmicpve.content.definition.scaling;

import net.minecraft.resources.Identifier;

public record ScalingProfile(
        Identifier id,
        double onePlayer,
        double twoPlayers,
        double threePlayers,
        double fourPlayers) {
    public double valueForPartySize(int partySize) {
        return switch (partySize) {
            case 1 -> onePlayer;
            case 2 -> twoPlayers;
            case 3 -> threePlayers;
            case 4 -> fourPlayers;
            default -> throw new IllegalArgumentException("Party size must be between 1 and 4: " + partySize);
        };
    }
}
