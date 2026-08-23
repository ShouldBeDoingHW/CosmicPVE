package com.cosmicpve.registry;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.component.CustomEnchantMetadata;
import com.cosmicpve.data.component.ArmorSetIdentity;
import com.cosmicpve.data.component.ArmorSetCrystalData;
import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import com.cosmicpve.data.component.EnchantmentOrbData;
import com.cosmicpve.data.component.BlackScrollData;
import com.cosmicpve.data.component.WeaponSkinIdentity;
import com.cosmicpve.data.component.WeaponSkinItemData;
import com.cosmicpve.data.component.HeroicIdentity;
import com.cosmicpve.data.component.UnexaminedBookData;
import com.cosmicpve.data.component.BanknoteData;
import com.cosmicpve.data.component.MobSpawnerData;
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

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UnexaminedBookData>> UNEXAMINED_BOOK =
            COMPONENTS.registerComponentType("unexamined_book",
                    builder -> builder.persistent(UnexaminedBookData.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(UnexaminedBookData.CODEC)).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EnchantmentOrbData>> ENCHANTMENT_ORB =
            COMPONENTS.registerComponentType("enchantment_orb",
                    builder -> builder.persistent(EnchantmentOrbData.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(EnchantmentOrbData.CODEC)).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlackScrollData>> BLACK_SCROLL =
            COMPONENTS.registerComponentType("black_scroll",
                    builder -> builder.persistent(BlackScrollData.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(BlackScrollData.CODEC)).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WeaponSkinIdentity>> WEAPON_SKIN =
            COMPONENTS.registerComponentType("weapon_skin",
                    builder -> builder.persistent(WeaponSkinIdentity.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(WeaponSkinIdentity.CODEC)).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WeaponSkinItemData>> WEAPON_SKIN_ITEM =
            COMPONENTS.registerComponentType("weapon_skin_item",
                    builder -> builder.persistent(WeaponSkinItemData.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(WeaponSkinItemData.CODEC)).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<HeroicIdentity>> HEROIC =
            COMPONENTS.registerComponentType("heroic",
                    builder -> builder.persistent(HeroicIdentity.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(HeroicIdentity.CODEC)).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BanknoteData>> BANKNOTE =
            COMPONENTS.registerComponentType("banknote",
                    builder -> builder.persistent(BanknoteData.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(BanknoteData.CODEC)).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MobSpawnerData>> MOB_SPAWNER =
            COMPONENTS.registerComponentType("mob_spawner",
                    builder -> builder.persistent(MobSpawnerData.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(MobSpawnerData.CODEC)).cacheEncoding());

    private ModDataComponents() {}

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
