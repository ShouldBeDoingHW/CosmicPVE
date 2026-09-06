package com.cosmicpve.adventure;

import com.cosmicpve.registry.ModItems;
import com.cosmicpve.reward.lootbox.AnimatedLootboxItem;
import com.cosmicpve.reward.lootbox.Step8ELootboxService;
import com.cosmicpve.economy.flashsale.FlashSaleCatalog;
import com.cosmicpve.trial.TrialRewardCatalogs;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import org.junit.jupiter.api.Test;
import java.io.InputStreamReader;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MysteryCallTest {
    @Test void presentationUsesRailForcedGlintAndExactStyledText() throws Exception {
        var item=ModItems.MYSTERY_CALL_OF_ADVENTURE.get(); var stack=new ItemStack(item);
        assertEquals("cosmicpve:mystery_call_of_adventure",BuiltInRegistries.ITEM.getKey(item).toString());
        assertTrue(item.isFoil(stack));
        var name=item.getName(stack);assertEquals("Mystery Call of Adventure",name.getString());
        assertTrue(name.getStyle().isBold());assertEquals(0x55FFFF,name.getStyle().getColor().getValue());
        var lore=AnimatedLootboxItem.mysteryCallLore();
        assertEquals(List.of("A distant path is waiting to answer.","Contains one random Call of Adventure.","RIGHT-CLICK TO REVEAL"),lore.stream().map(c->c.getString()).toList());
        assertTrue(lore.get(0).getStyle().isItalic());assertFalse(lore.get(1).getStyle().isItalic());
        assertEquals(0xAAAAAA,lore.get(0).getStyle().getColor().getValue());
        assertEquals(0xAAAAAA,lore.get(1).getStyle().getColor().getValue());
        assertTrue(lore.get(2).getStyle().isBold());assertEquals(0x55FFFF,lore.get(2).getStyle().getColor().getValue());
        try(var stream=getClass().getClassLoader().getResourceAsStream("assets/cosmicpve/items/mystery_call_of_adventure.json")) {
            assertNotNull(stream);var model=JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject().getAsJsonObject("model");
            assertEquals("minecraft:model",model.get("type").getAsString());assertEquals("minecraft:item/rail",model.get("model").getAsString());
        }
    }
    @Test void everyRegisteredProductionSkuIsUniformlyReachableAndFaithful() {
        var pool=ModItems.productionCalls();
        assertEquals(List.of("cosmicpve:call_of_forest_10","cosmicpve:call_of_forest_20","cosmicpve:call_of_forest_30"),pool.stream().map(s->BuiltInRegistries.ITEM.getKey(s.getItem()).toString()).toList());
        for(int i=0;i<pool.size();i++) {
            final int pick=i;
            var reward=Step8ELootboxService.selectCall(pool,new LegacyRandomSource(0) {
                @Override public int nextInt(int bound) {assertEquals(pool.size(),bound);return pick;}
            });
            assertTrue(ItemStack.matches(pool.get(i),reward));assertEquals(1,reward.getCount());
            reward.shrink(1);assertEquals(1,ModItems.productionCalls().get(i).getCount());
        }
    }
    @Test void trialTiersAndSingleFlashSaleRowRetainExactWeightsAndPrices() {
        for(var catalog:List.of(TrialRewardCatalogs.APPRENTICE,TrialRewardCatalogs.HARDCORE,TrialRewardCatalogs.IMPOSSIBLE,TrialRewardCatalogs.DEMONIC)) {
            var rows=catalog.declared().stream().filter(r->r.name().equals("Mystery Call of Adventure")).toList();
            int weight=catalog==TrialRewardCatalogs.HARDCORE?7:catalog==TrialRewardCatalogs.IMPOSSIBLE?9:0;
            assertEquals(weight==0?0:1,rows.size());
            if(weight>0){assertEquals(weight,rows.getFirst().weight());assertTrue(rows.getFirst().active());}
        }
        var rows=FlashSaleCatalog.productionRows().stream().filter(r->r.id().equals("mystery_call_of_adventure")).toList();
        assertEquals(1,rows.size());var row=rows.getFirst();assertEquals(1,row.quantity());
        assertEquals(90_000_000L,row.lowPrice());assertEquals(115_000_000L,row.mediumPrice());assertEquals(145_000_000L,row.highPrice());
        var result=row.create(net.minecraft.util.RandomSource.create(7)).orElseThrow();
        assertEquals(1,result.size());assertEquals(1,result.getFirst().getCount());assertTrue(result.getFirst().is(ModItems.MYSTERY_CALL_OF_ADVENTURE.get()));
        assertEquals(100,DenseWoodlandsSessionService.COMPASS_COOLDOWN_TICKS);
    }
}
