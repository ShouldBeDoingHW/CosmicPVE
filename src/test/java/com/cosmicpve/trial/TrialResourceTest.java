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
    }
    @Test void importedStructuresAreValidAndContainOneEmeraldSpawnMarker() throws Exception {
        assertStructure("decision_box",47,28,47);
        assertStructure("development_room",41,17,41);
    }
    @Test void instanceDimensionUsesControlledVoidFlatGenerator() throws Exception {
        var stream=getClass().getClassLoader().getResourceAsStream("data/cosmicpve/dimension/cosmic_instance.json");
        assertNotNull(stream);
        var json=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals("minecraft:flat",json.getAsJsonObject("generator").get("type").getAsString());
        assertEquals("minecraft:the_void",json.getAsJsonObject("generator").getAsJsonObject("settings").get("biome").getAsString());
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
    private void assertStructure(String name,int x,int y,int z) throws Exception {
        var stream=getClass().getClassLoader().getResourceAsStream("data/cosmicpve/structure/trial/"+name+".nbt");
        assertNotNull(stream,name);
        var tag=NbtIo.readCompressed(stream,NbtAccounter.unlimitedHeap());
        var size=tag.getListOrEmpty("size"); assertEquals(x,size.getIntOr(0,-1)); assertEquals(y,size.getIntOr(1,-1)); assertEquals(z,size.getIntOr(2,-1));
        var palette=tag.getListOrEmpty("palette"); int emeraldState=-1;
        for(int i=0;i<palette.size();i++) if("minecraft:emerald_block".equals(palette.getCompoundOrEmpty(i).getStringOr("Name",""))) emeraldState=i;
        assertTrue(emeraldState>=0); int markers=0;
        for(var block:tag.getListOrEmpty("blocks")) if(block instanceof net.minecraft.nbt.CompoundTag compound
                && compound.getIntOr("state",-1)==emeraldState) markers++;
        assertEquals(1,markers,name);
    }
}
