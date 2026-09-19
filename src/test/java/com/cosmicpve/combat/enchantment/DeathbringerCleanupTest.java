package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.equipment.enchantment.*;
import com.cosmicpve.equipment.armor.ArmorCrystalConfirmationService;
import com.cosmicpve.entity.woodlands.DreadmaneEntity;
import com.cosmicpve.reward.preview.LootPreviewMenu;
import com.cosmicpve.reward.animation.LootAnimationTimeline;
import com.cosmicpve.reward.lootbox.Step8ELootboxService;
import com.cosmicpve.registry.ModEnchantments;
import org.junit.jupiter.api.Test;

class DeathbringerCleanupTest {
    @Test void passiveBonusesAndReplacementCatalogAreExact() {
        for (int level = 1; level <= 3; level++) {
            assertEquals((1 + level) / 100.0, DeathbringerBehavior.bonus(level, false));
            assertEquals((5 + level) / 100.0, DeathbringerBehavior.bonus(level, true));
        }
        assertEquals(0, DeathbringerBehavior.bonus(0, false));
        assertThrows(IllegalArgumentException.class, () -> DeathbringerBehavior.bonus(4, true));
        assertEquals(CosmicEnchantmentTier.LEGENDARY, CosmicEnchantmentSpecs.DEATHBRINGER.tier());
        assertEquals("helmet", CosmicEnchantmentSpecs.DEATHBRINGER.equipmentApplicability());
        assertEquals(3, CosmicEnchantmentSpecs.PLANETARY_DEATHBRINGER.maxLevel());
        assertEquals(ModEnchantments.PLANETARY_DEATHBRINGER.identifier(),
                HeroicEnchantments.heroicFor(ModEnchantments.DEATHBRINGER.identifier()).orElseThrow());
        var source = com.cosmicpve.CosmicPVE.id("fixture");
        var invalid = new EffectiveEnchantmentsResolver().resolveSources(java.util.List.of(
                new ActualEnchantmentGrant(ModEnchantments.DEATHBRINGER.identifier(),3,source),
                new ActualEnchantmentGrant(ModEnchantments.PLANETARY_DEATHBRINGER.identifier(),1,source)),java.util.List.of());
        assertEquals(0, invalid.level(ModEnchantments.DEATHBRINGER.identifier()));
        assertEquals(1, invalid.level(ModEnchantments.PLANETARY_DEATHBRINGER.identifier()));
        assertTrue(CosmicEnchantmentSpecs.DEATHBRINGER.tier().extractableByBlackScroll());
        assertFalse(CosmicEnchantmentSpecs.PLANETARY_DEATHBRINGER.tier().extractableByBlackScroll());
    }
    @Test void everyJockeyRollAndPreviewPageBoundaryIsCovered() {
        int hits = 0;
        for (int roll = 0; roll < 100; roll++) if (DreadmaneEntity.shouldSpawnJockey(roll)) hits++;
        assertEquals(15, hits);
        assertFalse(DreadmaneEntity.shouldSpawnJockey(15));
        assertThrows(IllegalArgumentException.class, () -> DreadmaneEntity.shouldSpawnJockey(100));
        assertEquals(1, LootPreviewMenu.pages(54));
        assertEquals(2, LootPreviewMenu.pages(55));
        assertEquals(2, LootPreviewMenu.pages(90));
        assertEquals(3, LootPreviewMenu.pages(91));
    }
    @Test void confirmationAndDurationPoliciesAreExact() {
        for (String text : new String[]{"confirm", "CONFIRM", " Confirm "})
            assertTrue(ArmorCrystalConfirmationService.isConfirmation(text));
        for (String text : new String[]{"confirm now", "confirmed", "hello"})
            assertFalse(ArmorCrystalConfirmationService.isConfirmation(text));
        assertEquals(600, ArmorCrystalConfirmationService.TIMEOUT_TICKS);
        assertEquals(6, Step8ELootboxService.durationWeight(10));
        assertEquals(4, Step8ELootboxService.durationWeight(20));
        assertEquals(2, Step8ELootboxService.durationWeight(30));
        assertEquals(60, LootAnimationTimeline.CLOSE_TICK - LootAnimationTimeline.REVEAL_TICK);
    }
    @Test void compassTargetsAreSessionSpecificAndSurviveSerialization() {
        var home = new com.cosmicpve.adventure.AdventureSession.ReturnPoint(net.minecraft.world.level.Level.OVERWORLD,
                net.minecraft.world.phys.Vec3.ZERO, 0, 0, net.minecraft.world.level.GameType.SURVIVAL);
        var a = new com.cosmicpve.adventure.AdventureSession(java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), 10,
                com.cosmicpve.adventure.AdventureSession.Phase.ACTIVE, home, net.minecraft.core.BlockPos.ZERO,
                new net.minecraft.core.BlockPos(100,80,200), 12000, 0, 1);
        var b = new com.cosmicpve.adventure.AdventureSession(java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), 10,
                com.cosmicpve.adventure.AdventureSession.Phase.ACTIVE, home, net.minecraft.core.BlockPos.ZERO,
                new net.minecraft.core.BlockPos(-100,80,-200), 12000, 0, 1);
        var first = com.cosmicpve.adventure.DenseWoodlandsSessionService.compassTarget(a);
        var second = com.cosmicpve.adventure.DenseWoodlandsSessionService.compassTarget(b);
        assertNotEquals(first.target(), second.target());
        assertEquals(a.exit(), first.target().orElseThrow().pos());
        assertEquals(com.cosmicpve.adventure.DenseWoodlandsSessionService.DIMENSION, first.target().orElseThrow().dimension());
        assertFalse(first.tracked());
        var json = com.cosmicpve.adventure.AdventureSession.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, a).getOrThrow();
        var restored = com.cosmicpve.adventure.AdventureSession.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, json).getOrThrow();
        assertEquals(first, com.cosmicpve.adventure.DenseWoodlandsSessionService.compassTarget(restored));
    }
}
