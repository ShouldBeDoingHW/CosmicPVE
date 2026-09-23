package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.combat.proc.ProcChance;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.registry.ModEnchantments;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class HexEnchantmentTest {
    @Test void canonicalMetadataChanceAndPerStackDamageMath() {
        assertEquals(5, CosmicEnchantmentSpecs.HEX.maxLevel());
        assertEquals(CosmicEnchantmentTier.LEGENDARY, CosmicEnchantmentSpecs.HEX.tier());
        assertEquals("axe", CosmicEnchantmentSpecs.HEX.equipmentApplicability());
        assertTrue(CosmicEnchantmentSpecs.HEX.tier().extractableByBlackScroll());
        for (int level = 1; level <= 5; level++) assertEquals(level * .01, HexBehavior.chance(level), 1e-12);
        assertEquals(.06, ProcChance.calculate(HexBehavior.chance(5), List.of(1.2)), 1e-12);
        assertEquals(-.06, HexBehavior.outgoingPenalty(3), 1e-12);
        assertEquals(1.06, HexBehavior.incomingMultiplier(3), 1e-12);
        assertEquals(1.0, HexBehavior.incomingMultiplier(0), 1e-12);
        assertEquals(81, CosmicEnchantmentSpecs.ALL.size());
        assertTrue(CosmicEnchantmentSpecs.find(ModEnchantments.HEX.identifier()).isPresent());
    }

    @Test void stackDefinitionIsIndependentFiveSecondCleansableNegativeWithoutGameplayCap() throws Exception {
        try (var reader = new InputStreamReader(require("data/cosmicpve/cosmicpve/stack_definitions/hex.json"),
                StandardCharsets.UTF_8)) {
            var json = JsonParser.parseReader(reader).getAsJsonObject();
            assertEquals("negative", json.get("polarity").getAsString());
            assertEquals("independent", json.get("refresh_policy").getAsString());
            assertEquals(100, json.get("duration_ticks").getAsInt());
            assertEquals(1024, json.get("maximum_stacks").getAsInt());
            assertTrue(json.get("cleansable").getAsBoolean());
        }
    }

    @Test void moreThanFiveStacksCoexistAndExpireIndependently() {
        var repository = new com.cosmicpve.content.CosmicContentRepository();
        var definition = new com.cosmicpve.content.definition.stack.StackDefinition(
                HexBehavior.STACK_ID, com.cosmicpve.content.definition.stack.StackPolarity.NEGATIVE,
                1024, 100, com.cosmicpve.content.definition.stack.StackRefreshPolicy.INDEPENDENT,
                false, true, false);
        repository.publish(com.cosmicpve.content.validation.ValidationResult.success(
                new com.cosmicpve.content.ContentSnapshot(0, Map.of(), Map.of(HexBehavior.STACK_ID, definition))));
        var service = new com.cosmicpve.combat.stack.CombatStackService(repository);
        var container = new com.cosmicpve.combat.stack.CombatStackContainer();
        service.addStack(container, HexBehavior.STACK_ID, 6,
                com.cosmicpve.combat.stack.StackApplication.unattributed(), 0);
        assertEquals(6, service.count(container, HexBehavior.STACK_ID, 0));
        service.addStack(container, HexBehavior.STACK_ID, 1,
                com.cosmicpve.combat.stack.StackApplication.unattributed(), 40);
        assertEquals(1, service.expireDue(container, 100).expired() > 0 ? service.count(container, HexBehavior.STACK_ID, 100) : -1);
        assertEquals(0, service.count(container, HexBehavior.STACK_ID, 140));
    }
    private java.io.InputStream require(String path) {
        var stream = getClass().getClassLoader().getResourceAsStream(path);
        assertNotNull(stream, path);
        return stream;
    }
}
