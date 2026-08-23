package com.cosmicpve.trial.room;

import java.util.Locale;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum RainbowColor {
    RED(Items.RED_WOOL), ORANGE(Items.ORANGE_WOOL), YELLOW(Items.YELLOW_WOOL), LIME(Items.LIME_WOOL),
    GREEN(Items.GREEN_WOOL), CYAN(Items.CYAN_WOOL), LIGHT_BLUE(Items.LIGHT_BLUE_WOOL), BLUE(Items.BLUE_WOOL);
    private final Item wool;
    RainbowColor(Item wool) { this.wool = wool; }
    public Item wool() { return wool; }
    public String serialized() { return name().toLowerCase(Locale.ROOT); }
    public String display() {
        return java.util.Arrays.stream(name().toLowerCase(Locale.ROOT).split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1)).collect(java.util.stream.Collectors.joining(" "));
    }
    public static RainbowColor parse(String value) { return valueOf(value.toUpperCase(Locale.ROOT)); }
}
