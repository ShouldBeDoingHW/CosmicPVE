package com.cosmicpve.trial;

import com.cosmicpve.trial.persistence.TrialInventoryTransactionService;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.component.DyedItemColor;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.data.component.CustomEnchantMetadata;

public final class TrialRoomLoadoutService {
    public static final int FIRE_COLONY_ARMOR_COLOR = 0xFF0000;
    public static final int ZERO_G_PROTECTION_LEVEL = 4;
    public static final int ZERO_G_ANGELIC_LEVEL = 5;
    public static final int ZERO_G_UNBREAKING_LEVEL = 3;
    public static final int ZERO_G_GOLDEN_APPLES = 2;
    public static final int ZERO_G_MILK_BUCKETS = 3;
    public static final int RAIDING_RAINBOW_UNBREAKING_LEVEL = 3;
    public static final int RAIDING_RAINBOW_PORKCHOPS = 16;
    public static final int RAIDING_RAINBOW_ENDER_PEARLS = 0;
    public static final int COLD_SNAP_FEATHER_FALLING_LEVEL = 4;
    public static final int COLD_SNAP_PORKCHOPS = 16;
    public static final int BOMB_SQUAD_UNBREAKING_LEVEL = 3;
    public static final int BOMB_SQUAD_BLAST_PROTECTION_LEVEL = 1;
    public static final int BOMB_SQUAD_KNOCKBACK_LEVEL = 2;
    public static final int BOMB_SQUAD_STEAK = 5;
    public static final int BOMB_SQUAD_GOLDEN_APPLES = 5;
    public static final int HIDDEN_GRAVEYARD_PROTECTION = 4;
    public static final int HIDDEN_GRAVEYARD_UNBREAKING = 3;
    public static final int HIDDEN_GRAVEYARD_ENDER_SHIFT = 3;
    public static final int HIDDEN_GRAVEYARD_NUTRITION = 3;
    public static final int HIDDEN_GRAVEYARD_INSANITY = 8;
    public static final int HIDDEN_GRAVEYARD_PUMMEL = 3;
    public static final int HIDDEN_GRAVEYARD_APPLES = 5;
    public static final int HIDDEN_GRAVEYARD_HEALING_POTIONS = 4;
    public static final int DEADEYE_GOLDEN_APPLES = 16;
    public static final int DEADEYE_ARROWS = 1;
    public static final int DEADEYE_NUTRITION = 3;
    public static final int DEADEYE_LIGHTNING = 4;
    public static final int DEADEYE_EAGLE_EYE = 5;
    public static final java.util.List<net.minecraft.world.item.Item> RAIDING_RAINBOW_ARMOR = java.util.List.of(
            Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);
    private final TrialInventoryTransactionService inventories;
    public TrialRoomLoadoutService(TrialInventoryTransactionService inventories) { this.inventories = inventories; }
    public void clear(ServerPlayer player) { inventories.clearTrialInventory(player); }
    public void applyDevelopment(ServerPlayer player) {
        clear(player);
        ItemStack marker = new ItemStack(Items.STICK);
        marker.set(DataComponents.CUSTOM_NAME, Component.literal("Development Trial Item"));
        player.getInventory().setItem(0, marker);
        player.getInventory().setSelectedSlot(0);
    }
    public void applyRaidingRainbow(ServerPlayer player) {
        clear(player);
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        var unbreaking = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING);
        EnchantmentHelper.updateEnchantments(sword, mutable -> mutable.set(unbreaking, RAIDING_RAINBOW_UNBREAKING_LEVEL));
        player.getInventory().setItem(0, sword);
        player.getInventory().setItem(1, new ItemStack(Items.COOKED_PORKCHOP, RAIDING_RAINBOW_PORKCHOPS));
        equipUnbreaking(player, EquipmentSlot.HEAD, Items.NETHERITE_HELMET, unbreaking);
        equipUnbreaking(player, EquipmentSlot.CHEST, Items.NETHERITE_CHESTPLATE, unbreaking);
        equipUnbreaking(player, EquipmentSlot.LEGS, Items.NETHERITE_LEGGINGS, unbreaking);
        equipUnbreaking(player, EquipmentSlot.FEET, Items.NETHERITE_BOOTS, unbreaking);
        player.getInventory().setSelectedSlot(0);
    }
    public void applyCircuitCircus(ServerPlayer player) {
        clear(player);
        ItemStack bow = new ItemStack(Items.BOW);
        var registry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        EnchantmentHelper.updateEnchantments(bow, mutable -> mutable.set(registry.getOrThrow(Enchantments.INFINITY), 1));
        player.getInventory().setItem(0, bow);
        player.getInventory().setItem(1, new ItemStack(Items.ARROW));
        player.getInventory().setSelectedSlot(0);
    }
    public void applyFireColony(ServerPlayer player) {
        clear(player);
        equipDyed(player, EquipmentSlot.HEAD, Items.LEATHER_HELMET);
        equipDyed(player, EquipmentSlot.CHEST, Items.LEATHER_CHESTPLATE);
        equipDyed(player, EquipmentSlot.LEGS, Items.LEATHER_LEGGINGS);
        equipDyed(player, EquipmentSlot.FEET, Items.LEATHER_BOOTS);
        player.getInventory().setItem(0, new ItemStack(Items.GOLDEN_APPLE));
        player.getInventory().setSelectedSlot(0);
    }
    public void applyZeroG(ServerPlayer player) {
        clear(player);
        equipZeroG(player, EquipmentSlot.HEAD, Items.IRON_HELMET);
        equipZeroG(player, EquipmentSlot.CHEST, Items.IRON_CHESTPLATE);
        equipZeroG(player, EquipmentSlot.LEGS, Items.IRON_LEGGINGS);
        equipZeroG(player, EquipmentSlot.FEET, Items.IRON_BOOTS);
        player.getInventory().setItem(0, new ItemStack(Items.GOLDEN_APPLE, 2));
        player.getInventory().setItem(1, new ItemStack(Items.MILK_BUCKET));
        player.getInventory().setItem(2, new ItemStack(Items.MILK_BUCKET));
        player.getInventory().setItem(3, new ItemStack(Items.MILK_BUCKET));
        player.getInventory().setSelectedSlot(0);
    }
    public void applyColdSnap(ServerPlayer player) {
        clear(player);
        ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
        var registry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        EnchantmentHelper.updateEnchantments(boots, mutable -> mutable.set(
                registry.getOrThrow(Enchantments.FEATHER_FALLING), COLD_SNAP_FEATHER_FALLING_LEVEL));
        player.setItemSlot(EquipmentSlot.FEET, boots);
        player.getInventory().setItem(0, new ItemStack(Items.COOKED_PORKCHOP, COLD_SNAP_PORKCHOPS));
        player.getInventory().setSelectedSlot(0);
    }
    public void applyBombSquad(ServerPlayer player) {
        clear(player);
        var registry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        equipBombSquad(player, EquipmentSlot.HEAD, Items.LEATHER_HELMET, registry);
        equipBombSquad(player, EquipmentSlot.CHEST, Items.LEATHER_CHESTPLATE, registry);
        equipBombSquad(player, EquipmentSlot.LEGS, Items.LEATHER_LEGGINGS, registry);
        equipBombSquad(player, EquipmentSlot.FEET, Items.LEATHER_BOOTS, registry);
        ItemStack flint = new ItemStack(Items.FLINT_AND_STEEL);
        EnchantmentHelper.updateEnchantments(flint, mutable -> mutable.set(
                registry.getOrThrow(Enchantments.UNBREAKING), BOMB_SQUAD_UNBREAKING_LEVEL));
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        EnchantmentHelper.updateEnchantments(sword, mutable -> {
            mutable.set(registry.getOrThrow(Enchantments.KNOCKBACK), BOMB_SQUAD_KNOCKBACK_LEVEL);
            mutable.set(registry.getOrThrow(Enchantments.UNBREAKING), BOMB_SQUAD_UNBREAKING_LEVEL);
        });
        player.getInventory().setItem(0, flint);
        player.getInventory().setItem(1, sword);
        player.getInventory().setItem(2, new ItemStack(Items.COOKED_BEEF, BOMB_SQUAD_STEAK));
        player.getInventory().setItem(3, new ItemStack(Items.GOLDEN_APPLE, BOMB_SQUAD_GOLDEN_APPLES));
        player.getInventory().setSelectedSlot(0);
    }
    public void applyHiddenGraveyard(ServerPlayer player) {
        clear(player);
        var registry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        equipHiddenGraveyard(player, EquipmentSlot.HEAD, Items.IRON_HELMET, registry);
        equipHiddenGraveyard(player, EquipmentSlot.CHEST, Items.IRON_CHESTPLATE, registry);
        equipHiddenGraveyard(player, EquipmentSlot.LEGS, Items.IRON_LEGGINGS, registry);
        equipHiddenGraveyard(player, EquipmentSlot.FEET, Items.IRON_BOOTS, registry);
        ItemStack axe = new ItemStack(Items.DIAMOND_AXE);
        EnchantmentHelper.updateEnchantments(axe, mutable -> {
            mutable.set(registry.getOrThrow(Enchantments.SHARPNESS), 5);
            mutable.set(registry.getOrThrow(Enchantments.UNBREAKING), 3);
            mutable.set(registry.getOrThrow(ModEnchantments.INSANITY), HIDDEN_GRAVEYARD_INSANITY);
            mutable.set(registry.getOrThrow(ModEnchantments.PUMMEL), HIDDEN_GRAVEYARD_PUMMEL);
        });
        player.getInventory().setItem(0, axe);
        player.getInventory().setItem(1, new ItemStack(Items.APPLE, HIDDEN_GRAVEYARD_APPLES));
        ItemStack healing = net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_HEALING);
        healing.setCount(HIDDEN_GRAVEYARD_HEALING_POTIONS); player.getInventory().setItem(2, healing);
        player.getInventory().setSelectedSlot(0);
    }
    public void applyDeadeye(ServerPlayer player) {
        clear(player);
        var registry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        EnchantmentHelper.updateEnchantments(leggings, mutable -> mutable.set(
                registry.getOrThrow(ModEnchantments.NUTRITION), DEADEYE_NUTRITION));
        player.setItemSlot(EquipmentSlot.LEGS, leggings);

        ItemStack bow = new ItemStack(Items.BOW);
        EnchantmentHelper.updateEnchantments(bow, mutable -> {
            mutable.set(registry.getOrThrow(Enchantments.INFINITY), 1);
            mutable.set(registry.getOrThrow(Enchantments.POWER), 5);
            mutable.set(registry.getOrThrow(ModEnchantments.LIGHTNING), DEADEYE_LIGHTNING);
            mutable.set(registry.getOrThrow(Enchantments.UNBREAKING), 3);
            mutable.set(registry.getOrThrow(Enchantments.FLAME), 1);
            mutable.set(registry.getOrThrow(ModEnchantments.EAGLE_EYE), DEADEYE_EAGLE_EYE);
        });
        bow.set(ModDataComponents.CUSTOM_ENCHANT_META.get(), CustomEnchantMetadata.DEFAULT.withTransmogSorted(true));
        player.getInventory().setItem(0, bow);
        player.getInventory().setItem(1, new ItemStack(Items.GOLDEN_APPLE, DEADEYE_GOLDEN_APPLES));
        player.getInventory().setItem(2, new ItemStack(Items.ARROW, DEADEYE_ARROWS));
        player.getInventory().setSelectedSlot(0);
    }
    private static void equipDyed(ServerPlayer player, EquipmentSlot slot, net.minecraft.world.item.Item item) {
        ItemStack stack = new ItemStack(item); stack.set(DataComponents.DYED_COLOR, new DyedItemColor(FIRE_COLONY_ARMOR_COLOR));
        player.setItemSlot(slot, stack);
    }
    private static void equipZeroG(ServerPlayer player, EquipmentSlot slot, net.minecraft.world.item.Item item) {
        ItemStack stack = new ItemStack(item);
        var registry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        EnchantmentHelper.updateEnchantments(stack, mutable -> {
            mutable.set(registry.getOrThrow(Enchantments.PROTECTION), ZERO_G_PROTECTION_LEVEL);
            mutable.set(registry.getOrThrow(Enchantments.UNBREAKING), ZERO_G_UNBREAKING_LEVEL);
            mutable.set(registry.getOrThrow(ModEnchantments.ANGELIC), ZERO_G_ANGELIC_LEVEL);
        });
        player.setItemSlot(slot, stack);
    }
    private static void equipUnbreaking(ServerPlayer player, EquipmentSlot slot, net.minecraft.world.item.Item item,
            net.minecraft.core.Holder.Reference<net.minecraft.world.item.enchantment.Enchantment> unbreaking) {
        ItemStack stack = new ItemStack(item);
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(unbreaking, RAIDING_RAINBOW_UNBREAKING_LEVEL));
        player.setItemSlot(slot, stack);
    }
    private static void equipBombSquad(ServerPlayer player, EquipmentSlot slot, net.minecraft.world.item.Item item,
            net.minecraft.core.Registry<net.minecraft.world.item.enchantment.Enchantment> registry) {
        ItemStack stack = new ItemStack(item);
        EnchantmentHelper.updateEnchantments(stack, mutable -> {
            mutable.set(registry.getOrThrow(Enchantments.BLAST_PROTECTION), BOMB_SQUAD_BLAST_PROTECTION_LEVEL);
            mutable.set(registry.getOrThrow(Enchantments.UNBREAKING), BOMB_SQUAD_UNBREAKING_LEVEL);
        });
        player.setItemSlot(slot, stack);
    }
    private static void equipHiddenGraveyard(ServerPlayer player, EquipmentSlot slot, net.minecraft.world.item.Item item,
            net.minecraft.core.Registry<net.minecraft.world.item.enchantment.Enchantment> registry) {
        ItemStack stack = new ItemStack(item);
        EnchantmentHelper.updateEnchantments(stack, mutable -> {
            mutable.set(registry.getOrThrow(Enchantments.PROTECTION), HIDDEN_GRAVEYARD_PROTECTION);
            mutable.set(registry.getOrThrow(Enchantments.UNBREAKING), HIDDEN_GRAVEYARD_UNBREAKING);
            if (slot == EquipmentSlot.HEAD) mutable.set(registry.getOrThrow(ModEnchantments.ENDER_SHIFT), HIDDEN_GRAVEYARD_ENDER_SHIFT);
            if (slot == EquipmentSlot.LEGS) mutable.set(registry.getOrThrow(ModEnchantments.NUTRITION), HIDDEN_GRAVEYARD_NUTRITION);
        });
        player.setItemSlot(slot, stack);
    }
}
