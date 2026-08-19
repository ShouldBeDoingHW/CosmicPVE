package com.cosmicpve.registry;

import com.cosmicpve.CosmicPVE;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CosmicPVE.MOD_ID);

    public static final DeferredItem<Item> FOUNDATION_TOKEN = ITEMS.registerSimpleItem(
            "foundation_token",
            properties -> properties.stacksTo(1));

    private ModItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
