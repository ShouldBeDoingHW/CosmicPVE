package com.cosmicpve.adventure;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.registry.ModItems;
import com.cosmicpve.reward.*;
import com.cosmicpve.content.CosmicContent;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.*;
import java.util.function.Consumer;

/** Real generated-dimension and connected server-player paths, kept separate from bootstrap smoke tests. */
public final class AdventureGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS=DeferredRegister.create(Registries.TEST_FUNCTION,CosmicPVE.MOD_ID);
    static { FUNCTIONS.register("woodlands_generation",()->AdventureGameTests::generation); FUNCTIONS.register("woodlands_player_flow",()->AdventureGameTests::flow); }
    static {
        FUNCTIONS.register("woodlands_anchors",()->WoodlandPolishGameTests::grounding);
        FUNCTIONS.register("woodlands_polish",()->WoodlandPolishGameTests::freshWorld);
        FUNCTIONS.register("decision_restore",()->WoodlandPolishGameTests::decisionRestore);
        FUNCTIONS.register("adventure_hotfix",()->AdventureHotfixGameTests::verify);
    }
    public static void register(IEventBus bus) { FUNCTIONS.register(bus);bus.addListener(AdventureGameTests::tests); }
    private static void tests(RegisterGameTestsEvent event) {
        if (!Boolean.getBoolean("cosmicpve.adventureGameTests")) return;
        var env=event.registerEnvironment(CosmicPVE.id("woodlands_environment"),new TestEnvironmentDefinition.AllOf());
        for(String name:List.of("woodlands_generation","woodlands_player_flow","woodlands_anchors","woodlands_polish","decision_restore","adventure_hotfix"))event.registerTest(CosmicPVE.id(name),new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION,CosmicPVE.id(name)),new TestData<>(env,CosmicPVE.id("trial/development_room"),4000,0,true,Rotation.NONE,false,1,1,false)));
    }
    private static void generation(GameTestHelper helper) {
        var level=helper.getLevel().getServer().getLevel(DenseWoodlandsSessionService.DIMENSION);
        helper.assertTrue(level!=null,"Dense Woodlands must be a real loaded dimension");
        var result=WoodlandProbe.sample(level,120,120,16);
        CosmicPVE.LOGGER.info("WOODLANDS_RUNTIME_PROOF seed={} region=chunks[120..135]^2 {}",level.getSeed(),result);
        helper.assertTrue(result.min()>=30 && result.max()<=130,"Hard generated-ground range: "+result);
        helper.assertTrue(result.softPercent()>=95 && result.mean()>=65 && result.mean()<=95,"Soft/central terrain distribution: "+result);
        helper.assertTrue(result.maxNeighborSlope()<=4,"No abrupt neighboring ground jumps: "+result);
        for(String family:List.of("small_tree","tall_tree","fallen_tree","small_boulder","campsite"))
            helper.assertTrue(result.placements().entrySet().stream().anyMatch(e->e.getKey().contains(family)&&e.getValue()>0),"Actual generated family "+family);
        for(var e:result.placements().entrySet())helper.assertTrue(e.getValue()>0,"Actual authored variant "+e.getKey());
        boolean loot=false;
        for(var placement:WoodlandTemplateFeature.recent()) {
            if(!placement.variant().contains("campsite"))continue;
            var origin=placement.origin();
            for(int dx=-14;dx<=14;dx++)for(int dz=-14;dz<=14;dz++)for(int dy=0;dy<4;dy++) {
                var at=origin.offset(dx,dy,dz);if(!(level.getBlockEntity(at) instanceof RandomizableContainerBlockEntity barrel))continue;
                if(!ResourceKey.create(Registries.LOOT_TABLE,CosmicPVE.id("chests/adventure/dense_woodlands")).equals(barrel.getLootTable()))continue;
                barrel.unpackLootTable(null);
                helper.assertTrue(barrel.getLootTable()==null && !barrel.isEmpty(),"Generated campsite lazy table must produce real loot exactly once");
                var items=new ArrayList<ItemStack>();for(int i=0;i<barrel.getContainerSize();i++)items.add(barrel.getItem(i).copy());
                barrel.unpackLootTable(null);
                for(int i=0;i<items.size();i++)helper.assertTrue(ItemStack.matches(items.get(i),barrel.getItem(i)),"Reopen must not reroll");
                CosmicPVE.LOGGER.info("WOODLANDS_CAMPSITE_LOOT position={} items={}",at,items.stream().filter(s->!s.isEmpty()).toList());loot=true;break;
            }
            if(loot)break;
        }
        helper.assertTrue(loot,"At least one naturally generated campsite barrel must resolve canonical loot");
        for(String tier:List.of("ultimate","legendary")) {
            var table=CosmicContent.repository().requireRewardTable(CosmicPVE.id("space_chest/"+tier));
            var rows=table.entries().stream().filter(e->e.reward() instanceof com.cosmicpve.content.definition.reward.RewardDescriptor.StaticItem item && item.itemId().equals(CosmicPVE.id("white_scroll"))).toList();
            helper.assertTrue(rows.size()==1 && rows.getFirst().weight()==(tier.equals("ultimate")?18:20),"One exact White Scroll row");
            boolean selected=false;for(int seed=0;seed<10000 && !selected;seed++) {
                var context=new RewardGenerationContext(level.registryAccess(),net.minecraft.util.RandomSource.create(seed),null);
                var row=RewardTableService.select(table,context);
                if(row.equals(rows.getFirst())) {var item=new RewardGeneratorService().generate(row.reward(),context).orElseThrow();helper.assertTrue(item.is(ModItems.WHITE_SCROLL.get())&&item.getCount()==1,"Canonical constructible White Scroll");selected=true;}
            }
            helper.assertTrue(selected,"Deterministic weighted selection reaches White Scroll");
            CosmicPVE.LOGGER.info("WHITE_SCROLL_RUNTIME tier={} weight={} total={}",tier,rows.getFirst().weight(),table.totalWeight());
        }
        helper.succeed();
    }
    private static void flow(GameTestHelper helper) {
        var service=DenseWoodlandsBootstrap.SESSIONS;var players=new ArrayList<ServerPlayer>();
        for(int sourceY:new int[]{-100,10,64,250}) {
            var p=player(helper,"woodland"+sourceY);p.setPos(0,sourceY,0);p.setGameMode(GameType.SURVIVAL);
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.CALL_OF_FOREST_10.get(),3));
            ModItems.CALL_OF_FOREST_10.get().use(p.level(),p,InteractionHand.MAIN_HAND);
            helper.assertTrue(p.getMainHandItem().getCount()==2,"Exactly one Call consumed");
            helper.assertTrue(p.level()==helper.getLevel() && p.getY()==sourceY,"No immediate teleport");
            helper.assertTrue(p.getEffect(MobEffects.DARKNESS).getDuration()==100 && p.getEffect(MobEffects.NAUSEA).getDuration()==100,"Both transition effects last 100 ticks");
            ModItems.CALL_OF_FOREST_10.get().use(p.level(),p,InteractionHand.MAIN_HAND);
            helper.assertTrue(p.getMainHandItem().getCount()==2 && service.session(p).phase()==AdventureSession.Phase.TRANSITION,"Transition lock prevents repeat consumption");
            players.add(p);
        }
        helper.runAfterDelay(99,()->{
            for(var p:players){service.tickPlayer(p);helper.assertTrue(p.level()==helper.getLevel(),"Still at source before 100 ticks");}
        });
        helper.runAfterDelay(101,()->{
            for(var p:players) {
                service.tickPlayer(p);var s=service.session(p);
                helper.assertTrue(s!=null && s.phase()==AdventureSession.Phase.ACTIVE && p.level().dimension().equals(DenseWoodlandsSessionService.DIMENSION),"Delayed activation must enter real dimension; session="+s+" dimension="+p.level().dimension());
                helper.assertTrue(AdventureSurface.safe((ServerLevel)p.level(),p.blockPosition()),"Arrival must have actual safe ground");
                helper.assertTrue(p.gameMode.getGameModeForPlayer()==GameType.ADVENTURE,"Adventure mode");
                long compasses=p.getInventory().getNonEquipmentItems().stream().filter(i->DenseWoodlandsSessionService.owned(i,s)).count();
                helper.assertTrue(compasses==1,"Exactly one session-owned compass");
                helper.assertTrue(!service.extract(p,s.exit().offset(50,0,0)),"Foreign exit cannot extract");
                var level=(ServerLevel)p.level();level.setDayTime(1000);
                boolean zombie=false;for(int i=0;i<10&&!zombie;i++)zombie=AdventureZombies.spawn(p,s);
                helper.assertTrue(zombie,"Controlled armored Zombie physically spawns during daylight");
                CosmicPVE.LOGGER.info("WOODLANDS_ENTRY_RUNTIME sourceY={} entry={} exit={} zombies={}",s.home().position().y,s.entry(),s.exit(),AdventureZombies.count(p));
                if(p==players.getLast()) {
                    p.getInventory().setItem(7,new ItemStack(Items.DIAMOND,7));
                    p.setHealth(0);p.die(p.damageSources().genericKill());
                    helper.assertTrue(service.session(p).phase()==AdventureSession.Phase.DEAD,"Death closes active ownership/timer");
                    helper.assertTrue(p.getInventory().getNonEquipmentItems().stream().noneMatch(i->i.is(ModItems.ADVENTURE_COMPASS.get())),"No retained compass on death");
                    var drops=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,p.getBoundingBox().inflate(8));
                    helper.assertTrue(drops.stream().noneMatch(e->e.getItem().is(ModItems.ADVENTURE_COMPASS.get())),"Compass never drops");
                    helper.assertTrue(drops.stream().anyMatch(e->e.getItem().is(Items.DIAMOND)&&e.getItem().getCount()==7),"Ordinary loot drops at Adventure death position");
                    var respawn=level.getServer().getPlayerList().respawn(p,false,net.minecraft.world.entity.Entity.RemovalReason.KILLED);
                    service.tickPlayer(respawn);
                    helper.assertTrue(service.session(respawn)==null && !com.cosmicpve.combat.CosmicCombat.activities().isAdventure(respawn)
                            && respawn.gameMode.getGameModeForPlayer()==GameType.SURVIVAL,"Real respawn clears session/activity and restores mode");
                    helper.assertTrue(respawn.getInventory().getNonEquipmentItems().stream().noneMatch(i->i.is(ModItems.ADVENTURE_COMPASS.get())),"No compass after respawn");
                    CosmicPVE.LOGGER.info("WOODLANDS_DEATH_RUNTIME ordinaryDrops={} compassRetained=false compassDropped=false respawnSessionCleared=true",drops.size());
                    level.getServer().getPlayerList().remove(respawn);continue;
                }
                helper.assertTrue(service.extract(p,s.exit()),"Own exit extracts");
                helper.assertTrue(service.session(p)==null && p.getY()==s.home().position().y && p.gameMode.getGameModeForPlayer()==GameType.SURVIVAL,"Restores source and mode and closes session");
            }
            // Temporary Cave Diving loadout uses the production clear/grant/clear lifecycle.
            var p=players.getFirst();var loadout=new com.cosmicpve.trial.TrialRoomLoadoutService(new com.cosmicpve.trial.persistence.TrialInventoryTransactionService());
            loadout.applyCaveDiving(p);loadout.applyCaveDiving(p);
            helper.assertTrue(p.getInventory().getItem(0).is(Items.WOODEN_SWORD)&&p.getInventory().getItem(0).getCount()==1,"One temporary sword slot 1");
            helper.assertTrue(p.getInventory().getItem(8).is(Items.COOKED_COD)&&p.getInventory().getItem(8).getCount()==15,"15 temporary Cod slot 9");
            loadout.clear(p);helper.assertTrue(p.getInventory().isEmpty(),"Temporary loadout removed");
            players.forEach(ServerPlayer::discard);helper.succeed();
        });
    }
    static ServerPlayer player(GameTestHelper helper,String name) {
        return player(helper,name,packet->{});
    }
    static ServerPlayer player(GameTestHelper helper,String name,Consumer<Packet<?>> packets) {
        var profile=new GameProfile(UUID.randomUUID(),name);var cookie=CommonListenerCookie.createInitial(profile,false);
        var player=new ServerPlayer(helper.getLevel().getServer(),helper.getLevel(),profile,cookie.clientInformation());
        player.setInvulnerable(true);
        player.setNoGravity(true);
        var connection=new Connection(PacketFlow.SERVERBOUND) {
            {new io.netty.channel.embedded.EmbeddedChannel(this);}
            @Override public void send(Packet<?> packet,io.netty.channel.ChannelFutureListener listener,boolean flush) {packets.accept(packet);}
        };
        net.neoforged.neoforge.network.registration.NetworkRegistry.configureMockConnection(connection);
        new ServerGamePacketListenerImpl(helper.getLevel().getServer(),connection,player,cookie);return player;
    }
}
