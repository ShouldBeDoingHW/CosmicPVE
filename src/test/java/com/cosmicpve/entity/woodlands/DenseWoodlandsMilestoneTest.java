package com.cosmicpve.entity.woodlands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.combat.enchantment.DefensiveCosmicEnchantments;
import com.cosmicpve.combat.enchantment.NeutralizeBehavior;
import com.cosmicpve.combat.enchantment.NimbleBehavior;
import com.cosmicpve.combat.enchantment.ThunderingBlowBehavior;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.registry.ModEnchantments;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.List;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

class DenseWoodlandsMilestoneTest {
    @Test
    void enchantmentDefinitionsAndExactFormulasAreCanonical() {
        assertEquals(CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentSpecs.NIMBLE.tier());
        assertEquals("boots", CosmicEnchantmentSpecs.NIMBLE.equipmentApplicability());
        assertEquals(4, CosmicEnchantmentSpecs.NIMBLE.maxLevel());
        assertEquals(CosmicEnchantmentTier.SIMPLE, CosmicEnchantmentSpecs.THUNDERING_BLOW.tier());
        assertEquals("sword", CosmicEnchantmentSpecs.THUNDERING_BLOW.equipmentApplicability());
        assertEquals(3, CosmicEnchantmentSpecs.THUNDERING_BLOW.maxLevel());
        assertEquals(CosmicEnchantmentTier.MASTERY, CosmicEnchantmentSpecs.NEUTRALIZE.tier());
        assertEquals("axe", CosmicEnchantmentSpecs.NEUTRALIZE.equipmentApplicability());
        assertEquals(5, CosmicEnchantmentSpecs.NEUTRALIZE.maxLevel());

        assertEquals(List.of(2, 4, 6, 8), java.util.stream.IntStream.rangeClosed(1, 4)
                .map(NimbleBehavior::maximumStacks).boxed().toList());
        assertEquals(180L, NimbleBehavior.lifetimeTicks(4));
        assertEquals(.08, NimbleBehavior.outgoingBonus(8), 1.0E-12);
        assertEquals(List.of(.03, .06, .09), java.util.stream.IntStream.rangeClosed(1, 3)
                .mapToObj(ThunderingBlowBehavior::chance).toList());
        assertEquals(List.of(.01, .02, .03, .04, .05), java.util.stream.IntStream.rangeClosed(1, 5)
                .mapToObj(NeutralizeBehavior::chance).toList());
    }

    @Test
    void defensiveClassificationIsSemanticAndExplicit() {
        assertTrue(DefensiveCosmicEnchantments.contains(ModEnchantments.DODGE.identifier()));
        assertTrue(DefensiveCosmicEnchantments.contains(ModEnchantments.INVERSION.identifier()));
        assertTrue(DefensiveCosmicEnchantments.contains(ModEnchantments.AEGIS.identifier()));
        assertTrue(DefensiveCosmicEnchantments.contains(ModEnchantments.ARMORED.identifier()));
        assertTrue(DefensiveCosmicEnchantments.contains(ModEnchantments.CACTUS.identifier()));
        assertTrue(DefensiveCosmicEnchantments.contains(ModEnchantments.ANGELIC.identifier()));
        assertFalse(DefensiveCosmicEnchantments.contains(ModEnchantments.CLEAVE.identifier()));
        assertFalse(DefensiveCosmicEnchantments.contains(ModEnchantments.THUNDERING_BLOW.identifier()));
    }

