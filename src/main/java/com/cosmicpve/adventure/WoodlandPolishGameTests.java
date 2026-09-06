package com.cosmicpve.adventure;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.registry.ModAttachments;
import com.cosmicpve.trial.TrialDecisionEntryService;
import com.cosmicpve.trial.persistence.TrialInventoryTransactionService;
import com.cosmicpve.trial.room.WarzoneGiantsService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import java.util.*;

/** Loaded-world regression checks for Step 8O.3, executed on an isolated dedicated server. */
public final class WoodlandPolishGameTests {
    private WoodlandPolishGameTests() {}

    public static void grounding(GameTestHelper h) {
        var level = h.getLevel().getServer().getLevel(com.cosmicpve.trial.TrialRuntime.INSTANCE_DIMENSION); int index = 0;
        for (int relief : new int[]{0,1,2,4,-1}) for (int variant=1;variant<=3;variant++) for (Rotation rotation : Rotation.values()) {
            var testPos = h.absolutePos(new BlockPos(60 + index++ * 40, 0, 0));
            var origin = new BlockPos(testPos.getX(),80,testPos.getZ());
            for(int x=-15;x<=15;x++)for(int z=-15;z<=15;z++) {
                int surface=Math.abs(x)+Math.abs(z)>5 ? Math.max(0,relief) : 0;
                for(int y=-2;y<10;y++)level.setBlock(origin.offset(x,y,z),
                        (y<=surface ? Blocks.GRASS_BLOCK : relief<0 && y==1 ? Blocks.WATER : Blocks.AIR).defaultBlockState(),2);
            }
            for(int cx=(origin.getX()-15)>>4;cx<=(origin.getX()+15)>>4;cx++)
                for(int cz=(origin.getZ()-15)>>4;cz<=(origin.getZ()+15)>>4;cz++)
                    Heightmap.primeHeightmaps(level.getChunk(cx,cz),EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG));
            String name = "woodlands_campsite"+variant;
            var context = new FeaturePlaceContext<WoodlandTemplateFeature.Config>(Optional.empty(),level,
                    level.getChunkSource().getGenerator(),RandomSource.create(index),origin.above(),new WoodlandTemplateFeature.Config("campsite",3));
            boolean accepted=WoodlandTemplateFeature.placeTemplate(context,name,rotation);
            if(relief==4 || relief<0) {
                h.assertTrue(!accepted,"Reject cliff/water "+name+rotation+" relief="+relief);
                continue;
            }
            h.assertTrue(accepted,"Place audited variant/rotation "+name+rotation+" relief="+relief);
            var placed = WoodlandTemplateFeature.recent().getLast();
            h.assertTrue(placed.origin().getY()==origin.getY(),"Authored soil must intersect target grass, not air above it");
            inspectCamp(h,level,placed);
        }
        CosmicPVE.LOGGER.info("STEP8O4_ANCHORS all 3 variants x 4 rotations x flat/1/2-block relief accepted; cliffs/water rejected: soil flush, supported, barrels clear");
        h.succeed();
    }

    private static void inspectCamp(GameTestHelper h,ServerLevel level,WoodlandTemplateFeature.Placement placed) {
        var template=level.getStructureManager().get(CosmicPVE.id("woodlands/"+placed.variant())).orElseThrow();
        var tag=template.save(new CompoundTag()); var palette=tag.getListOrEmpty("palette");
        var settings=new StructurePlaceSettings().setRotation(placed.rotation()); int floor=0,barrels=0;
        for(var value:tag.getListOrEmpty("blocks")) {
            var cell=(CompoundTag)value; var p=cell.getListOrEmpty("pos");
            var state=palette.getCompoundOrEmpty(cell.getIntOr("state",0));
            String name=state.getStringOr("Name","");
            var local=new BlockPos(p.getIntOr(0,0),p.getIntOr(1,0),p.getIntOr(2,0));
            var at=placed.origin().offset(StructureTemplate.calculateRelativePosition(settings,local));
            if(local.getY()==0 && Set.of("minecraft:grass_block","minecraft:podzol","minecraft:coarse_dirt","minecraft:dirt").contains(name)) {
                floor++;
                h.assertTrue(level.getBlockState(at).is(BlockTags.DIRT),"Authored floor survives at "+at);
                h.assertTrue(level.getBlockState(at.below()).is(BlockTags.DIRT),"No floating floor at "+at);
                h.assertTrue(at.getY()<=placed.minSurface()-1,"No raised downhill pedestal at "+at);
            }
            if(name.equals("minecraft:barrel")) {
                barrels++;
                h.assertTrue(local.getY()==1 && level.getBlockEntity(at) instanceof RandomizableContainerBlockEntity,"Barrel remains present above floor");
                h.assertTrue(!level.getBlockState(at.above()).is(BlockTags.DIRT),"Barrel is not buried at "+at);
                var barrel=(RandomizableContainerBlockEntity)level.getBlockEntity(at);
                h.assertTrue(barrel.getLootTable()!=null && barrel.getLootTable().identifier().equals(CosmicPVE.id("chests/adventure/dense_woodlands")),"Canonical lazy campsite loot");
            }
        }
        h.assertTrue(floor>=74 && barrels==1,"Expected authored floor and barrel");
    }

    public static void freshWorld(GameTestHelper h) {
        var level=h.getLevel().getServer().getLevel(DenseWoodlandsSessionService.DIMENSION);
        int start=Integer.getInteger("cosmicpve.polishSampleChunk",500),width=32;
        var result=WoodlandProbe.sample(level,start,start,width);
        var counts=new TreeMap<String,Integer>(); var camps=new TreeMap<String,Integer>();
        for(var placed:WoodlandTemplateFeature.recentCamps()) {
            var p=placed.origin();
            if(p.getX()<start*16 || p.getX()>=(start+width)*16 || p.getZ()<start*16 || p.getZ()>=(start+width)*16)continue;
            if(placed.variant().contains("campsite")) { inspectCamp(h,level,placed);camps.merge(placed.variant(),1,Integer::sum); }
        }
        for(int x=start*16;x<(start+width)*16;x++)for(int z=start*16;z<(start+width)*16;z++) {
            int y=level.getHeight(Heightmap.Types.WORLD_SURFACE,x,z)-1;
            for(;y>=30;y--) {
                var state=level.getBlockState(new BlockPos(x,y,z));
                if(state.is(BlockTags.DIRT))break;
                if(state.is(Blocks.SHORT_GRASS)||state.is(Blocks.TALL_GRASS)||state.is(BlockTags.SMALL_FLOWERS)
                        ||state.is(Blocks.PUMPKIN)||state.is(Blocks.BROWN_MUSHROOM)||state.is(Blocks.RED_MUSHROOM)||state.is(Blocks.SUGAR_CANE))
                    counts.merge(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(),1,Integer::sum);
            }
        }
        var plains=level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS).value();
        var woods=level.getBiome(new BlockPos(start*16,80,start*16)).value();
        h.assertTrue(plains.getGrassColor(0,0)==woods.getGrassColor(0,0) && plains.getFoliageColor()==woods.getFoliageColor(),"Actual loaded Plains tint equality");
        h.assertTrue(counts.getOrDefault("minecraft:short_grass",0)>0 && counts.getOrDefault("minecraft:tall_grass",0)>0,"Both grass sizes physically generated");
        h.assertTrue(counts.entrySet().stream().anyMatch(e-> !e.getKey().contains("grass")&&!e.getKey().contains("mushroom")&&!e.getKey().contains("pumpkin")&&!e.getKey().contains("sugar_cane")),"Flowers physically generated");
        h.assertTrue(camps.size()==3,"All three campsites naturally generated in fresh sample");
        CosmicPVE.LOGGER.info("STEP8O4_FRESH chunks={}..{} columns={} terrain={} camps={} vegetation={} grassColor={} foliageColor={}",
                start,start+width-1,width*width*256,result,camps,counts,woods.getGrassColor(0,0),woods.getFoliageColor());
        h.succeed();
    }

    public static void decisionRestore(GameTestHelper h) {
        var service=new TrialDecisionEntryService(); var inventories=new TrialInventoryTransactionService();
        for(int member=0;member<4;member++) {
            var p=AdventureGameTests.player(h,"restore"+member);
            p.giveExperiencePoints(123);p.getInventory().setItem(0,new ItemStack(Items.DIAMOND,3));
            h.assertTrue(inventories.enter(p,UUID.randomUUID()),"Real Trial outside snapshot created");
            p.getInventory().setItem(0,new ItemStack(Items.WOODEN_SWORD));
            var snapshot=p.getData(ModAttachments.TRIAL_PLAYER_STATE);
            var inventory=p.getInventory().getNonEquipmentItems().stream().map(ItemStack::copy).toList();
            var trial=com.cosmicpve.trial.TrialRuntime.sessions().active(p.level().getServer());
            for(int repeat=0;repeat<5;repeat++) {
                p.setHealth(3);p.getFoodData().setFoodLevel(2);p.getFoodData().setSaturation(0);
                for(var effect:List.of(MobEffects.SPEED,MobEffects.HEALTH_BOOST,MobEffects.POISON,MobEffects.HUNGER))
                    p.addEffect(new MobEffectInstance(effect,400,1));
                var at=h.absolutePos(new BlockPos(23,14,23));
                service.enter(p,()->p.teleportTo(h.getLevel(),at.getX()+.5,at.getY(),at.getZ()+.5,Set.of(),0,0,true),()->{
                    h.assertTrue(p.getHealth()==p.getMaxHealth(),"Full health before Decision interaction");
                    h.assertTrue(p.getFoodData().getFoodLevel()==20 && p.getFoodData().getSaturationLevel()==1.5F,"Exact /restore food semantics");
                    h.assertTrue(p.getActiveEffects().isEmpty(),"All effects cleared before interaction");
                });
                h.assertTrue(snapshot.equals(p.getData(ModAttachments.TRIAL_PLAYER_STATE)),"Outside inventory/XP snapshot unchanged");
                for(int slot=0;slot<inventory.size();slot++)h.assertTrue(ItemStack.matches(inventory.get(slot),p.getInventory().getNonEquipmentItems().get(slot)),"Trial inventory unchanged");
                h.assertTrue(trial.equals(com.cosmicpve.trial.TrialRuntime.sessions().active(p.level().getServer())),"Restore does not mutate session timer, loot, Fame or progress");
            }
            inventories.restore(p);p.discard();
        }
        var zombie=EntityType.ZOMBIE.create(h.getLevel(),EntitySpawnReason.EVENT);
        double old=zombie.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();
        WarzoneGiantsService.applyMovementAndDamage(zombie);
        h.assertTrue(Math.abs(zombie.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue()-old-.05)<1e-12,"Exactly +0.05 movement");
        h.assertTrue(zombie.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue()==1,"Old zero base attack becomes one");
        CosmicPVE.LOGGER.info("STEP8O3_DECISION four players x five entries restored/effect-free, inventory/XP/session preserved; Giant speed {} -> {}, base attack 0 -> 1",old,zombie.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue());
        zombie.discard();h.succeed();
    }
}
