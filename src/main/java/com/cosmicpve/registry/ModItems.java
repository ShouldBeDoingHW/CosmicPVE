package com.cosmicpve.registry;

import com.cosmicpve.CosmicPVE;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.cosmicpve.equipment.armor.ArmorSetCrystalItem;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentBookItem;
import com.cosmicpve.equipment.enchantment.EnchantmentOrbItem;
import com.cosmicpve.equipment.enchantment.BlackScrollItem;
import com.cosmicpve.equipment.enchantment.WhiteScrollItem;
import com.cosmicpve.equipment.skin.WeaponSkinItem;
import com.cosmicpve.equipment.heroic.HeroicCrystalItem;
import com.cosmicpve.equipment.enchantment.UnexaminedEnchantmentBookItem;
import com.cosmicpve.economy.BanknoteItem;
import com.cosmicpve.reward.spawner.TypedMobSpawnerItem;
import com.cosmicpve.spacechest.SpaceChestItem;
import com.cosmicpve.trial.portal.TrialPortalItem;
import com.cosmicpve.trial.trinket.TrialTrinketItem;
import com.cosmicpve.data.component.TrialTrinketData;
import com.cosmicpve.data.component.TrialTrinketType;
import com.cosmicpve.equipment.mask.MaskItem;
import com.cosmicpve.equipment.mask.MaskSplicerItem;
import com.cosmicpve.conquest.ConquestFlareItem;
import com.cosmicpve.equipment.enchantment.CosmicDustItem;
import com.cosmicpve.reward.spawner.MysterySpawnerItem;
import com.cosmicpve.data.component.MysterySpawnerData;
import com.cosmicpve.data.component.MysterySpawnerTier;
import com.cosmicpve.item.BoldNameItem;
import com.cosmicpve.vkit.VKitCrystalItem;
import com.cosmicpve.vkit.VKitDefinition;
import com.cosmicpve.data.component.VKitCrystalData;
import com.cosmicpve.personalvault.PersonalVaultUnlockItem;
import com.cosmicpve.vkit.GodlyVKitBundleItem;
import com.cosmicpve.tinkerer.SalvagedXpBottleItem;
import com.cosmicpve.equipment.enchantment.EnchantedBlackScrollItem;
import com.cosmicpve.reward.lootbox.AnimatedLootboxItem;
import com.cosmicpve.upgrade.UpgradeCrystalItem;
import com.cosmicpve.equipment.enchantment.HolyWhiteScrollItem;
import com.cosmicpve.reward.lootbox.SpaceDustBundleItem;
import com.cosmicpve.adventure.CallOfForestItem;
import com.cosmicpve.adventure.DenseWoodlandsScrapItem;
import com.cosmicpve.adventure.AdventureCompassItem;
import com.cosmicpve.data.component.CallOfForestData;
import com.cosmicpve.data.component.AccessoryItemData;
import com.cosmicpve.data.component.AccessorySocketData;
import com.cosmicpve.data.component.AccessorySlot;
import com.cosmicpve.equipment.accessory.AmuletDefinition;
import com.cosmicpve.equipment.accessory.AmuletItem;
import com.cosmicpve.equipment.accessory.AccessorySocketItem;
import com.cosmicpve.equipment.accessory.BeltDefinition;
import com.cosmicpve.equipment.accessory.BeltItem;

