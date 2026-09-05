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
    private static final List<String> IDS = List.of("santa","reindeer","purge","party","lover","scarecrow","zeus","turkey","dragon",
            "thanos","monopoly","gucci");
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

    @Test void step8nMasksUseCanonicalColorsProfilesAndIdentityMetadata() throws Exception {
        assertCanonical("thanos", "#5A118F",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDNhYTZhMzlhNGEwNTI3NzkxNTVlY2Y4MDI1OTU4MTA1MmVhMjVmZmJlZmVlYjFiMjJiOTM5ZjU1ZDNhZWQ2In19fQ==");
        assertCanonical("monopoly", "#43C5F0",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGE0MDJmY2QxNGI5MTliNmZjNzA2YWQ0Y2Y4M2IxODliOWM0ZTVkMmE5ZjYyYmNkYzRkNWVmMGYxZjJiMGJiMyJ9fX0=");
        assertCanonical("gucci", "#3AE305",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2UyYTE0YmE5NjBkNTA2MjgwOWExNGM5NzcyMjkzYzg3YTczMmRkMjZiZDk0YWFlNmVhMGU3MWVmZTk3ZWNkZiJ9fX0=");
    }

    private void assertCanonical(String id, String color, String texture) throws Exception {
        var stream = java.util.Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(
                "data/cosmicpve/cosmicpve/masks/" + id + ".json"));
        var json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals(color, json.get("color").getAsString());
        assertEquals(texture, json.get("profile_texture").getAsString());
        assertEquals(id, json.get("behavior").getAsString());
        assertEquals("mask.cosmicpve." + id,
                json.getAsJsonObject("display_name").get("translate").getAsString());
        assertEquals("mask.cosmicpve." + id + ".effect",
                json.getAsJsonObject("effect_summary").get("translate").getAsString());
    }
}
