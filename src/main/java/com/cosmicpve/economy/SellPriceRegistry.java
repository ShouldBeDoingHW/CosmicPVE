package com.cosmicpve.economy;

import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.OptionalLong;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Canonical exact-cent commodity prices in stable display order. */
public final class SellPriceRegistry {
    private static final Map<Item, Long> PRICES = create();
    private SellPriceRegistry() {}

    public static OptionalLong price(Item item) {
        Long value = PRICES.get(item);
        return value == null ? OptionalLong.empty() : OptionalLong.of(value);
    }
    public static Map<Item, Long> prices() { return PRICES; }

    private static Map<Item, Long> create() {
        var prices = new LinkedHashMap<Item, Long>();
        add(prices, Items.BEEF, 275); add(prices, Items.COOKED_BEEF, 475);
        add(prices, Items.ROTTEN_FLESH, 245); add(prices, Items.BONE, 275); add(prices, Items.BONE_BLOCK, 875);
        add(prices, Items.STRING, 275); add(prices, Items.POTATO, 200); add(prices, Items.CARROT, 200);
        add(prices, Items.IRON_INGOT, 950); add(prices, Items.IRON_BLOCK, 9_000); add(prices, Items.BLAZE_ROD, 1_500);
        add(prices, Items.POPPY, 250); add(prices, Items.COAL, 450); add(prices, Items.COAL_BLOCK, 4_500);
        add(prices, Items.WITHER_SKELETON_SKULL, 50_000);
        for (Item wool : new Item[]{Items.WHITE_WOOL, Items.ORANGE_WOOL, Items.MAGENTA_WOOL, Items.LIGHT_BLUE_WOOL,
                Items.YELLOW_WOOL, Items.LIME_WOOL, Items.PINK_WOOL, Items.GRAY_WOOL, Items.LIGHT_GRAY_WOOL,
                Items.CYAN_WOOL, Items.PURPLE_WOOL, Items.BLUE_WOOL, Items.BROWN_WOOL, Items.GREEN_WOOL,
                Items.RED_WOOL, Items.BLACK_WOOL}) add(prices, wool, 100);
        add(prices, Items.MUTTON, 315); add(prices, Items.LEATHER, 600); add(prices, Items.COOKED_MUTTON, 515);
        add(prices, Items.ENDER_PEARL, 750); add(prices, Items.PORKCHOP, 300); add(prices, Items.COOKED_PORKCHOP, 450);
        add(prices, Items.PRISMARINE_SHARD, 500); add(prices, Items.PRISMARINE_CRYSTALS, 600);
        add(prices, Items.COD, 500); add(prices, Items.COOKED_COD, 600); add(prices, Items.SALMON, 500);
        add(prices, Items.COOKED_SALMON, 600); add(prices, Items.PUFFERFISH, 1_500); add(prices, Items.TROPICAL_FISH, 2_500);
        add(prices, Items.GUNPOWDER, 815); add(prices, Items.SPIDER_EYE, 500); add(prices, Items.RED_MUSHROOM, 235);
        add(prices, Items.RED_MUSHROOM_BLOCK, 2_400); add(prices, Items.CHICKEN, 275); add(prices, Items.FEATHER, 215);
        add(prices, Items.COOKED_CHICKEN, 450); add(prices, Items.GOLD_INGOT, 1_250); add(prices, Items.GOLD_BLOCK, 12_000);
        add(prices, Items.SLIME_BALL, 500); add(prices, Items.SLIME_BLOCK, 4_800);
        return Collections.unmodifiableMap(new LinkedHashMap<>(prices));
    }
    private static void add(Map<Item, Long> prices, Item item, long cents) { prices.put(item, cents); }
}
