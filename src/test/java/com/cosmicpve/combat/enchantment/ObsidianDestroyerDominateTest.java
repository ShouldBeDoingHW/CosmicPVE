package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.proc.ProcChance;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.equipment.enchantment.MiningEnchantmentService;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class ObsidianDestroyerDominateTest {
    @Test
    void obsidianDestroyerHasCanonicalMetadataAndExactAdditivePoints() {
        assertEquals(CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentSpecs.OBSIDIAN_DESTROYER.tier());
        assertEquals("pickaxe", CosmicEnchantmentSpecs.OBSIDIAN_DESTROYER.equipmentApplicability());
        assertEquals(4, CosmicEnchantmentSpecs.OBSIDIAN_DESTROYER.maxLevel());
        assertTrue(CosmicEnchantmentSpecs.OBSIDIAN_DESTROYER.tier().extractableByBlackScroll());

        assertEquals(0.0, MiningEnchantmentService.obsidianDestroyerBonus(Blocks.OBSIDIAN.defaultBlockState(), 0));
        assertEquals(2.0, MiningEnchantmentService.obsidianDestroyerBonus(Blocks.OBSIDIAN.defaultBlockState(), 1));
        assertEquals(4.0, MiningEnchantmentService.obsidianDestroyerBonus(Blocks.OBSIDIAN.defaultBlockState(), 2));
        assertEquals(6.0, MiningEnchantmentService.obsidianDestroyerBonus(Blocks.OBSIDIAN.defaultBlockState(), 3));
        assertEquals(8.0, MiningEnchantmentService.obsidianDestroyerBonus(Blocks.OBSIDIAN.defaultBlockState(), 4));
        assertEquals(0.0, MiningEnchantmentService.obsidianDestroyerBonus(Blocks.CRYING_OBSIDIAN.defaultBlockState(), 4));
        assertEquals(0.0, MiningEnchantmentService.obsidianDestroyerBonus(Blocks.STONE.defaultBlockState(), 4));

        // BreakSpeed's current value already contains vanilla tool/Efficiency contributions.
        assertEquals(20.0F, MiningEnchantmentService.applyObsidianDestroyer(
                12.0F, Blocks.OBSIDIAN.defaultBlockState(), 4));
    }

    @Test
    void dominateHasCanonicalMetadataChanceReductionAndDuration() {
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.DOMINATE.tier());
        assertEquals("bow_or_crossbow", CosmicEnchantmentSpecs.DOMINATE.equipmentApplicability());
        assertEquals(4, CosmicEnchantmentSpecs.DOMINATE.maxLevel());
        assertTrue(CosmicEnchantmentSpecs.DOMINATE.tier().extractableByBlackScroll());
        assertTrue(CosmicEnchantmentSpecs.find(ModEnchantments.DOMINATE.identifier()).isPresent());

        for (int level = 1; level <= 4; level++) {
            assertEquals(0.20, DominateBehavior.chance(level), 1.0E-12);
            assertEquals(0.03 * level, DominateBehavior.reduction(level), 1.0E-12);
            assertEquals(20 * level, DominateBehavior.durationTicks(level));
        }
        assertEquals(0.24, ProcChance.calculate(
                DominateBehavior.chance(4), List.of(LuckBehavior.chanceMultiplier(20))), 1.0E-12);
    }

    @Test
    void dominateRequiresProjectileAttributionAndNeverAHeldWeaponMeleeHit() {
        assertTrue(DominateBehavior.qualifies(AttackCategory.PROJECTILE, true));
        assertFalse(DominateBehavior.qualifies(AttackCategory.MELEE, true));
        assertFalse(DominateBehavior.qualifies(AttackCategory.PROJECTILE, false));
    }

    @Test
    void dominateIsOneStrongestRefreshableHarmfulState() {
        assertEquals(1, DominateBehavior.strongestLevel(1, 1));
        assertEquals(4, DominateBehavior.strongestLevel(4, 1));
        assertEquals(4, DominateBehavior.strongestLevel(1, 4));
        assertEquals(80, DominateBehavior.durationTicks(DominateBehavior.strongestLevel(4, 1)));
        assertEquals(MobEffectCategory.HARMFUL, new DominateMobEffect().getCategory());
    }

    @Test
    void dominateContributesOnlyToOrdinaryOutgoingDamage() {
        var ordinary = DominateBehavior.contribution(DamageChannel.ORDINARY, 4);
        assertEquals(1, ordinary.size());
        assertEquals(ModEnchantments.DOMINATE.identifier(), ordinary.getFirst().sourceId());
        assertEquals(-0.12, ordinary.getFirst().bonus(), 1.0E-12);
        assertTrue(DominateBehavior.contribution(DamageChannel.TRUE, 4).isEmpty());
        assertTrue(DominateBehavior.contribution(DamageChannel.ORDINARY, 0).isEmpty());
    }

    @Test
    void ordinaryEnchantmentRegistryNowContainsFiftyTwoEntries() {
        assertEquals(85, CosmicEnchantmentSpecs.ALL.size());
        assertTrue(CosmicEnchantmentSpecs.find(ModEnchantments.OBSIDIAN_DESTROYER.identifier()).isPresent());
        assertTrue(CosmicEnchantmentSpecs.find(ModEnchantments.DOMINATE.identifier()).isPresent());
    }
}
