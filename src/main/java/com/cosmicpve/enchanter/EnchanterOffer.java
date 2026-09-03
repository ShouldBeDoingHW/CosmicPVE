package com.cosmicpve.enchanter;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public record EnchanterOffer(int slot, CosmicEnchantmentTier tier, int cost, Item icon, int color) {
    public static final List<EnchanterOffer> ALL = List.of(
            new EnchanterOffer(0, CosmicEnchantmentTier.SIMPLE, 400, Items.WHITE_STAINED_GLASS_PANE, 0xFFFFFF),
            new EnchanterOffer(2, CosmicEnchantmentTier.UNIQUE, 800, Items.LIME_STAINED_GLASS_PANE, 0x55FF55),
            new EnchanterOffer(4, CosmicEnchantmentTier.ELITE, 1_500, Items.CYAN_STAINED_GLASS_PANE, 0xA3FFF5),
            new EnchanterOffer(6, CosmicEnchantmentTier.ULTIMATE, 2_500, Items.YELLOW_STAINED_GLASS_PANE, 0xFFFF55),
            new EnchanterOffer(8, CosmicEnchantmentTier.LEGENDARY, 4_000, Items.ORANGE_STAINED_GLASS_PANE, 0xFFAA00));

    public static EnchanterOffer at(int slot) {
        return ALL.stream().filter(offer -> offer.slot == slot).findFirst().orElse(null);
    }
}
