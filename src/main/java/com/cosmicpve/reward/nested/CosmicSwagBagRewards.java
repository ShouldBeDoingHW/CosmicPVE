package com.cosmicpve.reward.nested;

import com.cosmicpve.economy.Banknotes;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.equipment.enchantment.EnchantingRewardItemFactory;
import com.cosmicpve.equipment.enchantment.OrbType;
import com.cosmicpve.equipment.enchantment.UnexaminedBooks;
import com.cosmicpve.registry.ModItems;
import com.cosmicpve.tinkerer.GearSalvageService;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class CosmicSwagBagRewards {
    private static final EnchantingRewardItemFactory ENCHANTING = new EnchantingRewardItemFactory();
    public static final List<WeightedNestedRewards.Row> ROWS = build();
    private CosmicSwagBagRewards() {}

    private static List<WeightedNestedRewards.Row> build() {
        var rows = new ArrayList<WeightedNestedRewards.Row>();
        for (var tier : List.of(CosmicEnchantmentTier.SIMPLE, CosmicEnchantmentTier.UNIQUE,
                CosmicEnchantmentTier.ELITE, CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentTier.LEGENDARY))
            for (int count = 1; count <= 3; count++) {
                int quantity = count;
                rows.add(new WeightedNestedRewards.Row("unexamined_" + tier.serializedName() + "_" + count, 5,
                        random -> unexamined(tier, quantity), unexamined(tier, quantity)));
            }
        for (var tier : List.of(CosmicEnchantmentTier.HEROIC, CosmicEnchantmentTier.MASTERY))
            rows.add(new WeightedNestedRewards.Row("unexamined_" + tier.serializedName() + "_1", 5,
                    random -> UnexaminedBooks.create(tier), UnexaminedBooks.create(tier)));
        rows.add(new WeightedNestedRewards.Row("money_note_1_to_1000000", 25,
                random -> Banknotes.create(100L * (random.nextInt(1_000_000) + 1)),
                Banknotes.create(100_000_000L)));
        rows.add(item("personal_vault_unlock", 30, ModItems.PERSONAL_VAULT_UNLOCK.get(), 1));
        for (int xp : List.of(3_000, 5_000, 7_500, 10_000))
            rows.add(new WeightedNestedRewards.Row("xp_bottle_" + xp, 15,
                    random -> new GearSalvageService().bottle(xp), new GearSalvageService().bottle(xp)));
        rows.add(item("transmog_scroll", 25, ModItems.TRANSMOG_SCROLL.get(), 1));
        rows.add(item("white_scroll", 50, ModItems.WHITE_SCROLL.get(), 1));
        rows.add(item("repair_scroll_1", 25, ModItems.REPAIR_SCROLL.get(), 1));
        rows.add(item("repair_scroll_2", 25, ModItems.REPAIR_SCROLL.get(), 2));
        rows.add(orb(OrbType.ARMOR));
        rows.add(orb(OrbType.WEAPON));
        return List.copyOf(rows);
    }

    private static ItemStack unexamined(CosmicEnchantmentTier tier, int count) {
        ItemStack stack = UnexaminedBooks.create(tier);
        stack.setCount(count);
        return stack;
    }

    private static WeightedNestedRewards.Row item(String id, int weight, Item item, int count) {
        return new WeightedNestedRewards.Row(id, weight,
                random -> new ItemStack(item, count), new ItemStack(item, count));
    }

    private static WeightedNestedRewards.Row orb(OrbType type) {
        return new WeightedNestedRewards.Row(type.name().toLowerCase() + "_orb_50", 10,
                random -> ENCHANTING.orb(type, 50, random), ENCHANTING.orb(type, 50, 1));
    }

    public static List<ItemStack> preview() { return WeightedNestedRewards.previewMaximums(ROWS); }
}
