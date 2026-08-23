package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.data.component.UnexaminedBookData;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

class UnexaminedBookDataTest {
    @Test void codecRoundTripsEveryTierByStableLowercaseName() {
        for (var tier : CosmicEnchantmentTier.values()) {
            var data = new UnexaminedBookData(UnexaminedBookData.CURRENT_DATA_VERSION, tier);
            var encoded = UnexaminedBookData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
            assertEquals(tier.serializedName(), encoded.getAsJsonObject().get("tier").getAsString());
            assertEquals(data, UnexaminedBookData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
        }
    }

    @Test void codecRejectsUnknownTierAndInvalidVersion() {
        assertTrue(UnexaminedBookData.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString("{\"tier\":\"mythic\"}")).error().isPresent());
        assertTrue(UnexaminedBookData.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString("{\"data_version\":0,\"tier\":\"simple\"}"))
                .error().isPresent());
    }
}
