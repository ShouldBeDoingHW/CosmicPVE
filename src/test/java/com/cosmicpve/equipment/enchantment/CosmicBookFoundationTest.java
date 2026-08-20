package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CosmicBookFoundationTest {
    @Test void bookDataEnforcesOrdinaryRanges() {
        assertThrows(IllegalArgumentException.class, () -> new CosmicEnchantmentBookData(1, com.cosmicpve.CosmicPVE.id("execute"),1,0,1));
        assertThrows(IllegalArgumentException.class, () -> new CosmicEnchantmentBookData(1, com.cosmicpve.CosmicPVE.id("execute"),1,1,101));
    }
    @Test void masteryRatesComeFromTierMetadata() {
        assertTrue(CosmicEnchantmentTier.MASTERY.allowsRates(49,51));
        assertFalse(CosmicEnchantmentTier.MASTERY.allowsRates(50,51));
        assertFalse(CosmicEnchantmentTier.MASTERY.allowsRates(49,50));
        assertTrue(CosmicEnchantmentTier.ELITE.allowsRates(100,1));
    }
    @Test void deterministicSuccessNeverRollsDestroy() {
        var calls=new AtomicInteger();
        var decision=CosmicBookRollResolver.resolve(100,100,()->{calls.incrementAndGet();return 1;});
        assertEquals(CosmicBookRollResolver.Outcome.SUCCESS,decision.outcome()); assertEquals(0,calls.get());
        assertTrue(decision.destroyRoll().isEmpty());
    }
    @Test void failureRollsDestroyExactlyOnce() {
        int[] rolls={100,50}; var index=new AtomicInteger();
        var decision=CosmicBookRollResolver.resolve(1,50,()->rolls[index.getAndIncrement()]);
        assertEquals(CosmicBookRollResolver.Outcome.FAILED_DESTRUCTIVE,decision.outcome()); assertEquals(2,index.get());
    }
    @Test void fiveSlotSeamAllowsUpgradeButRejectsSixth() {
        assertEquals(5,CustomEnchantCapacityService.BASE_CAPACITY);
        assertFalse(CustomEnchantCapacityService.canAdd(5,5,false));
        assertTrue(CustomEnchantCapacityService.canAdd(5,5,true));
    }
    @Test void genericBookAndCrystalForceGlintAndCrystalIsNonStackable() {
        assertEquals(1,com.cosmicpve.equipment.armor.ArmorSetCrystalItem.MAX_STACK_SIZE);
        assertTrue(com.cosmicpve.equipment.armor.ArmorSetCrystalItem.FORCE_GLINT);
        assertTrue(CosmicEnchantmentBookItem.FORCE_GLINT);
    }
}