    @Test
    void seededEquipmentPlanUsesIndependentRollsAndExactRanges() {
        var values = new ArrayDeque<>(List.of(
                0, 3, 5, // helmet present: Protection IV, Voodoo VI
                1,       // chest absent
                0, 2,    // legs present: Armored III
                0, 4, 3, 0, // boots present: Angelic V, Armored IV, Protection I
                0, 4,    // Power V
                1,       // Virus absent
                0, 2,    // Obliterate III
                0, 3));  // Snare IV
        var plan = new ForestFanaticEquipmentService().generate(bound -> {
            int value = values.removeFirst();
            assertTrue(value >= 0 && value < bound, "fixture value must honor requested bound");
            return value;
        });

        assertEquals(new ForestFanaticEquipmentPlan.Helmet(4, 6), plan.helmet().orElseThrow());
        assertTrue(plan.chestplate().isEmpty());
        assertEquals(new ForestFanaticEquipmentPlan.Leggings(7, 3, 4), plan.leggings().orElseThrow());
        assertEquals(new ForestFanaticEquipmentPlan.Boots(4, 5, 4, 1), plan.boots().orElseThrow());
        assertEquals(new ForestFanaticEquipmentPlan.Bow(3, 5, 0, 3, 4), plan.bow());
        assertTrue(values.isEmpty());
        assertEquals(0.0F, ForestFanaticEquipmentService.DROP_CHANCE);
    }

    @Test
    void spawnDataAndFixedLootGatesMatchTheMilestone() throws Exception {
        assertEquals(70, DenseWoodlandsMobSpawns.FANATIC_WEIGHT);
        assertEquals(1, DenseWoodlandsMobSpawns.FANATIC_MIN);
        assertEquals(3, DenseWoodlandsMobSpawns.FANATIC_MAX);
        assertEquals(30, DenseWoodlandsMobSpawns.DREADMANE_WEIGHT);
        assertEquals(1, DenseWoodlandsMobSpawns.DREADMANE_MIN);
        assertEquals(2, DenseWoodlandsMobSpawns.DREADMANE_MAX);
        assertEquals("cosmicpve:adventure/dense_woodlands", DenseWoodlandsMobLoot.BASIC_TABLE.toString());

        assertTrue(DenseWoodlandsMobLoot.selected(.249999, .25));
        assertFalse(DenseWoodlandsMobLoot.selected(.25, .25));
        assertTrue(DenseWoodlandsMobLoot.selected(.399999, .40));
        assertFalse(DenseWoodlandsMobLoot.selected(.40, .40));

        try (var stream = getClass().getClassLoader().getResourceAsStream(
                "data/cosmicpve/worldgen/biome/dense_woodlands.json")) {
            var monsters = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonObject("spawners").getAsJsonArray("monster");
            assertEquals(2, monsters.size());
            assertEquals("cosmicpve:forest_fanatic", monsters.get(0).getAsJsonObject().get("type").getAsString());
            assertEquals(70, monsters.get(0).getAsJsonObject().get("weight").getAsInt());
            assertEquals("cosmicpve:dreadmane", monsters.get(1).getAsJsonObject().get("type").getAsString());
            assertEquals(30, monsters.get(1).getAsJsonObject().get("weight").getAsInt());
        }
    }

    @Test
    void buckRangeUsesCollisionBoundsRatherThanCenters() {
        var horse = new AABB(0, 0, 0, 1, 2, 1);
        assertEquals(.25, DreadmaneEntity.collisionDistanceSquared(horse,
                new AABB(1.5, 0, 0, 2.5, 2, 1)), 1.0E-12);
        assertEquals(0.0, DreadmaneEntity.collisionDistanceSquared(horse,
                new AABB(.5, 0, .5, 1.5, 2, 1.5)), 1.0E-12);
        assertTrue(DreadmaneEntity.collisionDistanceSquared(horse,
                new AABB(1.51, 0, 0, 2.51, 2, 1)) > DreadmaneEntity.BUCK_RANGE * DreadmaneEntity.BUCK_RANGE);
        assertEquals(10.0F, DreadmaneEntity.BUCK_DAMAGE);
        assertEquals(1.5, DreadmaneEntity.BUCK_KNOCKBACK);
        assertEquals(35, DreadmaneEntity.BUCK_SLOWNESS_TICKS);
        assertEquals(200, DreadmaneEntity.BUCK_COOLDOWN_TICKS);
    }
}
