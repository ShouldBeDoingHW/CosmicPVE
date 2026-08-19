package com.cosmicpve.content.definition.scaling;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScalingProfileDataTest {
    @Test
    void decodesAndResolvesValidProfile() {
        ScalingProfileData data = ScalingProfileData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("""
                {
                  "party_size_1": 1.0,
                  "party_size_2": 1.5,
                  "party_size_3": 2.0,
                  "party_size_4": 2.5
                }
                """)).result().orElseThrow();

        ScalingProfile profile = data.resolve(Identifier.parse("cosmicpve:test")).valueOrThrow();
        assertEquals(1.0D, profile.valueForPartySize(1));
        assertEquals(2.5D, profile.valueForPartySize(4));
    }

    @Test
    void rejectsMissingPartySizeEntryDuringDecode() {
        var result = ScalingProfileData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("""
                {
                  "party_size_1": 1.0,
                  "party_size_2": 1.5,
                  "party_size_3": 2.0
                }
                """));

        assertTrue(result.error().isPresent());
        assertFalse(result.result().isPresent());
    }

    @Test
    void rejectsNonPositiveResolvedValue() {
        ScalingProfileData data = new ScalingProfileData(1.0D, 0.0D, 2.0D, 2.5D);
        assertFalse(data.resolve(Identifier.parse("cosmicpve:invalid")).isSuccess());
    }
}
