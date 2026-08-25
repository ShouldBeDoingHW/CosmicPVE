package com.cosmicpve.content.definition.mask;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.CosmicPVE;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class MaskDefinitionDataTest {
    private static final List<String> IDS = List.of("santa","reindeer","purge","party","lover","scarecrow","zeus","turkey","dragon");
    @Test void allCanonicalMasksDecodeResolveAndHaveUsableProfiles() throws Exception {
        for (String id : IDS) {
            var stream=getClass().getClassLoader().getResourceAsStream(
                    "data/cosmicpve/cosmicpve/masks/"+id+".json");
            assertNotNull(stream,id);
            var data=MaskDefinitionData.CODEC.parse(JsonOps.INSTANCE,
                    JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8))).getOrThrow();
            var resolved=data.resolve(CosmicPVE.id(id));
            assertTrue(resolved.isSuccess(),id);
            assertEquals(id.toUpperCase(java.util.Locale.ROOT),resolved.valueOrThrow().behavior().name());
        }
    }
    @Test void invalidColorProfileAndBehaviorAreRejected() {
        var bad=JsonParser.parseString("""
                {"display_name":{"text":"Bad"},"effect_summary":{"text":"Bad"},
                "color":"red","profile_texture":"not-base64","behavior":"unknown"}
                """);
        assertTrue(MaskDefinitionData.CODEC.parse(JsonOps.INSTANCE,bad).error().isPresent());
        var data=new MaskDefinitionData(net.minecraft.network.chat.Component.literal("Bad"),
                net.minecraft.network.chat.Component.literal("Bad"),"red","not-base64",MaskBehavior.SANTA);
        assertFalse(data.resolve(CosmicPVE.id("bad")).isSuccess());
    }
}
