package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.cosmicpve.CosmicPVE;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TransmogTooltipOrderingTest {
    @Test void exactCategoryLevelAndIdentifierOrderIsDeterministic() {
        var vanillaFirst = key(false, null, 2, "vanilla_first", 3);
        var vanillaSecond = key(false, null, 1, "vanilla_second", 7);
        var mastery = key(true, CosmicEnchantmentTier.MASTERY, 1, "mastery", 0);
        var legendaryOne = key(true, CosmicEnchantmentTier.LEGENDARY, 1, "legendary_z", 0);
        var legendaryThreeB = key(true, CosmicEnchantmentTier.LEGENDARY, 3, "legendary_b", 0);
        var legendaryThreeA = key(true, CosmicEnchantmentTier.LEGENDARY, 3, "legendary_a", 0);
        var ultimate = key(true, CosmicEnchantmentTier.ULTIMATE, 10, "ultimate", 0);
        var elite = key(true, CosmicEnchantmentTier.ELITE, 10, "elite", 0);
        var unique = key(true, CosmicEnchantmentTier.UNIQUE, 10, "unique", 0);
        var simple = key(true, CosmicEnchantmentTier.SIMPLE, 10, "simple", 0);
        var values = new ArrayList<>(List.of(simple, legendaryOne, vanillaSecond, elite, mastery,
                legendaryThreeB, ultimate, vanillaFirst, unique, legendaryThreeA));
        values.sort(TransmogTooltipOrdering.COMPARATOR);
        assertEquals(List.of(vanillaFirst, vanillaSecond, mastery, legendaryThreeA, legendaryThreeB,
                legendaryOne, ultimate, elite, unique, simple), values);
    }

    private static TransmogTooltipOrdering.Key key(boolean cosmic, CosmicEnchantmentTier tier,
            int level, String id, int originalIndex) {
        return new TransmogTooltipOrdering.Key(cosmic, tier, level, CosmicPVE.id(id), originalIndex);
    }
}
