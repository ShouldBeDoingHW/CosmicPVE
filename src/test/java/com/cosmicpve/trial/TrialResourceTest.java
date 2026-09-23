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
        assertRoom("trial/fire_colony",1,46,30,10);
        assertRoom("trial/zero_g",1,21,44,20);
        assertRoom("trial/cold_snap",1,38,30,36);
        assertRoom("trial/bomb_squad",1,44,20,44);
        assertRoom("trial/hidden_graveyard",1,39,17,42);
        assertRoom("trial/deadeye",2,70,30,42);
        assertRoom("trial/haze_seek",1,34,14,32);
        assertRoom("trial/warzone_giants",2,51,16,35);
        assertRoom("trial/cave_diving",1,33,39,30);
    }
    @Test void importedStructuresAreValidAndContainOneEmeraldSpawnMarker() throws Exception {
        assertStructure("decision_box",47,28,47);
        assertStructure("development_room",41,17,41);
        assertStructure("raiding_rainbow",41,17,41,1);
        assertStructure("circuit_circus",25,25,31,1);
        assertStructure("fire_colony",47,31,11,1);
        assertStructure("zero_g",22,45,21,1);
        assertStructure("cold_snap",39,31,37,1);
        assertStructure("bomb_squad",45,21,45,4);
        assertStructure("hidden_graveyard",40,18,43,1);
        assertStructure("deadeye_west",41,31,43,1);
        assertStructure("deadeye_east",30,31,43,0);
        assertStructure("haze_seek",35,15,33,1);
        assertStructure("warzone_giants_west",26,17,36,1);
        assertStructure("warzone_giants_east",26,17,36,0);
        assertStructure("cave_diving",34,40,31,1);
        assertStructureEntities("zero_g",8,"minecraft:shulker");
    }
    @Test void deadeyeDefinitionIsDemonicAndComposesWestThenEast() throws Exception {
        var stream=getClass().getClassLoader().getResourceAsStream(
                "data/cosmicpve/cosmicpve/trial_rooms/trial/deadeye.json");
        assertNotNull(stream);
        var data=TrialRoomDefinitionData.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8))).getOrThrow();
        var room=data.resolve(Identifier.parse("cosmicpve:trial/deadeye")).valueOrThrow();
        assertEquals(com.cosmicpve.content.definition.trial.TrialRoomCategory.DEMONIC,room.category());
        assertEquals(Identifier.parse("cosmicpve:trial/deadeye_west"),room.pieces().get(0).structure());
        assertEquals(net.minecraft.core.BlockPos.ZERO,room.pieces().get(0).offset());
        assertEquals(Identifier.parse("cosmicpve:trial/deadeye_east"),room.pieces().get(1).structure());
        assertEquals(new net.minecraft.core.BlockPos(41,0,0),room.pieces().get(1).offset());
        assertEquals(com.cosmicpve.trial.room.DeadeyeService.WEST_SPAWN_MARKER,
                room.spawnMarkerPosition().orElseThrow());
    }
    @Test void hiddenGraveyardDefinitionIsImpossible() throws Exception {
        var stream=getClass().getClassLoader().getResourceAsStream(
                "data/cosmicpve/cosmicpve/trial_rooms/trial/hidden_graveyard.json");
        assertNotNull(stream);
        var data=TrialRoomDefinitionData.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8))).getOrThrow();
        assertEquals(com.cosmicpve.content.definition.trial.TrialRoomCategory.IMPOSSIBLE,
                data.resolve(Identifier.parse("cosmicpve:trial/hidden_graveyard")).valueOrThrow().category());
    }

    @Test void impossibleProductionTableIsExactConstructibleSubset() throws Exception {
        var stream=getClass().getClassLoader().getResourceAsStream(
                "data/cosmicpve/cosmicpve/reward_tables/trial/impossible.json");
        assertNotNull(stream);
        var entries=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8))
                .getAsJsonObject().getAsJsonArray("entries");
        assertEquals(22, entries.size());
        assertEquals(184, entries.asList().stream().mapToInt(value ->
                value.getAsJsonObject().get("weight").getAsInt()).sum());
        assertEquals(23, ImpossibleRewardCatalog.DECLARED.size());
        assertEquals(192, ImpossibleRewardCatalog.DECLARED_WEIGHT);
        assertEquals(184, ImpossibleRewardCatalog.ACTIVE_WEIGHT);
        assertEquals(1, entries.asList().stream().filter(value -> {
            var entry = value.getAsJsonObject(); var reward = entry.getAsJsonObject("reward");
            return entry.get("weight").getAsInt() == 3 && reward.get("type").getAsString().equals("static_item")
                    && reward.get("item").getAsString().equals("cosmicpve:random_weapon_skin_generator");
        }).count());
        assertTrue(ImpossibleRewardCatalog.DECLARED.stream().filter(row -> row.name().equals("Random Tier 2 Trial Trinket")).findFirst().orElseThrow().active());
        assertFalse(ImpossibleRewardCatalog.DECLARED.stream().filter(row -> row.name().contains("Abandoned")).findFirst().orElseThrow().active());
    }
    @Test void demonicDevelopmentTableUsesOnlyCurrentRealRewardPrimitivesAtCanonicalWeights() throws Exception {
        var stream=getClass().getClassLoader().getResourceAsStream(
                "data/cosmicpve/cosmicpve/reward_tables/trial/demonic_development.json");
        assertNotNull(stream);
        var entries=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8))
                .getAsJsonObject().getAsJsonArray("entries");
        assertEquals(20,entries.size());
        assertEquals(144,entries.asList().stream().mapToInt(value->value.getAsJsonObject().get("weight").getAsInt()).sum());
        assertEquals(1, entries.asList().stream().filter(value -> {
            var entry = value.getAsJsonObject(); var reward = entry.getAsJsonObject("reward");
            return entry.get("weight").getAsInt() == 5 && reward.get("type").getAsString().equals("static_item")
                    && reward.get("item").getAsString().equals("cosmicpve:random_weapon_skin_generator");
        }).count());
        assertTrue(entries.asList().stream().map(value->value.getAsJsonObject().getAsJsonObject("reward"))
                .anyMatch(reward->reward.get("type").getAsString().equals("mask")
                        && reward.get("mask_count").getAsInt()==2));
    }
    @Test void hardcoreDevelopmentTablePreservesEveryCurrentlySupportedCanonicalRow() throws Exception {
        var stream=getClass().getClassLoader().getResourceAsStream(
                "data/cosmicpve/cosmicpve/reward_tables/trial/hardcore_development.json");
        assertNotNull(stream);
        var entries=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8))
                .getAsJsonObject().getAsJsonArray("entries");
        assertEquals(13,entries.size());
        assertEquals(82,entries.asList().stream().mapToInt(value -> value.getAsJsonObject().get("weight").getAsInt()).sum());
        assertEquals(2,entries.asList().stream().map(value->value.getAsJsonObject().getAsJsonObject("reward"))
                .filter(reward->reward.get("type").getAsString().equals("unexamined_book")).count());
        assertTrue(entries.asList().stream().map(value->value.getAsJsonObject().getAsJsonObject("reward"))
                .anyMatch(reward->reward.get("type").getAsString().equals("unexamined_book")
                        && reward.get("rarity").getAsString().equals("heroic")));
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
        assertEquals(15,entries.size());
        assertEquals(108,entries.asList().stream().mapToInt(value -> value.getAsJsonObject().get("weight").getAsInt()).sum());
        var vault = entries.asList().stream().map(value -> value.getAsJsonObject())
                .filter(entry -> entry.getAsJsonObject("reward").has("item")
                        && entry.getAsJsonObject("reward").get("item").getAsString()
                                .equals("cosmicpve:personal_vault_unlock")).toList();
        assertEquals(1, vault.size());
        assertEquals(7, vault.getFirst().get("weight").getAsInt());
        assertEquals("static_item", vault.getFirst().getAsJsonObject("reward").get("type").getAsString());
        var books=entries.asList().stream().map(value -> value.getAsJsonObject())
                .filter(entry -> entry.getAsJsonObject("reward").get("type").getAsString().equals("unexamined_book")).toList();
        assertEquals(6,books.size());
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
        if(expectedEmeralds>0) assertTrue(emeraldState>=0); int markers=0;
        for(var block:tag.getListOrEmpty("blocks")) if(block instanceof net.minecraft.nbt.CompoundTag compound
                && compound.getIntOr("state",-1)==emeraldState) markers++;
        assertEquals(expectedEmeralds,markers,name);
    }
    private void assertStructureEntities(String name,int expected,String entityId) throws Exception {
        var stream=getClass().getClassLoader().getResourceAsStream("data/cosmicpve/structure/trial/"+name+".nbt");
        var tag=NbtIo.readCompressed(stream,NbtAccounter.unlimitedHeap());
        var entities=tag.getListOrEmpty("entities");
        assertEquals(expected,entities.size());
        for(var value:entities) assertEquals(entityId,((net.minecraft.nbt.CompoundTag)value)
                .getCompoundOrEmpty("nbt").getStringOr("id",""));
    }
}
