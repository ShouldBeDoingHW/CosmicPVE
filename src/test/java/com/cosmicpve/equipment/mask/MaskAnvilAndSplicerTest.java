package com.cosmicpve.equipment.mask;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.CosmicPVE;
import com.cosmicpve.content.ContentSnapshot;
import com.cosmicpve.content.CosmicContentRepository;
import com.cosmicpve.content.definition.mask.MaskBehavior;
import com.cosmicpve.content.definition.mask.MaskDefinition;
import com.cosmicpve.content.validation.ValidationResult;
import com.cosmicpve.registry.ModItems;
import com.mojang.serialization.JsonOps;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

class MaskAnvilAndSplicerTest {
    @Test void vanillaPickupGateIsBoundedToOneLevelForImmediateNetZeroRefund() {
        assertEquals(1,MaskAnvilEventBridge.PICKUP_GATE_COST);
    }
    @Test void anvilFlattensEverySupportedSingleAndMultiCombinationInLeftThenRightOrder() {
        var content=content(); var service=new MaskAnvilService();
        for (var sizes : List.of(new int[]{1,1},new int[]{1,2},new int[]{2,1},new int[]{2,2},new int[]{3,2})) {
            int total=sizes[0]+sizes[1]; var ids=ids(total);
            var left=MaskItemFactory.create(ids.subList(0,sizes[0]),content);
            var right=MaskItemFactory.create(ids.subList(sizes[0],total),content);
            var result=service.combine(left,right,total,content);
            assertEquals(MaskAnvilService.Outcome.SUCCESS,result.outcome());
            assertEquals(ids,result.maskIds());
            assertEquals(ids,result.output().get(com.cosmicpve.registry.ModDataComponents.MASK_ITEM.get()).maskIds());
            assertEquals(0,result.output().getOrDefault(net.minecraft.core.component.DataComponents.REPAIR_COST,0));
            assertEquals(1,left.getCount()); assertEquals(1,right.getCount());
        }
    }
    @Test void anvilRejectsDuplicatesAndCurrentLimitWithoutChangingSchemaValidity() {
        var content=content(); var service=new MaskAnvilService();
        assertEquals(MaskAnvilService.Outcome.DUPLICATE,service.combine(
                MaskItemFactory.create(ids(2),content),MaskItemFactory.create(ids(2).subList(1,2),content),5,content).outcome());
        assertEquals(MaskAnvilService.Outcome.TOO_MANY,service.combine(
                MaskItemFactory.create(ids(2),content),MaskItemFactory.create(ids(4).subList(2,4),content),3,content).outcome());
        assertEquals(MaskAnvilService.Outcome.TOO_MANY,service.combine(
                MaskItemFactory.create(ids(3),content),MaskItemFactory.create(ids(6).subList(3,6),content),5,content).outcome());
        assertTrue(new com.cosmicpve.data.component.MaskLoadout(ids(5)).valid(),"existing five-mask data remains valid");
        assertEquals(MaskAnvilService.Outcome.TOO_MANY,service.combine(
                MaskItemFactory.create(ids(3),content),MaskItemFactory.create(ids(5).subList(3,5),content),2,content).outcome());
    }
    @Test void splicerSupportsTwoThreeAndFiveIndependentlyOfCreationLimit() {
        var content=content(); var service=new MaskSplicerService(content);
        for (int count : List.of(2,3,5)) {
            var ids=ids(count); var splicer=new ItemStack(ModItems.MASK_SPLICER.get());
            var multi=MaskItemFactory.create(ids,content); var result=service.split(splicer,multi,splicer,multi);
            assertEquals(MaskSplicerService.Outcome.SUCCESS,result.outcome());
            assertEquals(count,result.outputs().size());
            assertEquals(ids,result.outputs().stream().map(stack -> stack.get(
                    com.cosmicpve.registry.ModDataComponents.MASK_ITEM.get()).maskIds().getFirst()).toList());
        }
    }
    @Test void splicerConsumesOneAndReturnsOrderedSinglesWhileRejectingSingleAndStaleTargets() {
        var content=content(); var service=new MaskSplicerService(content); var ids=ids(5);
        ItemStack splicer=new ItemStack(ModItems.MASK_SPLICER.get()); ItemStack multi=MaskItemFactory.create(ids,content);
        var success=service.split(splicer,multi,splicer,multi);
        assertEquals(MaskSplicerService.Outcome.SUCCESS,success.outcome());
        assertTrue(splicer.isEmpty()); assertTrue(multi.isEmpty()); assertEquals(5,success.outputs().size());
        for (int i=0;i<5;i++) assertEquals(List.of(ids.get(i)),success.outputs().get(i)
                .get(com.cosmicpve.registry.ModDataComponents.MASK_ITEM.get()).maskIds());
        ItemStack nextSplicer=new ItemStack(ModItems.MASK_SPLICER.get()); ItemStack single=MaskItemFactory.create(List.of(ids.getFirst()),content);
        assertEquals(MaskSplicerService.Outcome.SINGLE_MASK,service.split(nextSplicer,single,nextSplicer,single).outcome());
        assertEquals(1,nextSplicer.getCount()); assertEquals(1,single.getCount());
        assertEquals(MaskSplicerService.Outcome.STALE,service.split(nextSplicer,single,nextSplicer.copy(),single).outcome());
    }
    @Test void persistedLimitDefaultsToFiveAndAcceptsOnlyTwoThroughFive() {
        var defaults=MaskLimitSavedData.CODEC.parse(JsonOps.INSTANCE,new com.google.gson.JsonObject()).getOrThrow();
        assertEquals(5,defaults.limit());
        for (int limit=2;limit<=5;limit++) {
            var data=new com.google.gson.JsonObject(); data.addProperty("mask_limit",limit);
            assertEquals(limit,MaskLimitSavedData.CODEC.parse(JsonOps.INSTANCE,data).getOrThrow().limit());
        }
        var low=new com.google.gson.JsonObject(); low.addProperty("mask_limit",1);
        var high=new com.google.gson.JsonObject(); high.addProperty("mask_limit",6);
        assertTrue(MaskLimitSavedData.CODEC.parse(JsonOps.INSTANCE,low).error().isPresent());
        assertTrue(MaskLimitSavedData.CODEC.parse(JsonOps.INSTANCE,high).error().isPresent());
        var data=new com.google.gson.JsonObject(); data.addProperty("mask_limit",5);
        var roundTrip=MaskLimitSavedData.CODEC.parse(JsonOps.INSTANCE,
                MaskLimitSavedData.CODEC.encodeStart(JsonOps.INSTANCE,
                        MaskLimitSavedData.CODEC.parse(JsonOps.INSTANCE,data).getOrThrow()).getOrThrow()).getOrThrow();
        assertEquals(5,roundTrip.limit());
    }
    private static List<net.minecraft.resources.Identifier> ids(int count) {
        return java.util.stream.IntStream.range(0,count).mapToObj(i -> CosmicPVE.id("mask_"+i)).toList();
    }
    private static CosmicContentRepository content() {
        var definitions=new LinkedHashMap<net.minecraft.resources.Identifier,MaskDefinition>();
        var behaviors=MaskBehavior.values();
        for (int i=0;i<6;i++) { var id=CosmicPVE.id("mask_"+i); definitions.put(id,new MaskDefinition(id,
                Component.literal("Mask "+i),Component.literal("Effect "+i),i+1,MaskProfiles.MULTI_TEXTURE,behaviors[i])); }
        var repository=new CosmicContentRepository();
        assertTrue(repository.publish(ValidationResult.success(new ContentSnapshot(0,Map.of(),Map.of(),Map.of(),Map.of(),Map.of(),definitions))));
        return repository;
    }
}
