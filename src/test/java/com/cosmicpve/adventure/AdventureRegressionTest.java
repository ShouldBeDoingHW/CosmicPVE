package com.cosmicpve.adventure;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.equipment.enchantment.*;
import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AdventureRegressionTest {
    @Test void everyDurablePhaseRoundTripsExactReturnAndOwnedDestination() {
        var home=new AdventureSession.ReturnPoint(Level.NETHER,new Vec3(15,-100,80),12,24,GameType.SURVIVAL);
        for(var phase:AdventureSession.Phase.values()) {
            var s=new AdventureSession(UUID.randomUUID(),UUID.randomUUID(),20,phase,home,new BlockPos(30,81,50),new BlockPos(500,92,800),90100,8,3);
            var encoded=AdventureSession.CODEC.encodeStart(JsonOps.INSTANCE,s).getOrThrow();
            assertEquals(s,AdventureSession.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow());
        }
    }
    @Test void allCanonicalTiersHaveExplicitEffectPolicyAndVanillaRemainsEnabled() {
        for(var s:CosmicEnchantmentSpecs.ALL)assertEquals(Set.of(CosmicEnchantmentTier.SIMPLE,CosmicEnchantmentTier.UNIQUE,CosmicEnchantmentTier.ELITE).contains(s.tier()),AdventureRules.allows(s.id()),s.id().toString());
        assertTrue(AdventureRules.allows(net.minecraft.resources.Identifier.withDefaultNamespace("protection")));
    }
    @Test void allFifteenVariantsHaveBiomeConfiguredPlacedTemplatePaths() throws Exception {
        var biome=resource("data/cosmicpve/worldgen/biome/dense_woodlands.json");
        var features=biome.getAsJsonArray("features").get(9).getAsJsonArray();
        assertEquals(12,features.size());int variants=0;
        for(var family:features) {
            if (!family.getAsString().startsWith("cosmicpve:")) continue;
            String path=family.getAsString().split(":")[1];
            var placed=resource("data/cosmicpve/worldgen/placed_feature/"+path+".json");
            assertEquals(family.getAsString(),placed.get("feature").getAsString());
            var configured=resource("data/cosmicpve/worldgen/configured_feature/"+path+".json");
            assertEquals("cosmicpve:woodland_template",configured.get("type").getAsString());
            var config=configured.getAsJsonObject("config");int count=config.get("variants").getAsInt();variants+=count;
            for(int i=1;i<=count;i++)assertNotNull(getClass().getClassLoader().getResource("data/cosmicpve/structure/woodlands/"+path+i+".nbt"));
        }
        assertEquals(15,variants);
    }
    @Test void whiteScrollLogicalRowsHaveExactCountQuantityAndWeight() throws Exception {
        for(String tier:List.of("ultimate","legendary")) {
            var entries=resource("data/cosmicpve/cosmicpve/reward_tables/space_chest/"+tier+".json").getAsJsonArray("entries");
            var rows=entries.asList().stream().map(JsonElement::getAsJsonObject).filter(e->e.getAsJsonObject("reward").has("item")&&e.getAsJsonObject("reward").get("item").getAsString().equals("cosmicpve:white_scroll")).toList();
            assertEquals(1,rows.size());var row=rows.getFirst();assertEquals(tier.equals("ultimate")?18:20,row.get("weight").getAsInt());
            assertEquals(1,row.has("minimum_quantity")?row.get("minimum_quantity").getAsInt():1);
            assertEquals(1,row.has("maximum_quantity")?row.get("maximum_quantity").getAsInt():1);
        }
    }
    private JsonObject resource(String path) throws Exception {
        try(var input=getClass().getClassLoader().getResourceAsStream(path)) {assertNotNull(input,path);return JsonParser.parseString(new String(input.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();}
    }
}
