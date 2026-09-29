package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.content.definition.reward.RewardDescriptor;
import com.cosmicpve.data.component.TrialTrinketType;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.reward.RewardGenerationContext;
import com.cosmicpve.reward.RewardGeneratorService;
import com.cosmicpve.trial.trinket.TrialTrinkets;
import java.util.EnumSet;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class TrialTrinketTierMetaRowsTest {
    @Test void everyTierMetaRowMaterializesFiveRealPhysicalFamilies() {
        for (int tier = 1; tier <= 3; tier++) {
            var seen = EnumSet.noneOf(TrialTrinketType.class);
            var random = RandomSource.create(90 + tier);
            for (int draw = 0; draw < 500; draw++) {
                var stack = TrialTrinkets.randomTier(tier, random);
                var data = stack.get(ModDataComponents.TRIAL_TRINKET.get());
                assertNotNull(data);
                assertSame(TrialTrinkets.item(data.type(), data.value()), stack.getItem());
                int expected = switch (data.type()) {
                    case TIME -> new int[]{1, 3, 5}[tier - 1];
                    case FAME -> new int[]{33, 66, 100}[tier - 1];
                    case SKIP, INSURANCE, MADNESS -> tier;
                };
                assertEquals(expected, data.value());
                seen.add(data.type());
            }
            assertEquals(EnumSet.allOf(TrialTrinketType.class), seen);
        }
    }

    @Test void repeatedMetaRowMaterializationRollsEachUnitIndependently() {
        var generator = new RewardGeneratorService();
        var context = new RewardGenerationContext(null, RandomSource.create(717), null);
        var row = new RewardDescriptor.RandomTrialTrinket(3);
        var seen = EnumSet.noneOf(TrialTrinketType.class);
        for (int quantity = 0; quantity < 100; quantity++) {
            var first = generator.generate(row, context).orElseThrow();
            var second = generator.generate(row, context).orElseThrow();
            seen.add(first.get(ModDataComponents.TRIAL_TRINKET.get()).type());
            seen.add(second.get(ModDataComponents.TRIAL_TRINKET.get()).type());
        }
        assertEquals(5, seen.size());
    }
}
