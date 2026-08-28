package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.data.component.CosmicDustData;
import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

public final class CosmicDustService {
    public enum ApplicationOutcome { SUCCESS, REJECTED_INVALID, REJECTED_RARITY, REJECTED_CAPPED, REJECTED_STALE }
    public record ApplicationResult(ApplicationOutcome outcome, int consumed, int successBefore, int successAfter) {}

    public static int yield(int level, int successRate) {
        if (level < 1 || successRate < 1 || successRate > 100) throw new IllegalArgumentException("Invalid book values");
        return Math.min(10, 1 + level + successRate / 10);
    }

    public static Optional<CosmicEnchantmentTier> bookTier(ItemStack stack) {
        if (!stack.is(ModItems.COSMIC_ENCHANTMENT_BOOK.get())) return Optional.empty();
        var book = stack.get(ModDataComponents.COSMIC_ENCHANT_BOOK.get());
        return book == null ? Optional.empty() : CosmicEnchantmentSpecs.find(book.enchantmentId()).map(CosmicEnchantmentSpec::tier);
    }

    public static ItemStack dust(CosmicEnchantmentTier tier, int count) {
        if (count < 1) throw new IllegalArgumentException("Dust count must be positive");
        ItemStack stack = new ItemStack(ModItems.COSMIC_DUST.get(), count);
        stack.set(ModDataComponents.COSMIC_DUST.get(), new CosmicDustData(CosmicDustData.CURRENT_DATA_VERSION, tier));
        return stack;
    }

    public ApplicationResult apply(ItemStack dust, ItemStack targetSnapshot, ItemStack target) {
        if (targetSnapshot != target) return new ApplicationResult(ApplicationOutcome.REJECTED_STALE, 0, 0, 0);
        var dustData = dust.get(ModDataComponents.COSMIC_DUST.get());
        var book = target.get(ModDataComponents.COSMIC_ENCHANT_BOOK.get());
        if (!dust.is(ModItems.COSMIC_DUST.get()) || dustData == null || book == null
                || !target.is(ModItems.COSMIC_ENCHANTMENT_BOOK.get()) || dust.isEmpty())
            return new ApplicationResult(ApplicationOutcome.REJECTED_INVALID, 0, book == null ? 0 : book.successRate(), book == null ? 0 : book.successRate());
        var tier = CosmicEnchantmentSpecs.find(book.enchantmentId()).map(CosmicEnchantmentSpec::tier).orElse(null);
        if (tier == null) return new ApplicationResult(ApplicationOutcome.REJECTED_INVALID, 0, book.successRate(), book.successRate());
        if (tier != dustData.tier()) return new ApplicationResult(ApplicationOutcome.REJECTED_RARITY, 0, book.successRate(), book.successRate());
        int cap = tier == CosmicEnchantmentTier.MASTERY ? 50 : 100;
        if (book.successRate() >= cap) return new ApplicationResult(ApplicationOutcome.REJECTED_CAPPED, 0, book.successRate(), book.successRate());
        int consumed = Math.min(dust.getCount(), cap - book.successRate());
        int after = book.successRate() + consumed;
        target.set(ModDataComponents.COSMIC_ENCHANT_BOOK.get(), new CosmicEnchantmentBookData(
                book.dataVersion(), book.enchantmentId(), book.level(), after, book.destroyRate()));
        dust.shrink(consumed);
        return new ApplicationResult(ApplicationOutcome.SUCCESS, consumed, book.successRate(), after);
    }
}
