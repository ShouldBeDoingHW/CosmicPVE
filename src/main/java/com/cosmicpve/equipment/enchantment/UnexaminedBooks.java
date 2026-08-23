package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import com.cosmicpve.data.component.UnexaminedBookData;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.world.item.ItemStack;

/** Central construction and rate-modifier ownership for generic Unexamined Books. */
public final class UnexaminedBooks {
    private static final BookOpeningRateService RATE_SERVICE = new BookOpeningRateService();
    private static final UnexaminedBookOpeningService OPENING_SERVICE =
            new UnexaminedBookOpeningService(RATE_SERVICE);

    private UnexaminedBooks() {}

    public static ItemStack create(CosmicEnchantmentTier tier) {
        ItemStack stack = new ItemStack(ModItems.UNEXAMINED_ENCHANTMENT_BOOK.get());
        stack.set(ModDataComponents.UNEXAMINED_BOOK.get(),
                new UnexaminedBookData(UnexaminedBookData.CURRENT_DATA_VERSION, tier));
        return stack;
    }

    public static ItemStack revealed(UnexaminedBookOpeningService.Result result) {
        ItemStack stack = new ItemStack(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        stack.set(ModDataComponents.COSMIC_ENCHANT_BOOK.get(), new CosmicEnchantmentBookData(
                CosmicEnchantmentBookData.CURRENT_DATA_VERSION,
                result.enchantment().id(), result.level(),
                result.rates().successRate(), result.rates().destroyRate()));
        return stack;
    }

    public static UnexaminedBookOpeningService openingService() {
        return OPENING_SERVICE;
    }

    public static void registerRateModifier(BookOpeningRateModifier modifier) {
        RATE_SERVICE.register(modifier);
    }
}
