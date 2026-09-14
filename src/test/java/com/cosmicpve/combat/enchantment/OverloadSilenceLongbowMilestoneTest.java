package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.proc.ProcChance;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.equipment.enchantment.HeroicEnchantments;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.reward.lootbox.CosmicEnchantmentTableRewards;
import com.cosmicpve.reward.lootbox.HeroicCosmicEnchantmentTableRewards;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class OverloadSilenceLongbowMilestoneTest {
    @Test void registrationAndApplicabilityAreCanonical() {
        assertSpec(CosmicEnchantmentSpecs.OVERLOAD, 3, CosmicEnchantmentTier.LEGENDARY, "chestplate");
        assertSpec(CosmicEnchantmentSpecs.GODLY_OVERLOAD, 3, CosmicEnchantmentTier.HEROIC, "chestplate");
        assertSpec(CosmicEnchantmentSpecs.SILENCE, 4, CosmicEnchantmentTier.LEGENDARY, "all_weapons");
        assertSpec(CosmicEnchantmentSpecs.LONGBOW, 5, CosmicEnchantmentTier.LEGENDARY, "bow");
        assertEquals(ModEnchantments.GODLY_OVERLOAD.identifier(),
                HeroicEnchantments.heroicFor(ModEnchantments.OVERLOAD.identifier()).orElseThrow());
        assertEquals(ModEnchantments.OVERLOAD.identifier(),
                HeroicEnchantments.ordinaryFor(ModEnchantments.GODLY_OVERLOAD.identifier()).orElseThrow());
    }

    @Test void overloadValuesUseHpAndHeroicReplacesRatherThanStacks() {
        assertArrayEquals(new double[] {1, 2, 3},
                java.util.stream.IntStream.rangeClosed(1, 3).mapToDouble(OverloadBehavior::ordinaryBonus).toArray());
        assertArrayEquals(new double[] {4, 5, 6},
                java.util.stream.IntStream.rangeClosed(1, 3).mapToDouble(OverloadBehavior::godlyBonus).toArray());
        assertNotEquals(OverloadBehavior.MODIFIER_ID, ModEnchantments.OVERLOAD.identifier());
    }

    @Test void silenceChanceIsLuckRelativeAndWeaponFamiliesAreExact() {
        assertArrayEquals(new double[] {.11, .12, .13, .14},
                java.util.stream.IntStream.rangeClosed(1, 4).mapToDouble(SilenceBehavior::chance).toArray(), 1e-12);
        assertEquals(.168, ProcChance.calculate(SilenceBehavior.chance(4),
                List.of(LuckBehavior.chanceMultiplier(20))), 1e-12);
        assertTrue(SilenceBehavior.eligibleWeapon(AttackCategory.PROJECTILE, new ItemStack(Items.BOW)));
        assertTrue(SilenceBehavior.eligibleWeapon(AttackCategory.PROJECTILE, new ItemStack(Items.CROSSBOW)));
        assertFalse(SilenceBehavior.eligibleWeapon(AttackCategory.MELEE, new ItemStack(Items.BOW)));
        assertEquals(60, SilenceBehavior.DURATION_TICKS);
        assertTrue(SilenceBehavior.armorEnchantment(ModEnchantments.ANGELIC.identifier()));
        assertTrue(SilenceBehavior.armorEnchantment(ModEnchantments.GODLY_OVERLOAD.identifier()));
        assertFalse(SilenceBehavior.armorEnchantment(ModEnchantments.SILENCE.identifier()));
    }

    @Test void longbowValuesAndSourceTargetFamiliesAreExact() {
        assertArrayEquals(new double[] {.02, .04, .06, .08, .10},
                java.util.stream.IntStream.rangeClosed(1, 5).mapToDouble(LongbowBehavior::bonus).toArray(), 1e-12);
        assertTrue(LongbowBehavior.isBow(new ItemStack(Items.BOW)));
        assertFalse(LongbowBehavior.isBow(new ItemStack(Items.CROSSBOW)));
        assertTrue(LongbowBehavior.targetHoldsBow(new ItemStack(Items.BOW), ItemStack.EMPTY));
        assertTrue(LongbowBehavior.targetHoldsBow(ItemStack.EMPTY, new ItemStack(Items.BOW)));
        assertFalse(LongbowBehavior.targetHoldsBow(new ItemStack(Items.CROSSBOW), ItemStack.EMPTY));
        assertFalse(LongbowBehavior.targetHoldsBow(ItemStack.EMPTY, ItemStack.EMPTY));
    }

    @Test void ordinaryTableStaysStarredOnlyWhileHeroicPoolExpandsGenerically() {
        assertEquals(18, CosmicEnchantmentTableRewards.POOL.size());
        assertFalse(CosmicEnchantmentTableRewards.POOL.contains(ModEnchantments.OVERLOAD));
        assertFalse(CosmicEnchantmentTableRewards.POOL.contains(ModEnchantments.SILENCE));
        assertFalse(CosmicEnchantmentTableRewards.POOL.contains(ModEnchantments.LONGBOW));
        assertTrue(HeroicCosmicEnchantmentTableRewards.POOL.contains(ModEnchantments.GODLY_OVERLOAD.identifier()));
        assertEquals(HeroicCosmicEnchantmentTableRewards.POOL.size()
                * HeroicCosmicEnchantmentTableRewards.SUCCESS.size(),
                HeroicCosmicEnchantmentTableRewards.ENTRY_COUNT);
    }

    private static void assertSpec(com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpec spec,
            int max, CosmicEnchantmentTier tier, String applicability) {
        assertEquals(max, spec.maxLevel());
        assertEquals(tier, spec.tier());
        assertEquals(applicability, spec.equipmentApplicability());
    }
}
