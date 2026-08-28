package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.junit.jupiter.api.Test;

class Step7CBalanceBaselineTest {
    @Test void gearsUsesFivePercentPerLevelWithOneStableEquippedModifier() {
        assertEquals(0.05, GearsBehavior.movementBonus(1), 1e-12);
        assertEquals(0.10, GearsBehavior.movementBonus(2), 1e-12);
        assertEquals(0.15, GearsBehavior.movementBonus(3), 1e-12);
        var first = GearsBehavior.modifier(3);
        var replacement = GearsBehavior.modifier(3);
        assertEquals(GearsBehavior.MODIFIER_ID, first.id());
        assertEquals(first.id(), replacement.id());
        assertEquals(AttributeModifier.Operation.ADD_MULTIPLIED_BASE, first.operation());
        assertEquals(0.15, first.amount(), 1e-12);
    }

    @Test void permafrostChanceThresholdBinaryPenaltyAndBurstAreCanonical() {
        for (int level = 1; level <= 6; level++) {
            assertEquals(0.025 * level, PermafrostBehavior.chance(level), 1e-12);
            assertEquals(10 - level, PermafrostBehavior.threshold(level));
        }
        assertEquals(-0.02, PermafrostBehavior.OUTGOING_PENALTY, 1e-12);
        assertEquals(1.02, PermafrostBehavior.INCOMING_MULTIPLIER, 1e-12);
        assertEquals(6.0, PermafrostBehavior.burstPacket().amount(), 1e-12);
        assertTrue(PermafrostBehavior.burstPacket().bypassesArmor());
        assertTrue(PermafrostBehavior.burstPacket().bypassesCustomReduction());
        assertFalse(PermafrostBehavior.burstPacket().bypassesAbsorption());
    }

    @Test void permafrostDefinitionUsesIndependentSixtySecondNegativeStacks() throws Exception {
        try (var reader = resource("/data/cosmicpve/cosmicpve/stack_definitions/permafrost.json")) {
            var json = JsonParser.parseReader(reader).getAsJsonObject();
            assertEquals("negative", json.get("polarity").getAsString());
            assertEquals(9, json.get("maximum_stacks").getAsInt());
            assertEquals(1200, json.get("duration_ticks").getAsInt());
            assertEquals("independent", json.get("refresh_policy").getAsString());
            assertTrue(json.get("cleansable").getAsBoolean());
            assertFalse(json.get("persistent").getAsBoolean());
        }
        try (var reader = resource("/data/cosmicpve/cosmicpve/armor_sets/yeti.json")) {
            var immunities = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("immunities");
            assertTrue(immunities.asList().stream().anyMatch(value -> value.getAsString().equals("cosmicpve:permafrost")));
        }
    }

    @Test void mortalCoilIsDefensiveTwoLevelAbsorptionProc() {
        assertEquals(0.03, MortalCoilBehavior.chance(1), 1e-12);
        assertEquals(0.06, MortalCoilBehavior.chance(2), 1e-12);
        assertEquals(0.06, MortalCoilBehavior.chance(3), 1e-12); // legacy over-level data clamps safely
        assertEquals(100, MortalCoilBehavior.DURATION_TICKS);
        assertEquals(4.0F, MortalCoilBehavior.ABSORPTION_HP);
        assertEquals(0, MortalCoilBehavior.ABSORPTION_AMPLIFIER);
        assertEquals(2, CosmicEnchantmentSpecs.MORTAL_COIL.maxLevel());
        assertEquals(CosmicEnchantmentTier.MASTERY, CosmicEnchantmentSpecs.MORTAL_COIL.tier());
    }

    @Test void obsidianshieldUsesFireReductionInsteadOfPermanentResistance() {
        assertEquals(1.0, ObsidianshieldBehavior.incomingMultiplier(0), 1e-12);
        assertEquals(0.75, ObsidianshieldBehavior.incomingMultiplier(1), 1e-12);
        assertEquals(0.50, ObsidianshieldBehavior.incomingMultiplier(2), 1e-12);
        assertEquals(2, CosmicEnchantmentSpecs.OBSIDIANSHIELD.maxLevel());
    }

    @Test void remainingCanonicalBalanceValuesArePinnedTogether() {
        assertEquals(0.015, BleedBehavior.chance(1), 1e-12);
        assertEquals(0.09, BleedBehavior.chance(6), 1e-12);
        assertEquals(0.12, MoltenBehavior.chance(4), 1e-12);
        assertEquals(0.21, PoisonBehavior.chance(3), 1e-12);
        assertEquals(0.09, PummelBehavior.chance(3), 1e-12);
        assertEquals(60, PummelBehavior.DURATION_TICKS);
        assertEquals(1, PummelBehavior.AMPLIFIER);
        assertEquals(0.06, RageBehavior.bonus(1), 1e-12);
        assertEquals(0.11, RageBehavior.bonus(6), 1e-12);
        assertEquals(100, RageBehavior.windowTicks(1));
        assertEquals(200, RageBehavior.windowTicks(6));
    }

    private static InputStreamReader resource(String path) {
        return new InputStreamReader(Objects.requireNonNull(
                Step7CBalanceBaselineTest.class.getResourceAsStream(path)), StandardCharsets.UTF_8);
    }
}
