package com.cosmicpve.trial.room;

import java.util.Locale;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum CircuitMaterial {
    REDSTONE(Blocks.REDSTONE_BLOCK, Blocks.RED_STAINED_GLASS),
    GOLD(Blocks.GOLD_BLOCK, Blocks.YELLOW_STAINED_GLASS),
    EMERALD(Blocks.EMERALD_BLOCK, Blocks.GREEN_STAINED_GLASS),
    DIAMOND(Blocks.DIAMOND_BLOCK, Blocks.BLUE_STAINED_GLASS);
    private final Block pillar, glass;
    CircuitMaterial(Block pillar, Block glass) { this.pillar = pillar; this.glass = glass; }
    public Block pillar() { return pillar; }
    public Block glass() { return glass; }
    public Item glassItem() { return glass.asItem(); }
    public String serialized() { return name().toLowerCase(Locale.ROOT); }
    public static CircuitMaterial parse(String value) { return valueOf(value.toUpperCase(Locale.ROOT)); }
    public static CircuitMaterial fromGlass(Block block) {
        for (var value : values()) if (value.glass == block) return value;
        return null;
    }
}
