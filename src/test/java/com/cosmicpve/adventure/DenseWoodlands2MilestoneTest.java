package com.cosmicpve.adventure;

import com.cosmicpve.adventure.ranger.AdvancedWoodlandsRewards;
import com.cosmicpve.combat.enchantment.PinpointBehavior;
import com.cosmicpve.command.AdventureInfoCommands;
import com.cosmicpve.equipment.enchantment.*;
import com.cosmicpve.registry.ModEnchantments;
import com.google.gson.*;
import java.io.*;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DenseWoodlands2MilestoneTest {
    @Test void pinpointIsEliteBowSixAndExplicitOnly(){
        var s=CosmicEnchantmentSpecs.PINPOINT;assertEquals(ModEnchantments.PINPOINT.identifier(),s.id());
        assertEquals(CosmicEnchantmentTier.ELITE,s.tier());assertEquals("bow",s.equipmentApplicability());
        assertEquals(6,s.maxLevel());assertFalse(s.randomPoolEligible());
        for(int level=1;level<=6;level++)assertEquals(level*.03,PinpointBehavior.chance(level),1e-12);
        assertEquals(80,PinpointBehavior.DURATION_TICKS);assertEquals(0,PinpointBehavior.AMPLIFIER);
    }
    @Test void treePoolsRetainDensityAndReachAllVariants(){
        assertEquals(4,json("data/cosmicpve/worldgen/configured_feature/woodlands_small_tree.json").getAsJsonObject("config").get("variants").getAsInt());
        assertEquals(3,json("data/cosmicpve/worldgen/configured_feature/woodlands_tall_tree.json").getAsJsonObject("config").get("variants").getAsInt());
        assertEquals(10,json("data/cosmicpve/worldgen/placed_feature/woodlands_small_tree.json").getAsJsonArray("placement").get(0).getAsJsonObject().get("count").getAsInt());
        assertEquals(3,json("data/cosmicpve/worldgen/placed_feature/woodlands_tall_tree.json").getAsJsonArray("placement").get(0).getAsJsonObject().get("chance").getAsInt());
        for(int i=1;i<=4;i++)assertNotNull(resource("data/cosmicpve/structure/woodlands/woodlands_small_tree"+i+".nbt"));
        for(int i=1;i<=3;i++)assertNotNull(resource("data/cosmicpve/structure/woodlands/woodlands_tall_tree"+i+".nbt"));
    }
    @Test void campsiteAndArenaPlacementAreCanonical() throws Exception {
        assertEquals(69,json("data/cosmicpve/worldgen/placed_feature/woodlands_campsite.json").getAsJsonArray("placement").get(0).getAsJsonObject().get("chance").getAsInt());
        var placement=json("data/cosmicpve/worldgen/structure_set/woodlands_arena.json").getAsJsonObject("placement");
        assertEquals(64,placement.get("spacing").getAsInt());assertEquals(16,placement.get("separation").getAsInt());
        var tag=NbtIo.readCompressed(resource("data/cosmicpve/structure/woodlands/woodlands_arena.nbt"),NbtAccounter.unlimitedHeap());
        var palette=tag.getListOrEmpty("palette");int lecterns=0,lights=0;
        for(var value:tag.getListOrEmpty("blocks")){var b=(CompoundTag)value;var state=palette.getCompoundOrEmpty(b.getIntOr("state",0));
            String name=state.getStringOr("Name","");if(name.equals("minecraft:lectern"))lecterns++;if(name.equals("minecraft:light"))lights++;}
        assertTrue(lecterns>=1);assertEquals(4,lights);
        var arena=json("data/cosmicpve/worldgen/structure/woodlands_arena.json");
        assertEquals("beard_thin",arena.get("terrain_adaptation").getAsString(),
                "native jigsaw terrain adaptation must blend the arena into nearby terrain");
        int previousProjectedOffset=0;
        assertEquals(previousProjectedOffset-1,arena.getAsJsonObject("start_height").get("absolute").getAsInt(),
                "the projected terrain anchor must be lowered by exactly one block");
        var pool=json("data/cosmicpve/worldgen/template_pool/woodlands_arena.json");
        assertEquals("rigid",pool.getAsJsonArray("elements").get(0).getAsJsonObject()
                .getAsJsonObject("element").get("projection").getAsString(),
                "terrain blending must not vertically deform the authored arena template");
    }
    @Test void arenaLocatorUsesThePlacementGridWithoutAWorldScan(){
        var placement=new RandomSpreadStructurePlacement(64,16,RandomSpreadType.LINEAR,1847365297);
        var origin=new BlockPos(12_345,80,-54_321);
        var first=WoodlandsArenaLocator.nearestCandidate(placement,8675309L,origin);
        var second=WoodlandsArenaLocator.nearestCandidate(placement,8675309L,origin);
        assertEquals(first,second);
        var candidates=WoodlandsArenaLocator.candidates(placement,8675309L,origin);
        assertEquals(57,candidates.size());assertEquals(57,new HashSet<>(candidates).size());assertEquals(first,candidates.getFirst());
        assertTrue(Math.abs(first.getX()-origin.getX())<2048);
        assertTrue(Math.abs(first.getZ()-origin.getZ())<2048);
    }
    @Test void tombRecipeAndMenuDefinitions(){
        var recipe=json("data/cosmicpve/recipe/desecrated_tomb_ranger.json");assertEquals(List.of("SSS","SSS","SSS"),
                recipe.getAsJsonArray("pattern").asList().stream().map(JsonElement::getAsString).toList());
        assertEquals("cosmicpve:dense_woodlands_scrap",recipe.getAsJsonObject("key").get("S").getAsString());
        assertEquals("cosmicpve:desecrated_tomb_ranger",recipe.getAsJsonObject("result").get("id").getAsString());
        assertEquals(List.of("adventures","adventure","adv"),AdventureInfoCommands.ALIASES);
        assertEquals(List.of(11,13,15),AdventureInfoMenu.ENTRY_SLOTS);assertEquals(3,AdventureDefinition.ALL.size());
    }
    @Test void advancedCatalogHasExactWeightsAndQuantities(){
        var rows=json("data/cosmicpve/cosmicpve/reward_tables/adventure/advanced_dense_woodlands.json").getAsJsonArray("entries");
        var expected=JsonParser.parseString("""
                [
                  {"weight":6,"minimum_quantity":3,"maximum_quantity":3,"reward":{"type":"unexamined_book","rarity":"simple"}},
                  {"weight":6,"minimum_quantity":3,"maximum_quantity":3,"reward":{"type":"unexamined_book","rarity":"unique"}},
                  {"weight":6,"minimum_quantity":3,"maximum_quantity":3,"reward":{"type":"unexamined_book","rarity":"elite"}},
                  {"weight":5,"reward":{"type":"random_vkit_crystal"}},
                  {"weight":8,"reward":{"type":"space_chest","rarity":"ultimate"}},
                  {"weight":6,"reward":{"type":"space_chest","rarity":"legendary"}},
                  {"weight":4,"reward":{"type":"space_chest","rarity":"mastery"}},
                  {"weight":5,"reward":{"type":"armor_set_crystal","armor_set":"cosmicpve:ranger","success_rate":100}},
                  {"weight":5,"reward":{"type":"xp_bottle","experience":20000}},
                  {"weight":5,"reward":{"type":"xp_bottle","experience":30000}},
                  {"weight":5,"reward":{"type":"armor_orb","success_rate":100}},
                  {"weight":5,"reward":{"type":"weapon_orb","success_rate":100}},
                  {"weight":4,"reward":{"type":"static_item","item":"cosmicpve:secret_weapon_cache"}},
                  {"weight":4,"reward":{"type":"skip_two_portal"}},
                  {"weight":4,"reward":{"type":"madness_three_portal"}},
                  {"weight":5,"reward":{"type":"static_item","item":"cosmicpve:mystery_mastery_spawner"}},
                  {"weight":5,"reward":{"type":"static_item","item":"cosmicpve:heroic_crystal"}},
                  {"weight":15,"reward":{"type":"random_ranger_armor"}},
                  {"weight":8,"reward":{"type":"armor_set_crystal","armor_set":"cosmicpve:ranger","success_rate":75}},
                  {"weight":5,"minimum_quantity":2,"maximum_quantity":2,"reward":{"type":"static_item","item":"cosmicpve:space_dust_bundle"}},
                  {"weight":7,"reward":{"type":"advanced_banknote"}},
                  {"weight":7,"reward":{"type":"pinpoint_book"}}
                ]
                """).getAsJsonArray();
        assertEquals(expected,rows,"the complete ordered Advanced Dense Woodlands catalog must match canon exactly");
        assertEquals(22,rows.size());int[] weights={6,6,6,5,8,6,4,5,5,5,5,5,4,4,4,5,5,15,8,5,7,7};
        for(int i=0;i<weights.length;i++)assertEquals(weights[i],rows.get(i).getAsJsonObject().get("weight").getAsInt());
        for(int i=0;i<3;i++){assertEquals(3,rows.get(i).getAsJsonObject().get("minimum_quantity").getAsInt());assertEquals(3,rows.get(i).getAsJsonObject().get("maximum_quantity").getAsInt());}
        assertEquals(2,rows.get(19).getAsJsonObject().get("minimum_quantity").getAsInt());
        assertEquals(2,rows.get(19).getAsJsonObject().get("maximum_quantity").getAsInt());
        assertReward(rows,7,"armor_set_crystal",100);assertReward(rows,18,"armor_set_crystal",75);
        assertEquals("cosmicpve:ranger",rows.get(7).getAsJsonObject().getAsJsonObject("reward").get("armor_set").getAsString());
        assertEquals("cosmicpve:ranger",rows.get(18).getAsJsonObject().getAsJsonObject("reward").get("armor_set").getAsString());
        var serialized=rows.toString();assertFalse(serialized.contains("10000"));assertFalse(serialized.contains("cosmic_enchantment_table"));
        assertEquals(100_000_000L,AdvancedWoodlandsRewards.MIN_CENTS);assertEquals(400_000_000L,AdvancedWoodlandsRewards.MAX_CENTS);
        assertEquals(1_000_000L,AdvancedWoodlandsRewards.STEP_CENTS);
        assertEquals(100_000_000L,AdvancedWoodlandsRewards.banknoteCents(0));
        assertEquals(237_000_000L,AdvancedWoodlandsRewards.banknoteCents(137));
        assertEquals(400_000_000L,AdvancedWoodlandsRewards.banknoteCents(300));
        assertThrows(IllegalArgumentException.class,()->AdvancedWoodlandsRewards.banknoteCents(301));
    }
    @Test void arenaBoundsUseAllSixPlacedStructureFaces(){
        var bounds=new net.minecraft.world.level.levelgen.structure.BoundingBox(10,20,30,19,29,39);
        assertTrue(com.cosmicpve.entity.woodlands.CosmicRangerEntity.contains(bounds,new net.minecraft.world.phys.AABB(10,20,30,20,30,40)));
        assertFalse(com.cosmicpve.entity.woodlands.CosmicRangerEntity.contains(bounds,new net.minecraft.world.phys.AABB(9.99,20,30,11,22,31)));
        assertFalse(com.cosmicpve.entity.woodlands.CosmicRangerEntity.contains(bounds,new net.minecraft.world.phys.AABB(19,20,30,20.01,22,31)));
        assertFalse(com.cosmicpve.entity.woodlands.CosmicRangerEntity.contains(bounds,new net.minecraft.world.phys.AABB(10,20,29.99,11,22,31)));
        assertFalse(com.cosmicpve.entity.woodlands.CosmicRangerEntity.contains(bounds,new net.minecraft.world.phys.AABB(10,20,39,11,22,40.01)));
        assertFalse(com.cosmicpve.entity.woodlands.CosmicRangerEntity.contains(bounds,new net.minecraft.world.phys.AABB(10,19.99,30,11,22,31)));
        assertFalse(com.cosmicpve.entity.woodlands.CosmicRangerEntity.contains(bounds,new net.minecraft.world.phys.AABB(10,29,30,11,30.01,31)));
    }
    @Test void arenaExteriorPreservesTerrainAndTreesRejectTheWholeHorizontalFootprint() throws Exception {
        var tag=NbtIo.readCompressed(resource("data/cosmicpve/structure/woodlands/woodlands_arena.nbt"),NbtAccounter.unlimitedHeap());
        var palette=tag.getListOrEmpty("palette");int air=-1,structureVoid=-1;
        for(int i=0;i<palette.size();i++){
            String name=palette.getCompoundOrEmpty(i).getStringOr("Name","");
            if(name.equals("minecraft:air"))air=i;if(name.equals("minecraft:structure_void"))structureVoid=i;
        }
        assertTrue(air>=0);assertTrue(structureVoid>=0);
        int interiorAir=0,exteriorVoid=0;
        for(var value:tag.getListOrEmpty("blocks")){
            var block=(CompoundTag)value;var pos=block.getListOrEmpty("pos");
            int x=pos.getIntOr(0,0),z=pos.getIntOr(2,0),state=block.getIntOr("state",0);
            int radiusSquared=(x-18)*(x-18)+(z-18)*(z-18);
            if(radiusSquared>325){assertNotEquals(air,state,"circle-exterior cells must never carve terrain");if(state==structureVoid)exteriorVoid++;}
            else if(state==air)interiorAir++;
        }
        assertEquals(5_376,exteriorVoid);assertTrue(interiorAir>15_000,"intentional arena/interior clearing air remains");
        assertTrue(WoodlandTemplateFeature.isTreeFamily("small_tree"));
        assertTrue(WoodlandTemplateFeature.isTreeFamily("tall_tree"));
        assertTrue(WoodlandTemplateFeature.isTreeFamily("fallen_tree"));
        var arena=new net.minecraft.world.level.levelgen.structure.BoundingBox(100,50,100,136,65,136);
        assertTrue(WoodlandTemplateFeature.horizontallyIntersects(arena,
                new net.minecraft.world.level.levelgen.structure.BoundingBox(136,200,120,142,220,126)),
                "tree rejection ignores Y and includes the footprint boundary");
        assertFalse(WoodlandTemplateFeature.horizontallyIntersects(arena,
                new net.minecraft.world.level.levelgen.structure.BoundingBox(137,50,120,142,65,126)));
    }
    private static void assertReward(JsonArray rows,int index,String type,int rate){var reward=rows.get(index).getAsJsonObject().getAsJsonObject("reward");assertEquals(type,reward.get("type").getAsString());assertEquals(rate,reward.get("success_rate").getAsInt());}
    private JsonObject json(String path){try(var r=new InputStreamReader(resource(path))){return JsonParser.parseReader(r).getAsJsonObject();}catch(IOException e){throw new UncheckedIOException(e);}}
    private InputStream resource(String path){var in=getClass().getClassLoader().getResourceAsStream(path);assertNotNull(in,path);return in;}
}
