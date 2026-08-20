package com.cosmicpve.content.definition.armor;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.CosmicPVE;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

class ArmorSetDefinitionDataTest {
    @Test void phantomDefinitionDecodesAndResolvesExactValues() {
        var json = JsonParser.parseString("""
                {"display_name":{"text":"Phantom"},"color":"#FF6969",
                "full_set_bonus":[{"text":"bonus"}],"additive_outgoing_bonus":0.25,
                "incoming_multiplier":1.10,"proc_chance_multipliers":{"cosmicpve:mastery":1.25}}
                """);
        var data = ArmorSetDefinitionData.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        var definition = data.resolve(CosmicPVE.id("phantom")).valueOrThrow();
        assertEquals(0xFF6969, definition.presentationColor());
        assertEquals(0.25, definition.additiveOutgoingBonus());
        assertEquals(1.10, definition.incomingMultiplier());
        assertEquals(1.25, definition.procChanceMultipliers().get(CosmicPVE.id("mastery")));
    }

    @Test void invalidColorAndMultiplierAreRejected() {
        var data = new ArmorSetDefinitionData(net.minecraft.network.chat.Component.literal("Bad"), "red",
                java.util.List.of(net.minecraft.network.chat.Component.literal("bonus")), 0, -1,
                java.util.Map.of(), java.util.List.of());
        assertFalse(data.resolve(CosmicPVE.id("bad")).isSuccess());
    }
}
