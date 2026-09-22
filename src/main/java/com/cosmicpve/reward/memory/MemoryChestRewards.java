package com.cosmicpve.reward.memory;

import com.cosmicpve.cosmiccrate.CosmicCrateSeason;
import com.cosmicpve.cosmiccrate.CosmicCrateSide;
import com.cosmicpve.cosmiccrate.SeasonalCosmicCrates;
import com.cosmicpve.spacechest.SpaceChestTier;
import com.cosmicpve.spacechest.SpaceChests;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public final class MemoryChestRewards {
    public record Entry(String id, int weight, Supplier<ItemStack> factory) {
        public Entry {
            if (id == null || id.isBlank() || weight < 1 || factory == null) throw new IllegalArgumentException("Invalid Memory Chest entry");
        }
        public ItemStack create() { return factory.get().copy(); }
    }

    public static final List<Entry> ENTRIES = List.of(
            new Entry("ultimate_space_chest", 36, () -> SpaceChests.create(SpaceChestTier.ULTIMATE)),
            new Entry("legendary_space_chest", 24, () -> SpaceChests.create(SpaceChestTier.LEGENDARY)),
            new Entry("mastery_space_chest", 12, () -> SpaceChests.create(SpaceChestTier.MASTERY)),
            half(CosmicCrateSeason.SPRING, CosmicCrateSide.LEFT),
            half(CosmicCrateSeason.SPRING, CosmicCrateSide.RIGHT),
            half(CosmicCrateSeason.SUMMER, CosmicCrateSide.LEFT),
            half(CosmicCrateSeason.SUMMER, CosmicCrateSide.RIGHT),
            half(CosmicCrateSeason.FALL, CosmicCrateSide.LEFT),
            half(CosmicCrateSeason.FALL, CosmicCrateSide.RIGHT),
            half(CosmicCrateSeason.WINTER, CosmicCrateSide.LEFT),
            half(CosmicCrateSeason.WINTER, CosmicCrateSide.RIGHT));

    private MemoryChestRewards() {}

    private static Entry half(CosmicCrateSeason season, CosmicCrateSide side) {
        return new Entry(season.id() + "_cosmic_crate_" + side.id() + "_half", 4,
                () -> SeasonalCosmicCrates.half(season, side));
    }

    public static int totalWeight() { return ENTRIES.stream().mapToInt(Entry::weight).sum(); }
    public static List<ItemStack> previewOutcomes() { return ENTRIES.stream().map(Entry::create).toList(); }
    public static ItemStack select(RandomSource random) {
        int roll = random.nextInt(totalWeight());
        for (Entry entry : ENTRIES) {
            roll -= entry.weight();
            if (roll < 0) return entry.create();
        }
        throw new IllegalStateException("Memory Chest selection exhausted");
    }
}
