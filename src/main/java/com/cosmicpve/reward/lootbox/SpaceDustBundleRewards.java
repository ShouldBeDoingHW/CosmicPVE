package com.cosmicpve.reward.lootbox;

import com.cosmicpve.equipment.enchantment.CosmicDustService;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public final class SpaceDustBundleRewards {
    public record WeightedTier(CosmicEnchantmentTier tier, int weight) {}
    public static final List<WeightedTier> WEIGHTS = List.of(
            new WeightedTier(CosmicEnchantmentTier.SIMPLE, 20), new WeightedTier(CosmicEnchantmentTier.UNIQUE, 18),
            new WeightedTier(CosmicEnchantmentTier.ELITE, 16), new WeightedTier(CosmicEnchantmentTier.ULTIMATE, 14),
            new WeightedTier(CosmicEnchantmentTier.LEGENDARY, 12), new WeightedTier(CosmicEnchantmentTier.HEROIC, 8),
            new WeightedTier(CosmicEnchantmentTier.MASTERY, 4));
    public static final int TOTAL_WEIGHT = 92;
    private SpaceDustBundleRewards() {}
    public static ItemStack cosmeticPreview(RandomSource random) {
        return CosmicDustService.dust(WEIGHTS.get(random.nextInt(WEIGHTS.size())).tier(), random.nextInt(10) + 1);
    }
    public static List<ItemStack> roll(RandomSource random) {
        return java.util.stream.IntStream.range(0, 3).mapToObj(ignored ->
                CosmicDustService.dust(select(random.nextInt(TOTAL_WEIGHT)), random.nextInt(10) + 1)).toList();
    }
    static CosmicEnchantmentTier select(int roll) {
        if (roll < 0 || roll >= TOTAL_WEIGHT) throw new IllegalArgumentException("roll");
        int cursor = roll;
        for (var entry : WEIGHTS) { if (cursor < entry.weight()) return entry.tier(); cursor -= entry.weight(); }
        throw new IllegalStateException();
    }
}