public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CosmicPVE.MOD_ID);

    public static final DeferredItem<AccessorySocketItem> AMULET_SOCKET = ITEMS.registerItem("amulet_socket",
            AccessorySocketItem::new, properties -> properties.stacksTo(64)
                    .component(ModDataComponents.ACCESSORY_SOCKET.get(),
                            new AccessorySocketData(AccessorySocketData.DATA_VERSION, AccessorySlot.AMULET, 100)));
    public static final DeferredItem<AccessorySocketItem> BELT_SOCKET = ITEMS.registerItem("belt_socket",
            AccessorySocketItem::new, properties -> properties.stacksTo(64)
                    .component(ModDataComponents.ACCESSORY_SOCKET.get(),
                            new AccessorySocketData(AccessorySocketData.DATA_VERSION, AccessorySlot.BELT, 100)));
    public static final DeferredItem<AccessorySocketItem> OMNI_SOCKET = ITEMS.registerItem("omni_socket",
            AccessorySocketItem::new, properties -> properties.stacksTo(64)
                    .component(ModDataComponents.OMNI_SOCKET_SUCCESS.get(), 100));
    public static final DeferredItem<AmuletItem> BLOOD_DIAMOND_AMULET = amulet("blood_diamond_amulet",
            AmuletDefinition.BLOOD_DIAMOND);
    public static final DeferredItem<AmuletItem> ICICLE_AMULET = amulet("icicle_amulet", AmuletDefinition.ICICLE);
    public static final DeferredItem<AmuletItem> BLACK_HEART_AMULET = amulet("black_heart_amulet",
            AmuletDefinition.BLACK_HEART);
    public static final DeferredItem<BeltItem> SHOCK_THERAPY_BELT = belt("shock_therapy_belt", BeltDefinition.SHOCK_THERAPY);
    public static final DeferredItem<BeltItem> BANDOLIER_BELT = belt("bandolier_belt", BeltDefinition.BANDOLIER);
    public static final DeferredItem<BeltItem> JELLY_ROLL_BELT = belt("jelly_roll_belt", BeltDefinition.JELLY_ROLL);

    public static final DeferredItem<Item> FOUNDATION_TOKEN = ITEMS.registerSimpleItem(
            "foundation_token",
            properties -> properties.stacksTo(1));

    public static final DeferredItem<UpgradeCrystalItem> UPGRADE_CRYSTAL = ITEMS.registerItem(
            "upgrade_crystal", UpgradeCrystalItem::new, properties -> properties.stacksTo(64));

    public static final DeferredItem<ArmorSetCrystalItem> ARMOR_SET_CRYSTAL = ITEMS.registerItem(
            "armor_set_crystal", ArmorSetCrystalItem::new,
            properties -> properties.stacksTo(ArmorSetCrystalItem.MAX_STACK_SIZE));

    public static final DeferredItem<CosmicEnchantmentBookItem> COSMIC_ENCHANTMENT_BOOK = ITEMS.registerItem(
            "cosmic_enchantment_book", CosmicEnchantmentBookItem::new, properties -> properties.stacksTo(1));

    public static final DeferredItem<CosmicDustItem> COSMIC_DUST = ITEMS.registerItem(
            "cosmic_dust", CosmicDustItem::new, properties -> properties.stacksTo(64));

    public static final DeferredItem<UnexaminedEnchantmentBookItem> UNEXAMINED_ENCHANTMENT_BOOK = ITEMS.registerItem(
            "unexamined_enchantment_book", UnexaminedEnchantmentBookItem::new, properties -> properties.stacksTo(64));

    public static final DeferredItem<WhiteScrollItem> WHITE_SCROLL = ITEMS.registerItem(
            "white_scroll", WhiteScrollItem::new, properties -> properties.stacksTo(64));

    public static final DeferredItem<HolyWhiteScrollItem> HOLY_WHITE_SCROLL = ITEMS.registerItem(
            "holy_white_scroll", HolyWhiteScrollItem::new, properties -> properties.stacksTo(64));

    public static final DeferredItem<BoldNameItem> TRANSMOG_SCROLL = ITEMS.registerItem(
            "transmog_scroll", BoldNameItem::new, properties -> properties.stacksTo(64));

    public static final DeferredItem<EnchantmentOrbItem> ARMOR_ENCHANTMENT_ORB = ITEMS.registerItem(
            "armor_enchantment_orb", EnchantmentOrbItem::new,
            properties -> properties.stacksTo(EnchantmentOrbItem.MAX_STACK_SIZE));

    public static final DeferredItem<EnchantmentOrbItem> WEAPON_ENCHANTMENT_ORB = ITEMS.registerItem(
            "weapon_enchantment_orb", EnchantmentOrbItem::new,
            properties -> properties.stacksTo(EnchantmentOrbItem.MAX_STACK_SIZE));

    public static final DeferredItem<BlackScrollItem> BLACK_SCROLL = ITEMS.registerItem(
            "black_scroll", BlackScrollItem::new,
            properties -> properties.stacksTo(BlackScrollItem.MAX_STACK_SIZE));

    public static final DeferredItem<EnchantedBlackScrollItem> ENCHANTED_BLACK_SCROLL = ITEMS.registerItem(
            "enchanted_black_scroll", EnchantedBlackScrollItem::new, properties -> properties.stacksTo(1));

    public static final DeferredItem<SalvagedXpBottleItem> SALVAGED_XP_BOTTLE = ITEMS.registerItem(
            "salvaged_xp_bottle", SalvagedXpBottleItem::new, properties -> properties.stacksTo(1));

    public static final DeferredItem<WeaponSkinItem> WEAPON_SKIN = ITEMS.registerItem(
            "weapon_skin", WeaponSkinItem::new, properties -> properties.stacksTo(1));

    public static final DeferredItem<HeroicCrystalItem> HEROIC_CRYSTAL = ITEMS.registerItem(
            "heroic_crystal", HeroicCrystalItem::new,
            properties -> properties.stacksTo(HeroicCrystalItem.MAX_STACK_SIZE));

    public static final DeferredItem<BanknoteItem> BANKNOTE = ITEMS.registerItem(
            "banknote", BanknoteItem::new, properties -> properties.stacksTo(1));

    public static final DeferredItem<PersonalVaultUnlockItem> PERSONAL_VAULT_UNLOCK = ITEMS.registerItem(
            "personal_vault_unlock", PersonalVaultUnlockItem::new, properties -> properties.stacksTo(64));

    public static final DeferredItem<BoldNameItem> REPAIR_SCROLL = ITEMS.registerItem(
            "repair_scroll", BoldNameItem::new, properties -> properties.stacksTo(64));

    public static final DeferredItem<TypedMobSpawnerItem> MOB_SPAWNER = ITEMS.registerItem(
            "mob_spawner", TypedMobSpawnerItem::new, properties -> properties.stacksTo(64));

    public static final DeferredItem<MysterySpawnerItem> MYSTERY_SIMPLE_SPAWNER = mysterySpawner(
            "mystery_simple_spawner", MysterySpawnerTier.SIMPLE);
    public static final DeferredItem<MysterySpawnerItem> MYSTERY_ELITE_SPAWNER = mysterySpawner(
            "mystery_elite_spawner", MysterySpawnerTier.ELITE);
    public static final DeferredItem<MysterySpawnerItem> MYSTERY_MASTERY_SPAWNER = mysterySpawner(
            "mystery_mastery_spawner", MysterySpawnerTier.MASTERY);

    public static final DeferredItem<VKitCrystalItem> PHOENIX_VKIT_CRYSTAL = vkitCrystal(
            "phoenix_vkit_crystal", VKitDefinition.PHOENIX);
    public static final DeferredItem<VKitCrystalItem> OGRE_VKIT_CRYSTAL = vkitCrystal(
            "ogre_vkit_crystal", VKitDefinition.OGRE);
    public static final DeferredItem<VKitCrystalItem> JUDGEMENT_VKIT_CRYSTAL = vkitCrystal(
            "judgement_vkit_crystal", VKitDefinition.JUDGEMENT);
    public static final DeferredItem<VKitCrystalItem> SLAYER_VKIT_CRYSTAL = vkitCrystal(
            "slayer_vkit_crystal", VKitDefinition.SLAYER);
    public static final DeferredItem<GodlyVKitBundleItem> GODLY_VKIT_BUNDLE = ITEMS.registerItem(
            "godly_vkit_bundle", GodlyVKitBundleItem::new,
            properties -> properties.stacksTo(GodlyVKitBundleItem.MAX_STACK_SIZE));
    public static final DeferredItem<AnimatedLootboxItem> SECRET_WEAPON_CACHE = ITEMS.registerItem(
            "secret_weapon_cache", properties -> new AnimatedLootboxItem(properties,
                    AnimatedLootboxItem.Kind.SECRET_WEAPON_CACHE), properties -> properties.stacksTo(1));
    public static final DeferredItem<AnimatedLootboxItem> COSMIC_ENCHANTMENT_TABLE = ITEMS.registerItem(
            "cosmic_enchantment_table", properties -> new AnimatedLootboxItem(properties,
                    AnimatedLootboxItem.Kind.COSMIC_ENCHANTMENT_TABLE), properties -> properties.stacksTo(1));
    public static final DeferredItem<AnimatedLootboxItem> HEROIC_COSMIC_ENCHANTMENT_TABLE = ITEMS.registerItem(
            "heroic_cosmic_enchantment_table", properties -> new AnimatedLootboxItem(properties,
                    AnimatedLootboxItem.Kind.HEROIC_COSMIC_ENCHANTMENT_TABLE), properties -> properties.stacksTo(1));
    public static final DeferredItem<AnimatedLootboxItem> ADMIN_ABUSE = ITEMS.registerItem(
            "admin_abuse", properties -> new AnimatedLootboxItem(properties,
                    AnimatedLootboxItem.Kind.ADMIN_ABUSE), properties -> properties.stacksTo(1));
    public static final DeferredItem<AnimatedLootboxItem> RANDOM_WEAPON_SKIN_GENERATOR = ITEMS.registerItem(
            "random_weapon_skin_generator", properties -> new AnimatedLootboxItem(properties,
                    AnimatedLootboxItem.Kind.RANDOM_WEAPON_SKIN_GENERATOR), properties -> properties.stacksTo(1));
    public static final DeferredItem<SpaceDustBundleItem> SPACE_DUST_BUNDLE = ITEMS.registerItem(
            "space_dust_bundle", SpaceDustBundleItem::new, properties -> properties.stacksTo(1));

    public static final DeferredItem<SpaceChestItem> SPACE_CHEST = ITEMS.registerItem(
            "space_chest", SpaceChestItem::new, properties -> properties.stacksTo(1));

    public static final DeferredItem<TrialPortalItem> TRIAL_PORTAL = ITEMS.registerItem(
            "trial_portal", TrialPortalItem::new, properties -> properties.stacksTo(64));

    private static final java.util.List<DeferredItem<CallOfForestItem>> PRODUCTION_CALLS = new java.util.ArrayList<>();
    public static final DeferredItem<AnimatedLootboxItem> MYSTERY_CALL_OF_ADVENTURE = ITEMS.registerItem(
            "mystery_call_of_adventure", properties -> new AnimatedLootboxItem(properties,
                    AnimatedLootboxItem.Kind.MYSTERY_CALL_OF_ADVENTURE), properties -> properties.stacksTo(1));
    public static final DeferredItem<CallOfForestItem> CALL_OF_FOREST_10 = callOfForest("call_of_forest_10", 10);
    public static final DeferredItem<CallOfForestItem> CALL_OF_FOREST_20 = callOfForest("call_of_forest_20", 20);
    public static final DeferredItem<CallOfForestItem> CALL_OF_FOREST_30 = callOfForest("call_of_forest_30", 30);
    public static final DeferredItem<DenseWoodlandsScrapItem> DENSE_WOODLANDS_SCRAP = ITEMS.registerItem(
            "dense_woodlands_scrap", DenseWoodlandsScrapItem::new, properties -> properties.stacksTo(64)
                    .component(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
    public static final DeferredItem<AdventureCompassItem> ADVENTURE_COMPASS = ITEMS.registerItem(
            "adventure_compass", AdventureCompassItem::new, properties -> properties.stacksTo(1));

    public static final DeferredItem<ConquestFlareItem> CONQUEST_CHEST_FLARE = ITEMS.registerItem(
            "conquest_chest_flare", ConquestFlareItem::new, properties -> properties.stacksTo(64));

    public static final DeferredItem<MaskItem> MASK = ITEMS.registerItem(
            "mask", MaskItem::new, properties -> properties.stacksTo(1));
    public static final DeferredItem<MaskSplicerItem> MASK_SPLICER = ITEMS.registerItem(
            "mask_splicer", MaskSplicerItem::new, properties -> properties.stacksTo(1));

    public static final DeferredItem<TrialTrinketItem> TRIAL_TRINKET_TIME_1 = trinket("trial_trinket_time_1", TrialTrinketType.TIME, 1);
    public static final DeferredItem<TrialTrinketItem> TRIAL_TRINKET_TIME_3 = trinket("trial_trinket_time_3", TrialTrinketType.TIME, 3);
    public static final DeferredItem<TrialTrinketItem> TRIAL_TRINKET_TIME_5 = trinket("trial_trinket_time_5", TrialTrinketType.TIME, 5);
    public static final DeferredItem<TrialTrinketItem> TRIAL_TRINKET_SKIP_1 = trinket("trial_trinket_skip_1", TrialTrinketType.SKIP, 1);
    public static final DeferredItem<TrialTrinketItem> TRIAL_TRINKET_SKIP_2 = trinket("trial_trinket_skip_2", TrialTrinketType.SKIP, 2);
    public static final DeferredItem<TrialTrinketItem> TRIAL_TRINKET_SKIP_3 = trinket("trial_trinket_skip_3", TrialTrinketType.SKIP, 3);
    public static final DeferredItem<TrialTrinketItem> TRIAL_TRINKET_INSURANCE_1 = trinket("trial_trinket_insurance_1", TrialTrinketType.INSURANCE, 1);
    public static final DeferredItem<TrialTrinketItem> TRIAL_TRINKET_INSURANCE_2 = trinket("trial_trinket_insurance_2", TrialTrinketType.INSURANCE, 2);
    public static final DeferredItem<TrialTrinketItem> TRIAL_TRINKET_INSURANCE_3 = trinket("trial_trinket_insurance_3", TrialTrinketType.INSURANCE, 3);
    public static final DeferredItem<TrialTrinketItem> TRIAL_TRINKET_FAME_33 = trinket("trial_trinket_fame_33", TrialTrinketType.FAME, 33);
    public static final DeferredItem<TrialTrinketItem> TRIAL_TRINKET_FAME_66 = trinket("trial_trinket_fame_66", TrialTrinketType.FAME, 66);
    public static final DeferredItem<TrialTrinketItem> TRIAL_TRINKET_FAME_100 = trinket("trial_trinket_fame_100", TrialTrinketType.FAME, 100);

    private static DeferredItem<TrialTrinketItem> trinket(String name, TrialTrinketType type, int value) {
        return ITEMS.registerItem(name, TrialTrinketItem::new, properties -> properties.stacksTo(64)
                .component(ModDataComponents.TRIAL_TRINKET.get(), new TrialTrinketData(type, value)));
    }

    private static DeferredItem<AmuletItem> amulet(String name, AmuletDefinition definition) {
        return ITEMS.registerItem(name, AmuletItem::new, properties -> properties.stacksTo(1)
                .component(ModDataComponents.ACCESSORY_ITEM.get(), new AccessoryItemData(
                        AccessoryItemData.DATA_VERSION, AccessorySlot.AMULET, definition.id())));
    }

    private static DeferredItem<BeltItem> belt(String name, BeltDefinition definition) {
        return ITEMS.registerItem(name, BeltItem::new, properties -> properties.stacksTo(1)
                .component(ModDataComponents.ACCESSORY_ITEM.get(), new AccessoryItemData(
                        AccessoryItemData.DATA_VERSION, AccessorySlot.BELT, definition.id())));
    }

    private static DeferredItem<CallOfForestItem> callOfForest(String name, int minutes) {
        var item = ITEMS.registerItem(name, CallOfForestItem::new, properties -> properties.stacksTo(64)
                .component(ModDataComponents.CALL_OF_FOREST.get(), new CallOfForestData(CallOfForestData.CURRENT_DATA_VERSION, minutes))
                .component(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
        PRODUCTION_CALLS.add(item);
        return item;
    }

    /** Fresh canonical stacks, one per registered production Call SKU, without activating a Call. */
    public static java.util.List<net.minecraft.world.item.ItemStack> productionCalls() {
        return PRODUCTION_CALLS.stream().map(item -> new net.minecraft.world.item.ItemStack(item.get())).toList();
    }

    private static DeferredItem<MysterySpawnerItem> mysterySpawner(String name, MysterySpawnerTier tier) {
        return ITEMS.registerItem(name, MysterySpawnerItem::new, properties -> properties.stacksTo(64)
                .component(ModDataComponents.MYSTERY_SPAWNER.get(),
                        new MysterySpawnerData(MysterySpawnerData.CURRENT_DATA_VERSION, tier)));
    }

    private static DeferredItem<VKitCrystalItem> vkitCrystal(String name, VKitDefinition definition) {
        return ITEMS.registerItem(name, VKitCrystalItem::new, properties -> properties.stacksTo(VKitCrystalItem.MAX_STACK_SIZE)
                .component(ModDataComponents.VKIT_CRYSTAL.get(), new VKitCrystalData(
                        VKitCrystalData.CURRENT_DATA_VERSION, definition.id())));
    }

    private ModItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
