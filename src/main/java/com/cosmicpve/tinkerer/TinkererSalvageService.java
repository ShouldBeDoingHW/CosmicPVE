package com.cosmicpve.tinkerer;

import com.cosmicpve.equipment.enchantment.CosmicDustService;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.registry.ModDataComponents;
import java.util.EnumMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public final class TinkererSalvageService {
    public record Result(Map<CosmicEnchantmentTier, Integer> dust, List<ItemStack> xpBottles,
                         int consumedBooks, int consumedGear) {
        public Result { dust = Map.copyOf(dust); xpBottles = List.copyOf(xpBottles); }
        public boolean succeeded() { return consumedBooks > 0 || consumedGear > 0; }
    }

    public Result confirm(Container input, int firstSlot, int endExclusive) {
        var totals = new EnumMap<CosmicEnchantmentTier, Integer>(CosmicEnchantmentTier.class);
        int consumed = 0;
        int consumedGear = 0;
        var bottles = new ArrayList<ItemStack>();
        var gear = new GearSalvageService();
        for (int slot = firstSlot; slot < endExclusive; slot++) {
            ItemStack stack = input.getItem(slot);
            var tier = CosmicDustService.bookTier(stack);
            var data = stack.get(ModDataComponents.COSMIC_ENCHANT_BOOK.get());
            if (tier.isPresent() && data != null) {
                totals.merge(tier.orElseThrow(), CosmicDustService.yield(data.level(), data.successRate()) * stack.getCount(), Integer::sum);
                consumed += stack.getCount();
            } else {
                var xp = gear.storedXp(stack);
                if (xp.isPresent()) { bottles.add(gear.bottle(xp.getAsLong())); consumedGear++; }
            }
        }
        if (consumed == 0 && consumedGear == 0) return new Result(Map.of(), List.of(), 0, 0);
        for (int slot = firstSlot; slot < endExclusive; slot++) {
            ItemStack stack = input.getItem(slot);
            if (CosmicDustService.bookTier(stack).isPresent() || gear.storedXp(stack).isPresent()) {
                input.setItem(slot, ItemStack.EMPTY);
            }
        }
        return new Result(totals, bottles, consumed, consumedGear);
    }
}
