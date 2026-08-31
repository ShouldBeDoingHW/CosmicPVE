package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Selection-based, failure-free extraction mutation used only after a menu click is revalidated. */
public final class EnchantedBlackScrollExtractionService {
    public List<VisibleCosmicEnchantmentOrdering.Entry> candidates(ItemStack target) {
        return VisibleCosmicEnchantmentOrdering.eligibleBlackScroll(target);
    }

    public ItemStack extract(ItemStack target, ItemStack scroll,
                             VisibleCosmicEnchantmentOrdering.Entry chosen, int destroyRate) {
        var data = scroll.get(ModDataComponents.ENCHANTED_BLACK_SCROLL.get());
        if (!scroll.is(ModItems.ENCHANTED_BLACK_SCROLL.get()) || data == null || destroyRate < 1 || destroyRate > 100) {
            return ItemStack.EMPTY;
        }
        var current = candidates(target).stream().filter(entry -> entry.id().equals(chosen.id())
                && entry.level() == chosen.level()).findFirst();
        if (current.isEmpty()) return ItemStack.EMPTY;
        var selected = current.orElseThrow();
        EnchantmentHelper.updateEnchantments(target,
                mutable -> mutable.removeIf(holder -> holder.equals(selected.holder())));
        scroll.shrink(1);
        ItemStack book = new ItemStack(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        book.set(ModDataComponents.COSMIC_ENCHANT_BOOK.get(), new CosmicEnchantmentBookData(
                CosmicEnchantmentBookData.CURRENT_DATA_VERSION, selected.id(), selected.level(),
                data.returnedSuccessRate(), destroyRate));
        return book;
    }
}
