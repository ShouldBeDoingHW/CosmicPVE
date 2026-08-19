package com.cosmicpve.registry;

import com.cosmicpve.CosmicPVE;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public final class ModEnchantments {
    private ModEnchantments() {}

    /**
     * Creates stable keys for Cosmic enchantments. Enchantment definitions are dynamic registry data and will be
     * introduced with the Step 2 content-definition work; Step 1 deliberately registers no gameplay enchantments.
     */
    public static ResourceKey<Enchantment> createKey(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, CosmicPVE.id(path));
    }
}
