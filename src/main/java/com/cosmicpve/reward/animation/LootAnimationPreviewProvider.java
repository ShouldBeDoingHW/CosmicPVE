package com.cosmicpve.reward.animation;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface LootAnimationPreviewProvider {
    ItemStack next(RandomSource random);

    static LootAnimationPreviewProvider uniform(List<ItemStack> candidates) {
        List<ItemStack> safe = candidates.stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy).toList();
        if (safe.isEmpty()) throw new IllegalArgumentException("preview candidates must not be empty");
        return random -> safe.get(random.nextInt(safe.size())).copy();
    }

    static LootAnimationPreviewProvider weighted(List<WeightedPreview> candidates) {
        var safe = new ArrayList<>(candidates);
        int total = safe.stream().mapToInt(WeightedPreview::weight).sum();
        if (safe.isEmpty() || total <= 0 || safe.stream().anyMatch(entry -> entry.weight() <= 0 || entry.stack().isEmpty()))
            throw new IllegalArgumentException("weighted previews must be nonempty with positive weights");
        return random -> {
            int roll = random.nextInt(total);
            for (WeightedPreview entry : safe) if ((roll -= entry.weight()) < 0) return entry.stack().copy();
            throw new IllegalStateException("unreachable weighted preview selection");
        };
    }

    record WeightedPreview(ItemStack stack, int weight) {}
}
