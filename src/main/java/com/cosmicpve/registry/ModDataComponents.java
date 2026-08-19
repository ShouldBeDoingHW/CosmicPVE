package com.cosmicpve.registry;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.component.CustomEnchantMetadata;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CosmicPVE.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomEnchantMetadata>> CUSTOM_ENCHANT_META =
            COMPONENTS.registerComponentType(
                    "custom_enchant_meta",
                    builder -> builder.persistent(CustomEnchantMetadata.CODEC).cacheEncoding());

    private ModDataComponents() {}

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
