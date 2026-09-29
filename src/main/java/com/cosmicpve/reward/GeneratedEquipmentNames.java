package com.cosmicpve.reward;

import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Small type-aware grammar; names are rolled once when the final reward stack is created. */
public final class GeneratedEquipmentNames {
    private static final List<String> ADJECTIVES = List.of("Ancient", "Burning", "Cursed", "Dreadful",
            "Eternal", "Fatal", "Forgotten", "Frozen", "Gilded", "Grim", "Hollow", "Infernal",
            "Jagged", "Keen", "Merciless", "Obsidian", "Phantom", "Ruthless", "Savage", "Shadowed",
            "Shattered", "Silent", "Studded", "Totalitarian", "Venomous", "Vengeful", "Winged", "Wicked");
    private static final List<String> SUFFIXES = List.of("of Doom", "of Ares", "of Infamy", "of Ruin",
            "of the Void", "of Fury", "of Ash", "of War", "of Night", "of Dominion", "of Vengeance",
            "of Desolation");

    private GeneratedEquipmentNames() {}

    public static String roll(Item item, RandomSource random) {
        List<String> nouns = nouns(item);
        String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
        String noun = nouns.get(random.nextInt(nouns.size()));
        return adjective + " " + noun + (random.nextBoolean()
                ? " " + SUFFIXES.get(random.nextInt(SUFFIXES.size())) : "");
    }

    public static List<String> nouns(Item item) {
        if (item == Items.IRON_HELMET) return List.of("Helmet", "Helm", "Headpiece", "Crown", "Visor");
        if (item == Items.IRON_CHESTPLATE) return List.of("Chestplate", "Cuirass", "Plate", "Carapace");
        if (item == Items.IRON_LEGGINGS) return List.of("Leggings", "Greaves", "Legguards");
        if (item == Items.IRON_BOOTS) return List.of("Boots", "Treads", "Sabatons");
        if (item == Items.DIAMOND_SWORD) return List.of("Sword", "Blade", "Saber");
        if (item == Items.DIAMOND_AXE) return List.of("Axe", "Hatchet", "Cleaver");
        if (item == Items.BOW) return List.of("Bow", "Longbow");
        if (item == Items.CROSSBOW) return List.of("Crossbow", "Arbalest");
        throw new IllegalArgumentException("Not generated Space Chest equipment: " + item);
    }
}
