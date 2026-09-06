package com.cosmicpve.economy.fame;

import com.cosmicpve.registry.ModItems;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

/** Isolated GameTest probe of the production aggregate, using its existing seeded seam. */
public final class MysteryCallFameProbe {
    private MysteryCallFameProbe() {}
    public static void verify(GameTestHelper h,ServerPlayer player) {
        var prices=new java.util.HashSet<Long>();var service=new FameShopService();
        for(int seed=0;seed<300 && prices.size()<2;seed++) {
            var catalog=service.generate(player,0,RandomSource.create(seed));
            h.assertTrue(catalog.offers().size()==9,"Fame still materializes nine offers");
            var offers=catalog.offers().stream().filter(o->o.payload().stream().anyMatch(s->s.is(ModItems.MYSTERY_CALL_OF_ADVENTURE.get()))).toList();
            h.assertTrue(offers.size()<=1,"Identical unopened Mystery payloads remain distinct");
            for(var offer:offers) {
                h.assertTrue(offer.payload().size()==1 && offer.payload().getFirst().getCount()==1,"One unopened Mystery Call");
                h.assertTrue(offer.price()==FameShopService.priceFor(offer.sourceTier()),"Normal source-tier pricing");
                prices.add(offer.price());
            }
        }
        h.assertTrue(prices.equals(java.util.Set.of(35L,55L)),"Both Trial sources discoverable via normal aggregate: "+prices);
    }
}
