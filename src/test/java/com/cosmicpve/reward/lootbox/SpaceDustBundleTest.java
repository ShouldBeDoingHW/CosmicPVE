package com.cosmicpve.reward.lootbox;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.registry.ModDataComponents;
import java.util.List;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class SpaceDustBundleTest {
    @Test void weightsAndBoundariesAreCanonical() {
        assertEquals(92, SpaceDustBundleRewards.TOTAL_WEIGHT);
        assertEquals(List.of(20,18,16,14,12,8,4),
                SpaceDustBundleRewards.WEIGHTS.stream().map(SpaceDustBundleRewards.WeightedTier::weight).toList());
        assertEquals(CosmicEnchantmentTier.SIMPLE, SpaceDustBundleRewards.select(0));
        assertEquals(CosmicEnchantmentTier.MASTERY, SpaceDustBundleRewards.select(91));
    }
    @Test void everyOpeningProducesThreeIndependentRealDustStacks() {
        var rewards = SpaceDustBundleRewards.roll(RandomSource.create(991L));
        assertEquals(3, rewards.size());
        rewards.forEach(stack -> {
            assertNotNull(stack.get(ModDataComponents.COSMIC_DUST.get()));
            assertTrue(stack.getCount() >= 1 && stack.getCount() <= 10);
        });
    }
}
