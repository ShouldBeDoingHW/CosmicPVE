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

public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CosmicPVE.MOD_ID);

    public static final DeferredItem<Item> FOUNDATION_TOKEN = ITEMS.registerSimpleItem(
            "foundation_token",
            properties -> properties.stacksTo(1));

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
    public static final DeferredItem<AnimatedLootboxItem> ADMIN_ABUSE = ITEMS.registerItem(
            "admin_abuse", properties -> new AnimatedLootboxItem(properties,
                    AnimatedLootboxItem.Kind.ADMIN_ABUSE), properties -> properties.stacksTo(1));

    public static final DeferredItem<SpaceChestItem> SPACE_CHEST = ITEMS.registerItem(
            "space_chest", SpaceChestItem::new, properties -> properties.stacksTo(1));

    public static final DeferredItem<TrialPortalItem> TRIAL_PORTAL = ITEMS.registerItem(
            "trial_portal", TrialPortalItem::new, properties -> properties.stacksTo(64));

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

    private static DeferredItem<TrialTrinketItem> trinket(String name, TrialTrinketType type, int value) {
        return ITEMS.registerItem(name, TrialTrinketItem::new, properties -> properties.stacksTo(64)
                .component(ModDataComponents.TRIAL_TRINKET.get(), new TrialTrinketData(type, value)));
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
