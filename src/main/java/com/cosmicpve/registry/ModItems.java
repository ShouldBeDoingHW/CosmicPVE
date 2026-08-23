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
import com.cosmicpve.equipment.skin.WeaponSkinItem;
import com.cosmicpve.equipment.heroic.HeroicCrystalItem;
import com.cosmicpve.equipment.enchantment.UnexaminedEnchantmentBookItem;
import com.cosmicpve.economy.BanknoteItem;
import com.cosmicpve.reward.spawner.TypedMobSpawnerItem;
import com.cosmicpve.spacechest.SpaceChestItem;
import com.cosmicpve.trial.portal.TrialPortalItem;

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

    public static final DeferredItem<UnexaminedEnchantmentBookItem> UNEXAMINED_ENCHANTMENT_BOOK = ITEMS.registerItem(
            "unexamined_enchantment_book", UnexaminedEnchantmentBookItem::new, properties -> properties.stacksTo(64));

    public static final DeferredItem<Item> WHITE_SCROLL = ITEMS.registerSimpleItem(
            "white_scroll", properties -> properties.stacksTo(64));

    public static final DeferredItem<Item> TRANSMOG_SCROLL = ITEMS.registerSimpleItem(
            "transmog_scroll", properties -> properties.stacksTo(64));

    public static final DeferredItem<EnchantmentOrbItem> ARMOR_ENCHANTMENT_ORB = ITEMS.registerItem(
            "armor_enchantment_orb", EnchantmentOrbItem::new,
            properties -> properties.stacksTo(EnchantmentOrbItem.MAX_STACK_SIZE));

    public static final DeferredItem<EnchantmentOrbItem> WEAPON_ENCHANTMENT_ORB = ITEMS.registerItem(
            "weapon_enchantment_orb", EnchantmentOrbItem::new,
            properties -> properties.stacksTo(EnchantmentOrbItem.MAX_STACK_SIZE));

    public static final DeferredItem<BlackScrollItem> BLACK_SCROLL = ITEMS.registerItem(
            "black_scroll", BlackScrollItem::new,
            properties -> properties.stacksTo(BlackScrollItem.MAX_STACK_SIZE));

    public static final DeferredItem<WeaponSkinItem> WEAPON_SKIN = ITEMS.registerItem(
            "weapon_skin", WeaponSkinItem::new, properties -> properties.stacksTo(1));

    public static final DeferredItem<HeroicCrystalItem> HEROIC_CRYSTAL = ITEMS.registerItem(
            "heroic_crystal", HeroicCrystalItem::new,
            properties -> properties.stacksTo(HeroicCrystalItem.MAX_STACK_SIZE));

    public static final DeferredItem<BanknoteItem> BANKNOTE = ITEMS.registerItem(
            "banknote", BanknoteItem::new, properties -> properties.stacksTo(1));

    public static final DeferredItem<Item> REPAIR_SCROLL = ITEMS.registerSimpleItem(
            "repair_scroll", properties -> properties.stacksTo(64));

    public static final DeferredItem<TypedMobSpawnerItem> MOB_SPAWNER = ITEMS.registerItem(
            "mob_spawner", TypedMobSpawnerItem::new, properties -> properties.stacksTo(64));

    public static final DeferredItem<SpaceChestItem> SPACE_CHEST = ITEMS.registerItem(
            "space_chest", SpaceChestItem::new, properties -> properties.stacksTo(1));

    public static final DeferredItem<TrialPortalItem> TRIAL_PORTAL = ITEMS.registerItem(
            "trial_portal", TrialPortalItem::new, properties -> properties.stacksTo(64));

    public static final DeferredItem<Item> TRIAL_TRINKET_TIME_1 = ITEMS.registerSimpleItem(
            "trial_trinket_time_1", properties -> properties.stacksTo(64));
    public static final DeferredItem<Item> TRIAL_TRINKET_INSURANCE_1 = ITEMS.registerSimpleItem(
            "trial_trinket_insurance_1", properties -> properties.stacksTo(64));
    public static final DeferredItem<Item> TRIAL_TRINKET_SKIP_1 = ITEMS.registerSimpleItem(
            "trial_trinket_skip_1", properties -> properties.stacksTo(64));

    private ModItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
