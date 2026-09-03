package com.cosmicpve.upgrade;

import com.cosmicpve.data.attachment.PlayerUpgradeData;
import com.cosmicpve.economy.MoneyAmount;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerUpgradeFoundationTest {
    @Test
    void exactTracksCostsEffectsAndNamesAreCanonical() {
        assertArrayEquals(new String[]{"More Damage!", "Less Damage!", "Dungeon Mastery!", "Slow Mo!", "Safety Net!", "Pure RNG!"},
                java.util.Arrays.stream(PlayerUpgrade.values()).map(PlayerUpgrade::displayName).toArray(String[]::new));
        assertUpgrade(PlayerUpgrade.MORE_DAMAGE,
                new int[]{4, 8, 12, 16, 20}, new long[]{1, 2, 3, 4, 5}, new double[]{.01, .02, .03, .04, .05});
        assertUpgrade(PlayerUpgrade.LESS_DAMAGE,
                new int[]{4, 8, 12, 16, 20}, new long[]{1, 2, 3, 4, 5}, new double[]{.01, .02, .03, .04, .05});
        assertUpgrade(PlayerUpgrade.DUNGEON_MASTERY,
                new int[]{16, 32, 48, 64}, new long[]{5, 10, 15, 20}, new double[]{.025, .05, .075, .10});
        assertUpgrade(PlayerUpgrade.SAFETY_NET,
                new int[]{12, 24, 36, 48, 60}, new long[]{3, 6, 9, 12, 15}, new double[]{2, 4, 6, 8, 10});
        assertUpgrade(PlayerUpgrade.PURE_RNG, new int[]{32}, new long[]{8}, new double[]{2});

        assertEquals(3, PlayerUpgrade.SLOW_MO.maxTier());
        assertArrayEquals(new int[]{10, 20, 30}, costs(PlayerUpgrade.SLOW_MO));
        assertEquals("$2,500,000", MoneyAmount.format(PlayerUpgrade.SLOW_MO.moneyCost(1)));
        assertEquals("$5,000,000", MoneyAmount.format(PlayerUpgrade.SLOW_MO.moneyCost(2)));
        assertEquals("$7,500,000", MoneyAmount.format(PlayerUpgrade.SLOW_MO.moneyCost(3)));
        assertEquals(20, PlayerUpgrade.SLOW_MO.effect(1));
        assertEquals(40, PlayerUpgrade.SLOW_MO.effect(2));
        assertEquals(60, PlayerUpgrade.SLOW_MO.effect(3));
    }

    @Test
    void safetyNetReducesOnlyEffectiveDestroyRateAndFloorsAtZero() {
        assertEquals(44, SafetyNetService.effectiveDestroyRate(50, 3));
        assertEquals(90, SafetyNetService.effectiveDestroyRate(100, 5));
        assertEquals(0, SafetyNetService.effectiveDestroyRate(7, 5));
        assertEquals(75, SafetyNetService.effectiveDestroyRate(75, 0));
    }

    @Test
    void dungeonMasteryUsesExactNonLuckModifiedBoundaries() {
        assertEquals(.025, PlayerUpgrade.DUNGEON_MASTERY.effect(1));
        assertEquals(.05, PlayerUpgrade.DUNGEON_MASTERY.effect(2));
        assertEquals(.075, PlayerUpgrade.DUNGEON_MASTERY.effect(3));
        assertEquals(.10, PlayerUpgrade.DUNGEON_MASTERY.effect(4));
    }

    @Test
    void attachmentRoundTripsBankAndTiers() {
        var value = new PlayerUpgradeData(1, 37, Map.of(PlayerUpgrade.SAFETY_NET.id(), 5));
        var json = PlayerUpgradeData.CODEC.codec().encodeStart(JsonOps.INSTANCE, value).getOrThrow();
        assertEquals(value, PlayerUpgradeData.CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow());
    }

    @Test
    void upgradeCrystalPresentationIsCanonical() {
        var name = UpgradeCrystalItem.displayName();
        assertEquals("Upgrade Crystal", name.getString());
        assertTrue(name.getStyle().isBold());
        assertTrue(name.getStyle().isItalic());
        assertEquals(0x7EF2E0, name.getStyle().getColor().getValue());
    }

    private static void assertUpgrade(PlayerUpgrade upgrade, int[] crystalCosts, long[] moneyMillions, double[] effects) {
        assertEquals(crystalCosts.length, upgrade.maxTier());
        for (int tier = 1; tier <= crystalCosts.length; tier++) {
            assertEquals(crystalCosts[tier - 1], upgrade.crystalCost(tier));
            assertEquals(moneyMillions[tier - 1] * 100_000_000L, upgrade.moneyCost(tier));
            assertEquals(effects[tier - 1], upgrade.effect(tier));
        }
    }

    private static int[] costs(PlayerUpgrade upgrade) {
        int[] costs = new int[upgrade.maxTier()];
        for (int tier = 1; tier <= upgrade.maxTier(); tier++) costs[tier - 1] = upgrade.crystalCost(tier);
        return costs;
    }
}
