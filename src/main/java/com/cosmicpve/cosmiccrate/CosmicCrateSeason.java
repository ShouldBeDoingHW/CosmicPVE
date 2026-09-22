package com.cosmicpve.cosmiccrate;

import java.util.List;

public enum CosmicCrateSeason {
    SPRING("spring", "Spring", 0x72B545,
            List.of("1-3 Unexamined Heroic Enchantment Book", "3-5 Whitescrolls",
                    "1-4 Mystery Elite Spawner OR Mystery Mastery Spawner", "1-3 Trial Portals",
                    "100% Belt OR Amulet Socket", "Easter Basket OR Chocolate Egg Custom Block",
                    "1-3x Abandoned Spaceship Portal", "1-3x Vkit Unlock Gems",
                    "Party Blade Sword Skin OR Luck of the Irish Axe Skin"),
            List.of("Admin Abuse", "Dungeon Key Ring", "Secret Weapon Cache", "2x Heroic Abandoned Spaceship Portal")),
    SUMMER("summer", "Summer", 0xF0CA60,
            List.of("1-2 100% Blackscroll OR Enchanted Blackscroll", "1-5 Whitescrolls",
                    "1-3 Mystery Elite Spawner OR Mystery Mastery Spawner", "3-6 Trial Portals",
                    "Random Tier 3 Trial Trinket", "Sand Castle OR Potted Cactus Custom Block",
                    "2x Abandoned Spaceship Portal", "50% Blackout Mastery Enchantment Book",
                    "Jumbo Popsicle Sword Skin OR Spiked Baseball Bat Axe Skin"),
            List.of("Godly Vkit Bundle", "Memory Chest x2", "Secret Weapon Cache", "Maxed out Trial Portal")),
    FALL("fall", "Fall", 0xBA8807,
            List.of("1-3 Unexamined Heroic Enchantment Book", "1-5 Whitescrolls",
                    "3-5 Mystery Elite Spawner OR Mystery Mastery Spawner", "2-4 Trial Portals",
                    "85% Belt OR Amulet Socket", "Jack o’Lantern OR Cursed Skull Custom Block",
                    "1-3x Abandoned Spaceship Portal", "Triple Mask: Scarecrow + Turkey + ????",
                    "Spinal Tap Sword Skin OR Grim Axe Skin"),
            List.of("Admin Abuse", "2x Memory Chest", "Secret Weapon Cache", "2x Heroic Abandoned Spaceship Portal")),
    WINTER("winter", "Winter", 0x5ED1D6,
            List.of("1-2 100% Blackscroll OR Enchanted Blackscroll", "1-2 Call of the Blizzard (30)",
                    "1-3 Mystery Elite Spawner OR Mystery Mastery Spawner", "2-4 Trial Portal",
                    "2x Random Tier 3 Trial Trinket", "Snowglobe OR Milk and Cookies custom block",
                    "1-5 Whitescrolls", "50% Permafrost VI Mastery Enchantment Book",
                    "Ornamental Carnage Sword Skin OR Icicle Hatchet Axe Skin"),
            List.of("Dungeon Key Ring", "Memory Chest x2", "Secret Weapon Cache", "Maxed out Trial Portal"));

    private final String id;
    private final String displayName;
    private final int color;
    private final List<String> treasure;
    private final List<String> bonus;
    CosmicCrateSeason(String id, String displayName, int color, List<String> treasure, List<String> bonus) {
        this.id = id; this.displayName = displayName; this.color = color;
        this.treasure = List.copyOf(treasure); this.bonus = List.copyOf(bonus);
        if (this.treasure.size() != 9 || this.bonus.size() != 4) throw new IllegalArgumentException("Seasonal lore size");
    }
    public String id() { return id; }
    public String displayName() { return displayName; }
    public int color() { return color; }
    public List<String> treasure() { return treasure; }
    public List<String> bonus() { return bonus; }
}
