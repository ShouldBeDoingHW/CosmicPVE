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
import com.cosmicpve.data.component.SpaceChestData;
import com.cosmicpve.data.component.TrialTrinketData;
import com.cosmicpve.data.component.TrialPortalModifiers;
import com.cosmicpve.data.component.MaskLoadout;
import com.cosmicpve.data.component.HiddenGraveyardKeyData;
import com.cosmicpve.data.component.CosmicDustData;
import com.cosmicpve.data.component.MysterySpawnerData;
import com.cosmicpve.data.component.VKitCrystalData;
import com.cosmicpve.data.component.VKitEquipmentData;
import com.cosmicpve.data.component.StoredXpBottleData;
import com.cosmicpve.data.component.EnchantedBlackScrollData;
import com.cosmicpve.data.component.SignatureWeaponIdentity;
import com.cosmicpve.data.component.AdminAbuseRewardIdentity;
import com.cosmicpve.data.component.CosmicBookRateOverride;
import com.cosmicpve.data.component.CallOfForestData;
import com.cosmicpve.data.component.AccessoryLoadout;
import com.cosmicpve.data.component.AccessorySocketData;
import com.cosmicpve.data.component.AccessoryItemData;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CosmicPVE.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AccessoryLoadout>> ACCESSORY_LOADOUT =
            COMPONENTS.registerComponentType("accessory_loadout", builder -> builder.persistent(AccessoryLoadout.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(AccessoryLoadout.CODEC)).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AccessorySocketData>> ACCESSORY_SOCKET =
            COMPONENTS.registerComponentType("accessory_socket", builder -> builder.persistent(AccessorySocketData.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(AccessorySocketData.CODEC)).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AccessoryItemData>> ACCESSORY_ITEM =
            COMPONENTS.registerComponentType("accessory_item", builder -> builder.persistent(AccessoryItemData.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(AccessoryItemData.CODEC)).cacheEncoding());

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

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CosmicDustData>> COSMIC_DUST =
            COMPONENTS.registerComponentType("cosmic_dust",
                    builder -> builder.persistent(CosmicDustData.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(CosmicDustData.CODEC)).cacheEncoding());

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

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SpaceChestData>> SPACE_CHEST =
            COMPONENTS.registerComponentType("space_chest",
                    builder -> builder.persistent(SpaceChestData.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(SpaceChestData.CODEC)).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TrialTrinketData>> TRIAL_TRINKET =
            COMPONENTS.registerComponentType("trial_trinket",
                    builder -> builder.persistent(TrialTrinketData.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(TrialTrinketData.CODEC)).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TrialPortalModifiers>> TRIAL_PORTAL_MODIFIERS =
            COMPONENTS.registerComponentType("trial_portal_modifiers",
                    builder -> builder.persistent(TrialPortalModifiers.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(TrialPortalModifiers.CODEC)).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MaskLoadout>> MASK_ITEM =
            COMPONENTS.registerComponentType("mask_item", builder -> builder.persistent(MaskLoadout.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(MaskLoadout.CODEC)).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MaskLoadout>> MASK_LOADOUT =
            COMPONENTS.registerComponentType("mask_loadout", builder -> builder.persistent(MaskLoadout.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(MaskLoadout.CODEC)).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<HiddenGraveyardKeyData>> HIDDEN_GRAVEYARD_KEY =
            COMPONENTS.registerComponentType("hidden_graveyard_key",
                    builder -> builder.persistent(HiddenGraveyardKeyData.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(HiddenGraveyardKeyData.CODEC)).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> OMNI_ARMOR =
            COMPONENTS.registerComponentType("omni_armor",
                    builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> HOLY =
            COMPONENTS.registerComponentType("holy",
                    builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MysterySpawnerData>> MYSTERY_SPAWNER =
            COMPONENTS.registerComponentType("mystery_spawner",
                    builder -> builder.persistent(MysterySpawnerData.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(MysterySpawnerData.CODEC)).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<VKitCrystalData>> VKIT_CRYSTAL =
            COMPONENTS.registerComponentType("vkit_crystal",
                    builder -> builder.persistent(VKitCrystalData.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(VKitCrystalData.CODEC)).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<VKitEquipmentData>> VKIT_EQUIPMENT =
            COMPONENTS.registerComponentType("vkit_equipment",
                    builder -> builder.persistent(VKitEquipmentData.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(VKitEquipmentData.CODEC)).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<StoredXpBottleData>> STORED_XP_BOTTLE =
            COMPONENTS.registerComponentType("stored_xp_bottle",
                    builder -> builder.persistent(StoredXpBottleData.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(StoredXpBottleData.CODEC)).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EnchantedBlackScrollData>> ENCHANTED_BLACK_SCROLL =
            COMPONENTS.registerComponentType("enchanted_black_scroll",
                    builder -> builder.persistent(EnchantedBlackScrollData.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(EnchantedBlackScrollData.CODEC)).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SignatureWeaponIdentity>> SIGNATURE_WEAPON =
            COMPONENTS.registerComponentType("signature_weapon",
                    builder -> builder.persistent(SignatureWeaponIdentity.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(SignatureWeaponIdentity.CODEC)).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AdminAbuseRewardIdentity>> ADMIN_ABUSE_REWARD =
            COMPONENTS.registerComponentType("admin_abuse_reward",
                    builder -> builder.persistent(AdminAbuseRewardIdentity.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(AdminAbuseRewardIdentity.CODEC)).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CosmicBookRateOverride>> COSMIC_BOOK_RATE_OVERRIDE =
            COMPONENTS.registerComponentType("cosmic_book_rate_override",
                    builder -> builder.persistent(CosmicBookRateOverride.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(CosmicBookRateOverride.CODEC)).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CallOfForestData>> CALL_OF_FOREST =
            COMPONENTS.registerComponentType("call_of_forest", builder -> builder.persistent(CallOfForestData.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(CallOfForestData.CODEC)).cacheEncoding());
    private ModDataComponents() {}

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
