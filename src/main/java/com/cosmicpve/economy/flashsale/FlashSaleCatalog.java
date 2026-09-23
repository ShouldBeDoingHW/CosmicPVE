package com.cosmicpve.economy.flashsale;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.data.component.MysterySpawnerTier;
import com.cosmicpve.equipment.armor.ArmorSetCrystals;
import com.cosmicpve.equipment.enchantment.EnchantingRewardItemFactory;
import com.cosmicpve.equipment.enchantment.OrbType;
import com.cosmicpve.registry.ModItems;
import com.cosmicpve.reward.spawner.MysterySpawners;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class FlashSaleCatalog {
    private static final EnchantingRewardItemFactory ENCHANTING = new EnchantingRewardItemFactory();

    public static final List<FlashSaleEntry> CANONICAL_ROWS = List.of(
            item("trial_portal_1", "Trial Portal", 1, 20_000_000L, 35_000_000L, 50_000_000L, ModItems.TRIAL_PORTAL.get()),
            item("trial_portal_2", "Trial Portal", 2, 35_000_000L, 55_000_000L, 87_500_000L, ModItems.TRIAL_PORTAL.get()),
            item("memory_chest", "Memory Chest", 1, 250_000_000L, 350_000_000L, 450_000_000L,
                    ModItems.MEMORY_CHEST.get()),
            generated("armor_orb_100", "100% Armor Enchantment Orb", 1, 30_000_000L, 50_000_000L, 60_000_000L,
                    random -> new ItemStack[]{ENCHANTING.orb(OrbType.ARMOR, 100, random)}),
            generated("weapon_orb_100", "100% Weapon Enchantment Orb", 1, 30_000_000L, 50_000_000L, 60_000_000L,
                    random -> new ItemStack[]{ENCHANTING.orb(OrbType.WEAPON, 100, random)}),
            crystal("engineer_crystal_50", "50% Engineer Crystal", "engineer", 50, 240_000_000L, 280_000_000L, 320_000_000L),
            crystal("phantom_crystal_50", "50% Phantom Crystal", "phantom", 50, 240_000_000L, 280_000_000L, 320_000_000L),
            crystal("ranger_crystal_50", "50% Ranger Crystal", "ranger", 50, 240_000_000L, 280_000_000L, 320_000_000L),
            crystal("dragonslayer_crystal_50", "50% Dragonslayer Crystal", "dragonslayer", 50, 240_000_000L, 280_000_000L, 320_000_000L),
            crystal("yeti_crystal_75", "75% Yeti Crystal", "yeti", 75, 280_000_000L, 320_000_000L, 360_000_000L),
            crystal("dimensional_traveler_crystal_75", "75% Dimensional Traveler Crystal", "dimensional_traveler", 75,
                    280_000_000L, 320_000_000L, 360_000_000L),
            crystal("yjiki_crystal_75", "75% Yjiki Crystal", "yjiki", 75, 280_000_000L, 320_000_000L, 360_000_000L),
            crystal("ancient_crystal_75", "75% Ancient Crystal", "ancient", 75, 280_000_000L, 320_000_000L, 360_000_000L),
            deferred("abandoned_spaceship_portal", "Abandoned Spaceship Portal", 1,
                    150_000_000L, 160_000_000L, 170_000_000L),
            generated("mystery_elite_spawner", "Mystery Elite Spawner", 1, 60_000_000L, 80_000_000L, 100_000_000L,
                    random -> new ItemStack[]{MysterySpawners.create(MysterySpawnerTier.ELITE, 1)}),
            generated("mystery_simple_spawner", "Mystery Simple Spawner", 1, 25_000_000L, 30_000_000L, 35_000_000L,
                    random -> new ItemStack[]{MysterySpawners.create(MysterySpawnerTier.SIMPLE, 1)}),
            item("repair_scroll_1", "Repair Scroll", 1, 5_000_000L, 7_000_000L, 9_000_000L, ModItems.REPAIR_SCROLL.get()),
            item("repair_scroll_5", "Repair Scroll", 5, 20_000_000L, 40_000_000L, 60_000_000L, ModItems.REPAIR_SCROLL.get()),
            item("heroic_crystal", "Heroic Crystal", 1, 45_000_000L, 65_000_000L, 77_500_000L, ModItems.HEROIC_CRYSTAL.get()),
            item("godly_vkit_bundle", "Godly V-Kit Bundle", 1, 600_000_000L, 700_000_000L, 800_000_000L, ModItems.GODLY_VKIT_BUNDLE.get()),
            item("cosmic_enchantment_table", "Cosmic Enchantment Table", 1, 90_000_000L, 100_000_000L, 120_000_000L,
                    ModItems.COSMIC_ENCHANTMENT_TABLE.get()),
            item("white_scroll", "White Scroll", 1, 10_000_000L, 12_500_000L, 17_500_000L, ModItems.WHITE_SCROLL.get()),
            item("mystery_call_of_adventure", "Mystery Call of Adventure", 1, 90_000_000L, 115_000_000L, 145_000_000L,
                    ModItems.MYSTERY_CALL_OF_ADVENTURE.get()),
            item("trials_creation_kit", "Trials Creation Kit", 1,
                    250_000_000L, 280_000_000L, 310_000_000L, ModItems.TRIALS_CREATION_KIT.get()),
            item("heroic_cosmic_enchantment_table", "Heroic Cosmic Enchantment Table", 1,
                    160_000_000L, 180_000_000L, 200_000_000L, ModItems.HEROIC_COSMIC_ENCHANTMENT_TABLE.get()),
            item("space_dust_bundle", "Space Dust Bundle", 1,
                    17_500_000L, 22_500_000L, 25_000_000L, ModItems.SPACE_DUST_BUNDLE.get()),
            socket("amulet_socket_40", "40% Amulet Socket", ModItems.AMULET_SOCKET.get(), 40,
                    120_000_000L, 140_000_000L, 160_000_000L),
            socket("belt_socket_40", "40% Belt Socket", ModItems.BELT_SOCKET.get(), 40,
                    120_000_000L, 140_000_000L, 160_000_000L),
            socket("omni_socket_40", "40% Omni Socket", ModItems.OMNI_SOCKET.get(), 40,
                    140_000_000L, 160_000_000L, 180_000_000L));
    public static final FlashSaleEntry MEMORY_CHEST = find("memory_chest").orElseThrow();

    private FlashSaleCatalog() {}

    public static List<FlashSaleEntry> productionRows() {
        return CANONICAL_ROWS.stream().filter(FlashSaleEntry::productionSelectable).toList();
    }

    public static Optional<FlashSaleEntry> find(String id) {
        return CANONICAL_ROWS.stream().filter(entry -> entry.id().equals(id)).findFirst();
    }

    private static FlashSaleEntry item(String id, String name, int quantity, long low, long medium, long high, Item item) {
        return generated(id, name, quantity, low, medium, high,
                random -> new ItemStack[]{new ItemStack(item, quantity)});
    }
    private static FlashSaleEntry crystal(String id, String name, String armorSet, int success,
            long low, long medium, long high) {
        return generated(id, name, 1, low, medium, high, random -> CosmicContent.repository()
                .findArmorSetDefinition(CosmicPVE.id(armorSet))
                .map(definition -> new ItemStack[]{ArmorSetCrystals.create(definition, success)}).orElseGet(() -> new ItemStack[0]));
    }
    private static FlashSaleEntry socket(String id, String name, Item item, int success,
            long low, long medium, long high) {
        return generated(id, name, 1, low, medium, high, random -> {
            var stack = new ItemStack(item);
            if (stack.is(ModItems.OMNI_SOCKET.get()))
                stack.set(com.cosmicpve.registry.ModDataComponents.OMNI_SOCKET_SUCCESS.get(), success);
            else {
                var slot = stack.is(ModItems.AMULET_SOCKET.get())
                        ? com.cosmicpve.data.component.AccessorySlot.AMULET
                        : com.cosmicpve.data.component.AccessorySlot.BELT;
                stack.set(com.cosmicpve.registry.ModDataComponents.ACCESSORY_SOCKET.get(),
                        new com.cosmicpve.data.component.AccessorySocketData(
                                com.cosmicpve.data.component.AccessorySocketData.DATA_VERSION, slot, success));
            }
            return new ItemStack[]{stack};
        });
    }
    private static FlashSaleEntry deferred(String id, String name, int quantity, long low, long medium, long high) {
        return new FlashSaleEntry(id, Component.literal(name), quantity, low, medium, high, false,
                random -> Optional.empty());
    }
    private static FlashSaleEntry generated(String id, String name, int quantity, long low, long medium, long high,
            GeneratedFactory factory) {
        return new FlashSaleEntry(id, Component.literal(name), quantity, low, medium, high, true, random -> {
            ItemStack[] result = factory.create(random);
            return result.length == 0 ? Optional.empty() : Optional.of(List.of(result));
        });
    }

    @FunctionalInterface private interface GeneratedFactory { ItemStack[] create(net.minecraft.util.RandomSource random); }
}
