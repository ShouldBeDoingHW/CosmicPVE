package com.cosmicpve.content.definition.armor;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.CosmicPVE;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.InputStreamReader;
import org.junit.jupiter.api.Test;

class BundledArmorSetDefinitionsTest {
    @Test void bundledPhantomAndYetiUseApprovedSemantics() {
        var phantom = load("phantom");
        assertEquals(0xFF6969, phantom.presentationColor());
        assertEquals(.25, phantom.additiveOutgoingBonus());
        assertEquals(1.10, phantom.incomingMultiplier());
        assertEquals(1.25, phantom.procChanceMultipliers().get(CosmicPVE.id("mastery")));

        var yeti = load("yeti");
        assertEquals(0xA3FFF5, yeti.presentationColor());
        assertEquals(.10, yeti.additiveOutgoingBonus());
        assertEquals(.90, yeti.incomingMultiplier());
        assertTrue(yeti.immunities().containsAll(java.util.Set.of(CosmicPVE.id("freeze"), CosmicPVE.id("frozen"),
                CosmicPVE.id("permafrost"), CosmicPVE.id("ice_aspect"))));
    }

    private static ArmorSetDefinition load(String name) {
        String path = "/data/cosmicpve/cosmicpve/armor_sets/" + name + ".json";
        try (var reader = new InputStreamReader(java.util.Objects.requireNonNull(
                BundledArmorSetDefinitionsTest.class.getResourceAsStream(path)))) {
            var data = ArmorSetDefinitionData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();
            return data.resolve(CosmicPVE.id(name)).valueOrThrow();
        } catch (java.io.IOException exception) {
            throw new AssertionError(exception);
        }
    }
}
