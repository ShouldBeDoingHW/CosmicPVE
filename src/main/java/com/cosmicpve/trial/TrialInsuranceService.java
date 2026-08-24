package com.cosmicpve.trial;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/** Selects complete acquired pot entries without replacement; delivery remains a separate transaction. */
public final class TrialInsuranceService {
    public List<TrialPotEntry> select(List<TrialPotEntry> pot, int insuranceLevel, RandomSource random) {
        if (insuranceLevel < 0 || insuranceLevel > 3) throw new IllegalArgumentException("Insurance must be in [0,3]");
        var remaining = new ArrayList<>(pot);
        var selected = new ArrayList<TrialPotEntry>();
        int count = Math.min(insuranceLevel, remaining.size());
        for (int i = 0; i < count; i++) selected.add(remaining.remove(random.nextInt(remaining.size())));
        return List.copyOf(selected);
    }

    public List<ItemStack> flattenedCopies(List<TrialPotEntry> selected) {
        return selected.stream().flatMap(entry -> entry.items().stream()).map(ItemStack::copy).toList();
    }
}
