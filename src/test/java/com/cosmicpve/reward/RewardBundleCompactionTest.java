package com.cosmicpve.reward;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.equipment.enchantment.UnexaminedBooks;
import com.cosmicpve.registry.ModDataComponents;
import org.junit.jupiter.api.Test;

class RewardBundleCompactionTest {
    @Test void identicalStackableRewardsBecomeOneLegalStack() {
        var generated = java.util.stream.IntStream.range(0, 16)
                .mapToObj(ignored -> new ItemStack(Items.GOLDEN_APPLE)).toList();
        var compacted = RewardTableService.compact(generated);
        assertEquals(1, compacted.size());
        assertEquals(16, compacted.getFirst().getCount());
    }

    @Test void overflowSplitsAtLegalMaximum() {
        var generated = java.util.stream.IntStream.range(0, 70)
                .mapToObj(ignored -> new ItemStack(Items.APPLE)).toList();
        var compacted = RewardTableService.compact(generated);
        assertEquals(List.of(64, 6), compacted.stream().map(ItemStack::getCount).toList());
    }

    @Test void identicalUnexaminedBooksCompactWithoutPreRollingActualBookData() {
        var compacted = RewardTableService.compact(List.of(
                UnexaminedBooks.create(CosmicEnchantmentTier.ELITE),
                UnexaminedBooks.create(CosmicEnchantmentTier.ELITE)));
        assertEquals(1, compacted.size());
        assertEquals(2, compacted.getFirst().getCount());
        assertEquals(CosmicEnchantmentTier.ELITE,
                compacted.getFirst().get(ModDataComponents.UNEXAMINED_BOOK.get()).tier());
        assertEquals(null, compacted.getFirst().get(ModDataComponents.COSMIC_ENCHANT_BOOK.get()));
    }
}
