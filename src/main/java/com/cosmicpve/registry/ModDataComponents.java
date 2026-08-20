package com.cosmicpve.registry;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.component.CustomEnchantMetadata;
import com.cosmicpve.data.component.ArmorSetIdentity;
import com.cosmicpve.data.component.ArmorSetCrystalData;
import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import com.cosmicpve.data.component.EnchantmentOrbData;
import com.cosmicpve.data.component.BlackScrollData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CosmicPVE.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomEnchantMetadata>> CUSTOM_ENCHANT_META =
            COMPONENTS.registerComponentType(
                    "custom_enchant_meta",
                    builder -> builder.persistent(CustomEnchantMetadata.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(CustomEnchantMetadata.CODEC)).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ArmorSetIdentity>> ARMOR_SET_ID =
            COMPONENTS.registerComponentType("armor_set_id",
                    builder -> builder.persistent(ArmorSetIdentity.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(ArmorSetIdentity.CODEC)).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ArmorSetCrystalData>> ARMOR_SET_CRYSTAL =
            COMPONENTS.registerComponentType("armor_set_crystal",
                    builder -> builder.persistent(ArmorSetCrystalData.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(ArmorSetCrystalData.CODEC)).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CosmicEnchantmentBookData>> COSMIC_ENCHANT_BOOK =
            COMPONENTS.registerComponentType("cosmic_enchant_book",
                    builder -> builder.persistent(CosmicEnchantmentBookData.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(CosmicEnchantmentBookData.CODEC)).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EnchantmentOrbData>> ENCHANTMENT_ORB =
            COMPONENTS.registerComponentType("enchantment_orb",
                    builder -> builder.persistent(EnchantmentOrbData.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(EnchantmentOrbData.CODEC)).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlackScrollData>> BLACK_SCROLL =
            COMPONENTS.registerComponentType("black_scroll",
                    builder -> builder.persistent(BlackScrollData.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(BlackScrollData.CODEC)).cacheEncoding());

    private ModDataComponents() {}

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
