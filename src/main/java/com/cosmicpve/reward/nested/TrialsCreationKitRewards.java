package com.cosmicpve.reward.nested;

import com.cosmicpve.data.component.TrialTrinketType;
import com.cosmicpve.registry.ModItems;
import com.cosmicpve.trial.trinket.TrialTrinkets;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class TrialsCreationKitRewards {
    public static final List<WeightedNestedRewards.Row> ROWS = List.of(
            portal(1), portal(2), portal(3),
            trinket(TrialTrinketType.SKIP, 1, 6), trinket(TrialTrinketType.SKIP, 2, 4),
            trinket(TrialTrinketType.SKIP, 3, 2),
            trinket(TrialTrinketType.TIME, 1, 6), trinket(TrialTrinketType.TIME, 3, 4),
            trinket(TrialTrinketType.TIME, 5, 2),
            trinket(TrialTrinketType.FAME, 33, 6), trinket(TrialTrinketType.FAME, 66, 4),
            trinket(TrialTrinketType.FAME, 100, 2),
            trinket(TrialTrinketType.MADNESS, 1, 6), trinket(TrialTrinketType.MADNESS, 2, 4),
            trinket(TrialTrinketType.MADNESS, 3, 2));

    private TrialsCreationKitRewards() {}

    private static WeightedNestedRewards.Row portal(int count) {
        return new WeightedNestedRewards.Row("trial_portal_" + count, 10,
                random -> new ItemStack(ModItems.TRIAL_PORTAL.get(), count),
                new ItemStack(ModItems.TRIAL_PORTAL.get(), count));
    }

    private static WeightedNestedRewards.Row trinket(TrialTrinketType type, int value, int weight) {
        ItemStack preview = TrialTrinkets.create(type, value, 1);
        return new WeightedNestedRewards.Row(type.name().toLowerCase() + "_" + value, weight,
                random -> TrialTrinkets.create(type, value, 1), preview);
    }

    public static List<ItemStack> preview() { return WeightedNestedRewards.previewMaximums(ROWS); }
}
