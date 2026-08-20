package com.cosmicpve.registry;

import com.cosmicpve.CosmicPVE;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public final class ModEnchantments {
    public static final ResourceKey<Enchantment> EXECUTE = createKey("execute");
    public static final ResourceKey<Enchantment> ANGELIC = createKey("angelic");
    public static final ResourceKey<Enchantment> LIGHTNING = createKey("lightning");
    public static final ResourceKey<Enchantment> ENDER_SHIFT = createKey("ender_shift");
    public static final ResourceKey<Enchantment> DOUBLESTRIKE = createKey("doublestrike");
    public static final ResourceKey<Enchantment> BLEED = createKey("bleed");
    public static final ResourceKey<Enchantment> LUCK = createKey("luck");
    public static final ResourceKey<Enchantment> POISON = createKey("poison");
    public static final ResourceKey<Enchantment> PUMMEL = createKey("pummel");

    private ModEnchantments() {}

    public static ResourceKey<Enchantment> createKey(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, CosmicPVE.id(path));
    }
}
