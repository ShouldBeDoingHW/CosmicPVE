package com.cosmicpve.trial;

import java.util.List;

/** Canonical declaration, including intentionally inactive dependency-bound rows. */
public final class ImpossibleRewardCatalog {
    public static final List<Row> DECLARED = List.of(
            row("Heroic Crystal", 10, 1, true), row("Mystery Elite Spawner", 10, 2, true),
            row("Random Mask", 10, 1, true), row("Conquest Chest Flare", 10, 1, true),
            row("Skip 2 Room Trial Trinket", 8, 1, true), row("+66% Fame Trial Trinket", 8, 1, true),
            row("+3 Minute Trial Trinket", 8, 1, true), row("+2 Insurance Trial Trinket", 8, 1, true),
            row("Random V-Kit Unlock", 12, 1, true), row("25% Dragonslayer Crystal", 8, 1, true),
            row("40% Dragonslayer Crystal", 8, 1, true), row("Legendary Space Chest", 10, 1, true),
            row("Cosmic Enchantment Table", 5, 1, true), row("100% Black Scroll", 10, 1, true),
            row("50% Enchanted Black Scroll", 10, 1, true), row("60% Weapon Enchantment Orb", 10, 1, true),
            row("60% Armor Enchantment Orb", 10, 1, true), row("Abandoned Spaceship Portal", 8, 1, false),
            row("Mystery Call of Adventure",9,1,true));
    public static final int DECLARED_WEIGHT = DECLARED.stream().mapToInt(Row::weight).sum();
    public static final int ACTIVE_WEIGHT = DECLARED.stream().filter(Row::active).mapToInt(Row::weight).sum();
    private ImpossibleRewardCatalog() {}
    private static Row row(String name, int weight, int quantity, boolean active) {
        return new Row(name, weight, quantity, active);
    }
    public record Row(String name, int weight, int quantity, boolean active) {}
}
