package com.cosmicpve.loot;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.equipment.enchantment.UnexaminedBooks;
import java.util.Set;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Converts a generated player-progression Diamond armor result into exactly one Unexamined Book. */
public final class DiamondArmorLootReplacementService {
    public static final int SIMPLE_PERCENT = 65;
    private static final Set<Item> DIAMOND_ARMOR = Set.of(
            Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);

    public boolean isDiamondArmor(ItemStack stack) {
        return DIAMOND_ARMOR.contains(stack.getItem());
    }

    public ItemStack replace(ItemStack generated, RandomSource random) {
        if (!isDiamondArmor(generated)) return generated;
        return UnexaminedBooks.create(tierForRoll(random.nextInt(100)));
    }

    public static CosmicEnchantmentTier tierForRoll(int zeroToNinetyNine) {
        if (zeroToNinetyNine < 0 || zeroToNinetyNine >= 100) {
            throw new IllegalArgumentException("Loot replacement roll must be in [0, 99]");
        }
        return zeroToNinetyNine < SIMPLE_PERCENT
                ? CosmicEnchantmentTier.SIMPLE : CosmicEnchantmentTier.UNIQUE;
    }
}
