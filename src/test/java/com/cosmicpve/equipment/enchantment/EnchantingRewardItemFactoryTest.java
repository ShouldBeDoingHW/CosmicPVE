package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class EnchantingRewardItemFactoryTest {
    @Test void exactRewardPercentagesUseCanonicalTypedData() {
        var factory = new EnchantingRewardItemFactory();
        assertEquals(75, factory.blackScroll(75).get(ModDataComponents.BLACK_SCROLL.get()).returnedSuccessRate());
        for (OrbType type : OrbType.values()) {
            var orb = factory.orb(type, 50, RandomSource.create(4));
            var data = orb.get(ModDataComponents.ENCHANTMENT_ORB.get());
            assertEquals(50, data.successRate());
            assertTrue(data.destroyRate() >= 1 && data.destroyRate() <= 100);
        }
    }
}
