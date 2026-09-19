package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.combat.proc.ProcChance;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.equipment.enchantment.HeroicEnchantments;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import org.junit.jupiter.api.Test;

class CleaveCurseMilestoneTest {
    @Test void cleaveFamilyUsesExactLuckRelativeChancesDamageAndRecursionExclusions() {
        assertArrayEquals(new double[] {.01, .02, .03, .04, .05, .06, .07, .08},
                java.util.stream.IntStream.rangeClosed(1, 8).mapToDouble(CleaveBehavior::cleaveChance).toArray(), 1e-12);
        assertEquals(.08, CleaveBehavior.mightyChance(), 1e-12);
        assertEquals(1.8, CleaveBehavior.childDamage(10.0, 8, false), 1e-12);
        assertEquals(2.0, CleaveBehavior.childDamage(10.0, 8, true), 1e-12);
        assertEquals(.096, ProcChance.calculate(CleaveBehavior.cleaveChance(8),
                List.of(LuckBehavior.chanceMultiplier(20))), 1e-12);
        assertEquals(java.util.Set.of(ModEnchantments.CLEAVE.identifier(),
                ModEnchantments.MIGHTY_CLEAVE.identifier()), CleaveBehavior.RECURSION_EXCLUSIONS);
        assertEquals(6.0, CleaveBehavior.CLEAVE_RADIUS);
        assertEquals(7.0, CleaveBehavior.MIGHTY_RADIUS);
        assertEquals(4.0, CleaveBehavior.MIGHTY_HEAL_RADIUS);
        assertEquals(1.0F, CleaveBehavior.MIGHTY_HEAL_HP);
    }

    @Test void solitudeAddsLevelsInsideTheSingleEffectiveLuckFactor() {
        assertEquals(7.0, LuckBehavior.solitudeRadius(1));
        assertEquals(6.0, LuckBehavior.solitudeRadius(2));
        assertEquals(5.0, LuckBehavior.solitudeRadius(3));
        var combined = LuckBehavior.modifiersForLevels(23, 3);
        assertEquals(List.of(1.23), combined.chanceMultipliers());
        assertEquals(.123, ProcChance.calculate(.10, combined.chanceMultipliers()), 1e-12);
        assertNotEquals(.13, ProcChance.calculate(.10, combined.chanceMultipliers()), 1e-12);
        assertNotEquals(.1236, ProcChance.calculate(.10, combined.chanceMultipliers()), 1e-12);
    }

    @Test void curseUsesStrictThirtyPercentThresholdAndLevelScaledOrdinaryBonus() {
        for (int level = 1; level <= 5; level++) {
            assertFalse(CurseBehavior.state(6.0, 20.0, level, 0).active());
            var active = CurseBehavior.state(5.999, 20.0, level, 0);
            assertTrue(active.active());
            assertEquals(level * .01, active.outgoingBonus(), 1e-12);
            assertEquals(.10, active.knockbackResistance(), 1e-12);
            assertEquals(ModEnchantments.CURSE.identifier(), active.sourceId());
        }
        assertFalse(CurseBehavior.state(1.0, 20.0, 0, 0).active());
    }

    @Test void forbiddenCurseUsesLevelThresholdAndFixedBonusesAndReplacesOrdinaryState() {
        for (int level = 1; level <= 5; level++) {
            double threshold = CurseBehavior.forbiddenThreshold(level);
            assertEquals((30.0 + level) / 100.0, threshold, 1e-12);
            assertFalse(CurseBehavior.state(threshold * 20.0, 20.0, 5, level).active());
            var active = CurseBehavior.state(threshold * 20.0 - .001, 20.0, 5, level);
            assertTrue(active.active());
            assertEquals(.075, active.outgoingBonus(), 1e-12);
            assertEquals(.15, active.knockbackResistance(), 1e-12);
            assertEquals(ModEnchantments.FORBIDDEN_CURSE.identifier(), active.sourceId());
        }
    }

    @Test void registryMetadataAndHeroicReplacementPairsContainExactlyTheFiveAdditions() {
        assertEquals(78, CosmicEnchantmentSpecs.ALL.size());
        assertEquals(66, CosmicEnchantmentSpecs.ALL.stream()
                .filter(spec -> spec.tier() != CosmicEnchantmentTier.HEROIC).count());
        assertEquals(12, CosmicEnchantmentSpecs.ALL.stream()
                .filter(spec -> spec.tier() == CosmicEnchantmentTier.HEROIC).count());
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.CLEAVE.tier());
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.SOLITUDE.tier());
        assertEquals(CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentSpecs.CURSE.tier());
        assertEquals(ModEnchantments.MIGHTY_CLEAVE.identifier(),
                HeroicEnchantments.heroicFor(ModEnchantments.CLEAVE.identifier()).orElseThrow());
        assertEquals(ModEnchantments.FORBIDDEN_CURSE.identifier(),
                HeroicEnchantments.heroicFor(ModEnchantments.CURSE.identifier()).orElseThrow());
    }

    @Test void trialAndAdventureActivityIdentityFollowTheirRealSessionSemantics() {
        var first = java.util.UUID.randomUUID();
        var second = java.util.UUID.randomUUID();
        var trial = com.cosmicpve.trial.TrialSession.joining(java.util.UUID.randomUUID(),
                net.minecraft.world.level.Level.OVERWORLD.identifier(), net.minecraft.core.BlockPos.ZERO,
                List.of(net.minecraft.core.BlockPos.ZERO), List.of(),
                new com.cosmicpve.trial.TrialOwner(first, "first"),
                com.cosmicpve.data.component.TrialPortalModifiers.EMPTY).addParticipant(first).addParticipant(second);
        assertTrue(com.cosmicpve.combat.ownership.GeneralAllyResolver.sameTrialSession(trial, first, second));
        assertFalse(com.cosmicpve.combat.ownership.GeneralAllyResolver.sameTrialSession(trial, first,
                java.util.UUID.randomUUID()));

        var home = new com.cosmicpve.adventure.AdventureSession.ReturnPoint(
                net.minecraft.world.level.Level.OVERWORLD, net.minecraft.world.phys.Vec3.ZERO, 0, 0,
                net.minecraft.world.level.GameType.SURVIVAL);
        var adventureOne = new com.cosmicpve.adventure.AdventureSession(first, java.util.UUID.randomUUID(), 10,
                com.cosmicpve.adventure.AdventureSession.Phase.ACTIVE, home, net.minecraft.core.BlockPos.ZERO,
                net.minecraft.core.BlockPos.ZERO, 100, 0, 1);
        var adventureTwo = new com.cosmicpve.adventure.AdventureSession(second, java.util.UUID.randomUUID(), 30,
                com.cosmicpve.adventure.AdventureSession.Phase.ACTIVE, home, net.minecraft.core.BlockPos.ZERO,
                net.minecraft.core.BlockPos.ZERO, 100, 0, 1);
        assertTrue(com.cosmicpve.combat.ownership.GeneralAllyResolver.sameAdventureContext(adventureOne, adventureTwo));
        assertFalse(com.cosmicpve.combat.ownership.GeneralAllyResolver.sameAdventureContext(adventureOne,
                adventureTwo.phase(com.cosmicpve.adventure.AdventureSession.Phase.RETURNING, 100)));
    }
}
