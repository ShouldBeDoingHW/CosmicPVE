package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.content.definition.trial.TrialRoomDefinitionData;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class TrialResourceTest {
    @Test void decisionAndDevelopmentDefinitionsDecode() throws Exception {
        assertRoom("trial/decision_box",1,46,27,46);
        assertRoom("trial/development_room",1,40,16,40);
        assertRoom("trial/raiding_rainbow",1,40,16,40);
        assertRoom("trial/circuit_circus",1,24,24,30);
    }
    @Test void importedStructuresAreValidAndContainOneEmeraldSpawnMarker() throws Exception {
        assertStructure("decision_box",47,28,47);
        assertStructure("development_room",41,17,41);
        assertStructure("raiding_rainbow",41,17,41,1);
        assertStructure("circuit_circus",25,25,31,5);
    }
    @Test void instanceDimensionUsesControlledVoidFlatGenerator() throws Exception {
        var stream=getClass().getClassLoader().getResourceAsStream("data/cosmicpve/dimension/cosmic_instance.json");
        assertNotNull(stream);
        var json=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals("minecraft:flat",json.getAsJsonObject("generator").get("type").getAsString());
        assertEquals("minecraft:the_void",json.getAsJsonObject("generator").getAsJsonObject("settings").get("biome").getAsString());
    }
    @Test void apprenticeRewardTableHasCanonicalWeightsAndDeferredBookRarities() throws Exception {
        var stream=getClass().getClassLoader().getResourceAsStream(
                "data/cosmicpve/cosmicpve/reward_tables/trial/apprentice.json");
        assertNotNull(stream);
        var entries=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8))
                .getAsJsonObject().getAsJsonArray("entries");
        assertEquals(16,entries.size());
        assertEquals(166,entries.asList().stream().mapToInt(value -> value.getAsJsonObject().get("weight").getAsInt()).sum());
        var books=entries.asList().stream().map(value -> value.getAsJsonObject())
                .filter(entry -> entry.getAsJsonObject("reward").get("type").getAsString().equals("unexamined_book")).toList();
        assertEquals(5,books.size());
        assertTrue(books.stream().allMatch(entry -> !entry.getAsJsonObject("reward").has("enchantment")));
        var simple=books.stream().filter(entry -> entry.getAsJsonObject("reward").get("rarity").getAsString().equals("simple")).findFirst().orElseThrow();
        assertEquals(2,simple.get("minimum_quantity").getAsInt()); assertEquals(2,simple.get("maximum_quantity").getAsInt());
    }
    private void assertRoom(String path,int pieces,int x,int y,int z) throws Exception {
        var stream=getClass().getClassLoader().getResourceAsStream("data/cosmicpve/cosmicpve/trial_rooms/"+path+".json");
        assertNotNull(stream,path);
        var data=TrialRoomDefinitionData.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8))).getOrThrow();
        var definition=data.resolve(Identifier.parse("cosmicpve:"+path)).valueOrThrow();
        assertEquals(pieces,definition.pieces().size()); assertEquals(x,definition.bounds().max().getX());
        assertEquals(y,definition.bounds().max().getY()); assertEquals(z,definition.bounds().max().getZ());
    }
    private void assertStructure(String name,int x,int y,int z) throws Exception { assertStructure(name,x,y,z,1); }
    private void assertStructure(String name,int x,int y,int z,int expectedEmeralds) throws Exception {
        var stream=getClass().getClassLoader().getResourceAsStream("data/cosmicpve/structure/trial/"+name+".nbt");
        assertNotNull(stream,name);
        var tag=NbtIo.readCompressed(stream,NbtAccounter.unlimitedHeap());
        var size=tag.getListOrEmpty("size"); assertEquals(x,size.getIntOr(0,-1)); assertEquals(y,size.getIntOr(1,-1)); assertEquals(z,size.getIntOr(2,-1));
        var palette=tag.getListOrEmpty("palette"); int emeraldState=-1;
        for(int i=0;i<palette.size();i++) if("minecraft:emerald_block".equals(palette.getCompoundOrEmpty(i).getStringOr("Name",""))) emeraldState=i;
        assertTrue(emeraldState>=0); int markers=0;
        for(var block:tag.getListOrEmpty("blocks")) if(block instanceof net.minecraft.nbt.CompoundTag compound
                && compound.getIntOr("state",-1)==emeraldState) markers++;
        assertEquals(expectedEmeralds,markers,name);
    }
}
