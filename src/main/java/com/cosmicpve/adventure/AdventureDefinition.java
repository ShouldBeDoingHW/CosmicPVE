package com.cosmicpve.adventure;

import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public record AdventureDefinition(String id, String name, Item icon, int color, List<String> lore) {
    public static final AdventureDefinition DENSE_WOODLANDS = new AdventureDefinition("dense_woodlands", "Dense Woodlands",
            Items.DARK_OAK_SAPLING, 0x43B03C, List.of(
            "Explore the Dense Woodlands and escape before your time runs out!",
            "Inside the Dense Woodlands, only Vanilla, Simple, Unique, and Elite enchantments work!",
            "Masks and item skins are disabled, and armorset bonuses do not apply!"));
    public static final AdventureDefinition FROZEN_WASTELAND = new AdventureDefinition("frozen_wasteland", "Frozen Wasteland",
            Items.PACKED_ICE, 0x97CEF7, List.of(
            "Explore the Frozen Wasteland and escape before your time runs out!",
            "Inside the Frozen Wasteland, Mastery and Heroic enchantments are disabled!",
            "Masks and item skins are disabled!"));
    public static final AdventureDefinition CORAL_WASTES = new AdventureDefinition("coral_wastes", "Coral Wastes",
            Items.TUBE_CORAL, 0x14A38F, List.of(
            "Explore the Coral Wastes and escape before your time runs out!",
            "Inside the Coral Wastes, everything is allowed!"));
    public static final List<AdventureDefinition> ALL = List.of(DENSE_WOODLANDS, FROZEN_WASTELAND, CORAL_WASTES);
}
