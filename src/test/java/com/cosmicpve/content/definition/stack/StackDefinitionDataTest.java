package com.cosmicpve.content.definition.stack;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StackDefinitionDataTest {
    @Test
    void canonicalBleedDefinitionHasPinnedIndependentRuntimeSemantics() throws Exception {
        var resource = getClass().getClassLoader().getResourceAsStream(
                "data/cosmicpve/cosmicpve/stack_definitions/bleed.json");
        try (var reader = new InputStreamReader(java.util.Objects.requireNonNull(resource), StandardCharsets.UTF_8)) {
            StackDefinitionData data = StackDefinitionData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                    .result().orElseThrow();
            StackDefinition definition = data.resolve(Identifier.parse("cosmicpve:bleed")).valueOrThrow();
            assertEquals(StackPolarity.NEGATIVE, definition.polarity());
            assertEquals(10, definition.maximumStacks());
            assertEquals(100, definition.durationTicks());
            assertEquals(StackRefreshPolicy.INDEPENDENT, definition.refreshPolicy());
            assertFalse(definition.transferable());
            assertTrue(definition.cleansable());
            assertFalse(definition.persistent());
        }
    }

    @Test
    void decodesAndResolvesValidDefinition() {
        StackDefinitionData data = StackDefinitionData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("""
                {
                  "polarity": "negative",
                  "maximum_stacks": 10,
                  "duration_ticks": 100,
                  "refresh_policy": "refresh_all",
                  "transferable": false,
                  "cleansable": true,
                  "persistent": false
                }
                """)).result().orElseThrow();

        StackDefinition definition = data.resolve(Identifier.parse("cosmicpve:test_stack")).valueOrThrow();
        assertEquals(StackPolarity.NEGATIVE, definition.polarity());
        assertEquals(10, definition.maximumStacks());
    }

    @Test
    void rejectsUnknownEnumDuringDecode() {
        var result = StackDefinitionData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("""
                {
                  "polarity": "neutral",
                  "maximum_stacks": 10,
                  "duration_ticks": 100,
                  "refresh_policy": "refresh_all",
                  "transferable": false,
                  "cleansable": true,
                  "persistent": false
                }
                """));

        assertTrue(result.error().isPresent());
        assertFalse(result.result().isPresent());
    }

    @Test
    void rejectsInvalidCountsDurationsAndCombinations() {
        Identifier id = Identifier.parse("cosmicpve:invalid_stack");
        assertFalse(new StackDefinitionData(
                StackPolarity.POSITIVE,
                0,
                0,
                StackRefreshPolicy.REFRESH_ALL,
                true,
                true,
                false).resolve(id).isSuccess());
        assertFalse(new StackDefinitionData(
                StackPolarity.POSITIVE,
                1,
                60,
                StackRefreshPolicy.INDEPENDENT,
                true,
                true,
                false).resolve(id).isSuccess());
    }
}
