package com.cosmicpve.trial;

import java.util.List;

/** Current canonical declaration, including the intentionally inactive Spaceship portal row. */
public final class ImpossibleRewardCatalog {
    public static final List<Row> DECLARED = List.of(
            row("Heroic Crystal",10,1,true),
            row("Mystery Elite Spawner",10,2,true),
            row("Random Mask",10,1,true),
            row("Conquest Chest Flare",10,1,true),
            row("Random Tier 2 Trial Trinket",15,1,true),
            row("Unexamined Mastery Enchantment Book",5,2,true),
            row("Random V-Kit Crystal",12,1,true),
            row("25% Dragonslayer Crystal",8,1,true),
            row("40% Dragonslayer Crystal",8,1,true),
            row("Legendary Space Chest",10,1,true),
            row("Cosmic Enchantment Table",5,1,true),
            row("100% Black Scroll",10,1,true),
            row("50% Enchanted Black Scroll",10,1,true),
            row("60% Weapon Enchantment Orb",10,1,true),
            row("60% Armor Enchantment Orb",10,1,true),
            row("Abandoned Spaceship Portal",8,1,false),
            row("Mystery Call of Adventure",9,1,true),
            row("25% Amulet Socket",6,1,true),
            row("25% Belt Socket",6,1,true),
            row("Random Weapon Skin Generator",3,1,true),
            row("Random Tier 3 Trial Trinket",8,1,true));
    public static final int DECLARED_WEIGHT = DECLARED.stream().mapToInt(Row::weight).sum();
    public static final int ACTIVE_WEIGHT = DECLARED.stream().filter(Row::active).mapToInt(Row::weight).sum();
    private ImpossibleRewardCatalog() {}
    private static Row row(String name, int weight, int quantity, boolean active) { return new Row(name, weight, quantity, active); }
    public record Row(String name, int weight, int quantity, boolean active) {}
}
