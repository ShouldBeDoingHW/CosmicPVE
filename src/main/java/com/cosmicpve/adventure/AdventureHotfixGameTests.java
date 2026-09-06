package com.cosmicpve.adventure;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.registry.ModAttachments;
import com.cosmicpve.registry.ModItems;
import com.cosmicpve.reward.animation.*;
import com.cosmicpve.economy.flashsale.*;
import com.cosmicpve.economy.MoneyService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import java.util.*;

/** Tests real item-use, menus, attachments, delivery, catalogs and targeted sound packets. */
public final class AdventureHotfixGameTests {
    private AdventureHotfixGameTests() {}
    public static void verify(GameTestHelper h) {
        var service=SingleRewardAnimationService.INSTANCE;
        for(var hand:InteractionHand.values()) {
            var p=AdventureGameTests.player(h,"mystery"+hand.ordinal());p.setPos(h.absolutePos(new BlockPos(3,3,3)).getCenter());
            var source=new ItemStack(ModItems.MYSTERY_CALL_OF_ADVENTURE.get(),3);p.setItemInHand(hand,source);
            var dimension=p.level().dimension();
            var used=source.use(p.level(),p,hand);
            h.assertTrue(used instanceof InteractionResult.Success,"Actual ItemStack.use accepted");
            if(used instanceof InteractionResult.Success success && success.heldItemTransformedTo()!=null)p.setItemInHand(hand,success.heldItemTransformedTo());
            h.assertTrue(source.getCount()==2,"One source consumed, no transformed-hand copy");
            var pending=p.getData(ModAttachments.LOOT_ANIMATION);var reward=pending.allRewards().getFirst();
            h.assertTrue(pending.valid() && pending.allRewards().size()==1 && canonical(reward),"One faithful production Call preselected");
            var saved=p.level().getServer().getPlayerList().getPlayerIo().load(new net.minecraft.server.players.NameAndId(p.getUUID(),p.getGameProfile().name())).orElseThrow();
            var reloaded=AdventureGameTests.player(h,"mysteryreload");
            reloaded.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,p.registryAccess(),saved));
            h.assertTrue(ItemStack.matches(reloaded.getData(ModAttachments.LOOT_ANIMATION).allRewards().getFirst(),reward)
                    && reloaded.getItemInHand(hand).getCount()==2,"Actual player-file checkpoint contains pending reward and consumed source together");
            h.assertTrue(p.containerMenu instanceof SingleRewardAnimationMenu,"Canonical nine-slot animation menu");
            h.assertTrue(DenseWoodlandsBootstrap.SESSIONS.session(p)==null && p.level().dimension().equals(dimension),"Opening does not start Adventure");
            var opposite=hand==InteractionHand.MAIN_HAND?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND;
            var second=new ItemStack(ModItems.MYSTERY_CALL_OF_ADVENTURE.get());p.setItemInHand(opposite,second);
            h.assertTrue(second.use(p.level(),p,opposite)==InteractionResult.FAIL && second.getCount()==1,"Other hand cannot reopen/consume");
            h.assertTrue(source.use(p.level(),p,hand)==InteractionResult.FAIL && source.getCount()==2,"Double-click cannot consume twice");
            var ops=p.registryAccess().createSerializationContext(NbtOps.INSTANCE);
            var encoded=PendingLootAnimation.CODEC.codec().encodeStart(ops,pending).getOrThrow();
            p.setData(ModAttachments.LOOT_ANIMATION,PendingLootAnimation.CODEC.codec().parse(ops,encoded).getOrThrow());
            if(hand==InteractionHand.OFF_HAND) {
                // Retire the disconnected in-memory copy without resolving it, then recover
                // the player reconstructed from the actual source/reward checkpoint.
                p.setData(ModAttachments.LOOT_ANIMATION,PendingLootAnimation.empty());p.closeContainer();p.discard();
                service.recover(reloaded);service.recover(reloaded);
                h.assertTrue(calls(reloaded)==1 && !reloaded.getData(ModAttachments.LOOT_ANIMATION).valid(),"Player-file recovery delivers persisted result once");
                reloaded.discard();continue;
            }
            reloaded.discard();p.closeContainer();service.recover(p);service.recover(p);
            h.assertTrue(calls(p)==1 && p.getInventory().getNonEquipmentItems().stream().anyMatch(s->ItemStack.matches(s,reward)),"Early close and repeated recovery deliver the saved result once");
            h.assertTrue(!p.getData(ModAttachments.LOOT_ANIMATION).valid(),"Resolved obligation cleared");p.discard();
        }
        var full=AdventureGameTests.player(h,"mysteryfull");full.setPos(h.absolutePos(new BlockPos(8,3,8)).getCenter());
        for(int slot=0;slot<36;slot++)full.getInventory().setItem(slot,new ItemStack(Items.STONE,64));
        full.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.MYSTERY_CALL_OF_ADVENTURE.get(),2));
        full.getOffhandItem().use(full.level(),full,InteractionHand.OFF_HAND);
        var expected=full.getData(ModAttachments.LOOT_ANIMATION).allRewards().getFirst();
        full.setHealth(0);full.closeContainer();h.assertTrue(full.getData(ModAttachments.LOOT_ANIMATION).valid(),"Death/closed menu retains recovery obligation");
        full.setHealth(full.getMaxHealth());service.recover(full);service.recover(full);
        var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,full.getBoundingBox().inflate(5),e->ItemStack.matches(e.getItem(),expected));
        h.assertTrue(drops.size()==1 && calls(full)==0,"Full inventory delivers exactly one real dropped reward");drops.forEach(ItemEntity::discard);full.discard();

        var normal=AdventureGameTests.player(h,"mysterynormal");normal.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.MYSTERY_CALL_OF_ADVENTURE.get()));
        normal.getMainHandItem().use(normal.level(),normal,InteractionHand.MAIN_HAND);
        var selected=normal.getData(ModAttachments.LOOT_ANIMATION).allRewards().getFirst();
        var menu=(SingleRewardAnimationMenu)normal.containerMenu;
        com.cosmicpve.economy.fame.MysteryCallFameProbe.verify(h,normal);
        var sale=new FlashSaleService();var server=h.getLevel().getServer();
        for(var tier:FlashSalePriceTier.values()) {
            var buyer=AdventureGameTests.player(h,"mysterybuyer"+tier.ordinal());var entry=FlashSaleCatalog.find("mystery_call_of_adventure").orElseThrow();
            new MoneyService().set(buyer,entry.price(tier));sale.start(server,entry,tier);
            h.assertTrue(sale.buy(buyer)==FlashSalePurchaseTransaction.Outcome.SUCCESS,"Actual Flash Sale purchase "+tier);
            h.assertTrue(sale.buy(buyer)==FlashSalePurchaseTransaction.Outcome.ALREADY_PURCHASED,"Once-per-player Flash Sale");
            h.assertTrue(buyer.getInventory().countItem(ModItems.MYSTERY_CALL_OF_ADVENTURE.get())==1 && calls(buyer)==0 && !buyer.getData(ModAttachments.LOOT_ANIMATION).valid(),"Purchase delivers unopened Mystery only");
            buyer.discard();
        }
        sale.close(server);
        var packets=new ArrayList<Packet<?>>();var compassPlayer=AdventureGameTests.player(h,"compasshotfix",packets::add);
        var woods=server.getLevel(DenseWoodlandsSessionService.DIMENSION);
        compassPlayer.teleportTo(woods,0,200,0,Set.of(),0,0,true);
        var session=new AdventureSession(compassPlayer.getUUID(),UUID.randomUUID(),10,AdventureSession.Phase.ACTIVE,
                new AdventureSession.ReturnPoint(h.getLevel().dimension(),compassPlayer.position(),0,0,GameType.SURVIVAL),
                BlockPos.ZERO,new BlockPos(30,80,30),server.overworld().getGameTime()+12000,0,1);
        AdventureSavedData.get(server).put(server,session);
        var compass=new ItemStack(ModItems.ADVENTURE_COMPASS.get());var tag=new CompoundTag();
        tag.putString("adventure_owner",session.owner().toString());tag.putString("adventure_session",session.id().toString());
        compass.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));compassPlayer.setItemInHand(InteractionHand.MAIN_HAND,compass);packets.clear();
        h.assertTrue(!DenseWoodlandsBootstrap.SESSIONS.hint(compassPlayer,new ItemStack(ModItems.ADVENTURE_COMPASS.get())) && packets.isEmpty(),"Unowned compass rejected silently");
        long now=server.overworld().getGameTime();
        h.assertTrue(compass.use(woods,compassPlayer,InteractionHand.MAIN_HAND) instanceof InteractionResult.Success,"First valid use");
        h.assertTrue(compassPlayer.getPersistentData().getLongOr(DenseWoodlandsSessionService.COMPASS_NEXT_HINT,0)==now+100,"Exactly 100 ticks");
        assertFeedback(h,packets);packets.clear();
        h.assertTrue(compass.use(woods,compassPlayer,InteractionHand.MAIN_HAND)==InteractionResult.FAIL && packets.isEmpty(),"Immediate rejected use silent");
        h.runAfterDelay(50,()->{service.tick(normal,menu);h.assertTrue(ItemStack.matches(selected,normal.getData(ModAttachments.LOOT_ANIMATION).allRewards().getFirst()) && calls(normal)==0,"Cosmetic previews cannot change or deliver preselected reward");});
        h.runAfterDelay(99,()->{
            var result=compass.use(woods,compassPlayer,InteractionHand.MAIN_HAND);
            h.assertTrue(result==InteractionResult.FAIL && packets.stream().noneMatch(p->p instanceof ClientboundSoundPacket || p instanceof ClientboundSystemChatPacket),
                    "Tick 99 rejected without repeated action/sound; elapsed="+(server.overworld().getGameTime()-now)+" result="+result+" packets="+packets.stream().map(p->p.getClass().getSimpleName()).toList());
            packets.clear();
        });
        h.runAfterDelay(100,()->{
            h.assertTrue(compass.use(woods,compassPlayer,InteractionHand.MAIN_HAND) instanceof InteractionResult.Success,"Tick 100 succeeds");assertFeedback(h,packets);
            h.assertTrue(compassPlayer.getPersistentData().getLongOr(DenseWoodlandsSessionService.COMPASS_NEXT_HINT,0)==server.overworld().getGameTime()+100,"New 100-tick deadline");
        });
        h.runAfterDelay(121,()->{
            service.tick(normal,menu);service.recover(normal);
            h.assertTrue(calls(normal)==1 && normal.getInventory().getNonEquipmentItems().stream().anyMatch(s->ItemStack.matches(s,selected)),"Normal timed reveal delivers original result once");
            AdventureSavedData.get(server).remove(server,compassPlayer.getUUID());compassPlayer.discard();normal.discard();
            CosmicPVE.LOGGER.info("STEP8O4_HOTFIX Mystery hands/repeat/early-close/codec/recovery/full-inventory/normal-preview-reveal passed; Fame 35/55 aggregate; all three Flash prices purchased unopened once; Compass sound/action at ticks 0/100 only, tick99 silent");h.succeed();
        });
    }
    private static boolean canonical(ItemStack reward) {return ModItems.productionCalls().stream().anyMatch(s->ItemStack.matches(s,reward));}
    private static int calls(ServerPlayer p) {return p.getInventory().getNonEquipmentItems().stream().filter(AdventureHotfixGameTests::canonical).mapToInt(ItemStack::getCount).sum();}
    private static void assertFeedback(GameTestHelper h,List<Packet<?>> packets) {
        var sounds=packets.stream().filter(p->p instanceof ClientboundSoundPacket).map(p->(ClientboundSoundPacket)p).toList();
        h.assertTrue(sounds.size()==1 && sounds.getFirst().getSound().value()==SoundEvents.EXPERIENCE_ORB_PICKUP && sounds.getFirst().getVolume()==1F && sounds.getFirst().getPitch()==1F,"One targeted pickup sound volume/pitch 1");
        h.assertTrue(packets.stream().filter(p->p instanceof ClientboundSystemChatPacket).count()==1,"Normal distance action occurs once");
    }
}
