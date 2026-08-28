package com.cosmicpve.tinkerer;

import com.cosmicpve.equipment.enchantment.CosmicDustService;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.registry.ModDataComponents;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public final class TinkererSalvageService {
    public record Result(Map<CosmicEnchantmentTier, Integer> dust, int consumedBooks) {
        public Result { dust = Map.copyOf(dust); }
        public boolean succeeded() { return consumedBooks > 0; }
    }

    public Result confirm(Container input, int firstSlot, int endExclusive) {
        var totals = new EnumMap<CosmicEnchantmentTier, Integer>(CosmicEnchantmentTier.class);
        int consumed = 0;
        for (int slot = firstSlot; slot < endExclusive; slot++) {
            ItemStack stack = input.getItem(slot);
            var tier = CosmicDustService.bookTier(stack);
            var data = stack.get(ModDataComponents.COSMIC_ENCHANT_BOOK.get());
            if (tier.isEmpty() || data == null) continue;
            totals.merge(tier.orElseThrow(), CosmicDustService.yield(data.level(), data.successRate()) * stack.getCount(), Integer::sum);
            consumed += stack.getCount();
        }
        if (consumed == 0) return new Result(Map.of(), 0);
        for (int slot = firstSlot; slot < endExclusive; slot++) {
            if (CosmicDustService.bookTier(input.getItem(slot)).isPresent()) input.setItem(slot, ItemStack.EMPTY);
        }
        return new Result(totals, consumed);
    }
}
