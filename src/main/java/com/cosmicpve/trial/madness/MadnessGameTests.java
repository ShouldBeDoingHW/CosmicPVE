package com.cosmicpve.trial.madness;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.content.ContentSnapshot;
import com.cosmicpve.content.validation.ValidationResult;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.data.component.TrialPortalModifiers;
import com.cosmicpve.trial.portal.TrialPortalPresets;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.*;
import java.util.function.Consumer;

public final class MadnessGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION,CosmicPVE.MOD_ID);
    static {
        FUNCTIONS.register("trial_madness_foundation",ignored -> MadnessGameTests::verify);
        FUNCTIONS.register("trial_madness_hotfix",ignored -> MadnessGameTests::verifyHotfix);
        FUNCTIONS.register("trial_madness_polish",ignored -> MadnessGameTests::verifyPolish);
    }
    public static void register(IEventBus bus) { FUNCTIONS.register(bus); bus.addListener(MadnessGameTests::registerTests); }
    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(CosmicPVE.id("madness_environment"),new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("trial_madness_foundation"),new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION,CosmicPVE.id("trial_madness_foundation")),
                new TestData<>(environment,CosmicPVE.id("trial/development_room"),120,0,true,Rotation.NONE,false,1,1,false)));
        event.registerTest(CosmicPVE.id("trial_madness_hotfix"),new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION,CosmicPVE.id("trial_madness_hotfix")),
                new TestData<>(environment,CosmicPVE.id("trial/development_room"),120,0,true,Rotation.NONE,false,1,1,false)));
        event.registerTest(CosmicPVE.id("trial_madness_polish"),new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION,CosmicPVE.id("trial_madness_polish")),
                new TestData<>(environment,CosmicPVE.id("trial/development_room"),120,0,true,Rotation.NONE,false,1,1,false)));
    }
    private static void verifyPolish(GameTestHelper helper) {
        var server=helper.getLevel().getServer(); var sessions=new com.cosmicpve.trial.persistence.TrialSessionRepository();
        var originalSession=sessions.active(server); var content=CosmicContent.repository(); var original=content.snapshot();
        var a=mockPlayer(helper); var b=mockPlayer(helper); var spectator=mockPlayer(helper);
        try {
            var definitions=List.copyOf(original.madnessDefinitions().values());
            helper.assertTrue(definitions.size()==10,"ten production Madness definitions");
            var fixture=com.cosmicpve.trial.TrialSession.joining(UUID.randomUUID(),CosmicPVE.id("unused"),net.minecraft.core.BlockPos.ZERO,List.of(),List.of())
                    .addParticipant(a.getUUID()).addParticipant(b.getUUID())
                    .withState(com.cosmicpve.trial.TrialLifecycleState.DECISION,600,Optional.empty(),false,List.of());
            for(int offset=0;offset<10;offset+=5) {
                var ballot=new MadnessState(1,List.of(),definitions.subList(offset,offset+5),List.of(),offset+1);
                sessions.publish(server,fixture.withProgress(fixture.progress().withMadness(ballot))); open(a);
                for(int index=0;index<5;index++) {
                    var d=ballot.options().get(index); var icon=a.containerMenu.getSlot(index).getItem();
                    helper.assertTrue(d.valid() && !icon.is(Items.AIR) && !d.description().isBlank(),"valid icon/name/description/handler");
                    helper.assertTrue(icon.get(DataComponents.LORE).lines().getFirst().getString().equals(d.description())
                            && icon.getHoverName().getStyle().getColor().getValue()==0x8C1708,"actual ballot icons show data-owned lore and canonical name color");
                }
            }
            var source=server.createCommandSourceStack().withEntity(a);
            for(String id:List.of("wet_noodle","thin_skin","rocket_man","statues","owl_gene","time_glitch")) {
                sessions.publish(server,fixture); a.closeContainer();
                helper.assertTrue(server.getCommands().getDispatcher().execute("cosmic trial debug madness add cosmicpve:"+id,source)==1,"runtime-verified namespaced debug add "+id);
                helper.assertTrue(sessions.active(server).orElseThrow().progress().madness().has(id),"debug command activates correct Trial-local modifier");
            }
            helper.assertTrue(server.getCommands().getDispatcher().execute("cosmic trial portal give @s",source)==1,"normal portal give command");
            for(int value=1;value<=3;value++) {
                helper.assertTrue(server.getCommands().getDispatcher().execute("cosmic trial trinket give @s madness "+value,source)==1,"physical Madness trinket command "+value);
                var trinket=com.cosmicpve.trial.trinket.TrialTrinkets.create(com.cosmicpve.data.component.TrialTrinketType.MADNESS,value,1);
                var portal=new ItemStack(com.cosmicpve.registry.ModItems.TRIAL_PORTAL.get());
                helper.assertTrue(new com.cosmicpve.trial.trinket.TrialTrinketApplicationService().apply(trinket,portal,portal)
                        ==com.cosmicpve.trial.trinket.TrialTrinketApplicationService.Outcome.SUCCESS && trinket.isEmpty(),"application consumes exactly one trinket");
                var modifiers=portal.get(ModDataComponents.TRIAL_PORTAL_MODIFIERS.get());
                var ballot=MadnessState.EMPTY.queue(1).offer(definitions,modifiers.madnessChoices(10),net.minecraft.util.RandomSource.create(9));
                helper.assertTrue(ballot.options().size()==2+value,"real applied portal offers 3/4/5 choices");
                a.setItemSlot(EquipmentSlot.MAINHAND,portal);
                helper.assertTrue(server.getCommands().getDispatcher().execute("cosmic trial portal inspect",source)==1,"runtime portal inspect command");
                var encoded=ItemStack.CODEC.encodeStart(server.registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE),portal).getOrThrow();
                var decoded=ItemStack.CODEC.parse(server.registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE),encoded).getOrThrow();
                helper.assertTrue(ItemStack.matches(portal,decoded),"portal item round-trip retains components for save/relog and copies");
                var lore=new ArrayList<net.minecraft.network.chat.Component>();
                var display=com.cosmicpve.trial.trinket.TrialTrinkets.create(com.cosmicpve.data.component.TrialTrinketType.MADNESS,value,1);
                display.getItem().appendHoverText(display,net.minecraft.world.item.Item.TooltipContext.of(helper.getLevel()),
                        net.minecraft.world.item.component.TooltipDisplay.DEFAULT,lore::add,net.minecraft.world.item.TooltipFlag.NORMAL);
                helper.assertTrue(lore.getFirst().getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents text
                        && text.getKey().startsWith("tooltip.cosmicpve.trial_trinket.madness"),"variant-specific clear tooltip");
            }
            sessions.publish(server,fixture.withProgress(fixture.progress().withMadness(MadnessState.EMPTY.queue(1))));
            helper.assertTrue(server.getCommands().getDispatcher().execute("cosmic trial debug continue",source)==1
                    && a.containerMenu instanceof MadnessMenu,"existing admin continue opens a pending ballot in a post-room Decision Box");
            for(boolean obfuscated:List.of(false,true)) {
                var payload=new com.cosmicpve.network.TrialTimerPayload(543,obfuscated);
                var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),server.registryAccess());
                try {
                    com.cosmicpve.network.TrialTimerPayload.STREAM_CODEC.encode(buffer,payload);
                    helper.assertTrue(com.cosmicpve.network.TrialTimerPayload.STREAM_CODEC.decode(buffer).equals(payload),"timer transport preserves actual seconds and native-obfuscation flag");
                } finally {buffer.release();}
            }
            // Actual registered contributors plus the real combat engine: shared additive outgoing bucket,
            // multiplicative incoming stack, and a completely untouched true packet.
            var wet=definitions.stream().filter(d -> d.handler().getPath().equals("wet_noodle")).findFirst().orElseThrow();
            var thin=definitions.stream().filter(d -> d.handler().getPath().equals("thin_skin")).findFirst().orElseThrow();
            var active=new MadnessState(0,List.of(wet,thin),List.of(),List.of());
            sessions.publish(server,fixture.withProgress(fixture.progress().withMadness(active)));
            var contributor=new MadnessCombatContributor();
            var context=combatContext(a,b,com.cosmicpve.combat.api.DamageChannel.ORDINARY,com.cosmicpve.combat.api.AttackCategory.MELEE);
            var outgoing=contributor.resolve(context); var incoming=contributor.resolveIncoming(context);
            helper.assertTrue(outgoing.size()==1 && outgoing.getFirst().bonus()==-.15 && incoming.size()==1 && incoming.getFirst().multiplier()==1.15,"ordinary modifiers apply exactly once");
            var truePacket=com.cosmicpve.combat.api.TrueDamagePacket.standard(CosmicPVE.id("polish_test"),7);
            var request=new com.cosmicpve.combat.pipeline.CombatCalculationRequest(20,0,List.of(),com.cosmicpve.combat.api.DamageBounds.UNBOUNDED,List.of(),com.cosmicpve.combat.api.DamageBounds.UNBOUNDED,List.of(truePacket),outgoing,incoming);
            var result=new com.cosmicpve.combat.pipeline.CombatEngine().calculate(context,request);
            helper.assertTrue(Math.abs(result.breakdown().finalOrdinaryDamage()-19.55)<.00001 && result.totalQueuedTrueDamage()==7,"Wet Noodle and Thin Skin compose once; true packet is unchanged");
            var stacked=new com.cosmicpve.combat.pipeline.CombatCalculationRequest(20,.30,List.of(),com.cosmicpve.combat.api.DamageBounds.UNBOUNDED,List.of(.8),com.cosmicpve.combat.api.DamageBounds.UNBOUNDED,List.of(),outgoing,incoming);
            helper.assertTrue(Math.abs(new com.cosmicpve.combat.pipeline.CombatEngine().calculate(context,stacked).breakdown().finalOrdinaryDamage()-21.16)<.00001,"normal armor/accessory/enchantment bucket composition");
            for(var excluded:List.of(combatContext(a,b,com.cosmicpve.combat.api.DamageChannel.TRUE,com.cosmicpve.combat.api.AttackCategory.MELEE),
                    combatContext(a,b,com.cosmicpve.combat.api.DamageChannel.ORDINARY,com.cosmicpve.combat.api.AttackCategory.ENVIRONMENTAL),
                    context.withDamageSource(b.damageSources().genericKill())))
                helper.assertTrue(contributor.resolve(excluded).isEmpty() && contributor.resolveIncoming(excluded).isEmpty(),"true/environmental/execution damage bypasses new ordinary modifiers");
            sessions.publish(server,fixture);
            helper.assertTrue(contributor.resolve(context).isEmpty() && contributor.resolveIncoming(context).isEmpty(),"unselected/cleared state has no effect");
            for(var d:List.of(wet,thin)) {
                var offered=new MadnessState(1,List.of(),List.of(d),List.of(),1).vote(a.getUUID(),d.id()).resolveBallot(1,List.of(a.getUUID()),net.minecraft.util.RandomSource.create());
                sessions.publish(server,fixture.withProgress(fixture.progress().withMadness(offered)));
                helper.assertTrue(d==wet ? contributor.resolve(context).size()==1 : contributor.resolveIncoming(context).size()==1,"ballot selection activates new damage handler");
            }
            sessions.clear(server);
            helper.assertTrue(contributor.resolve(context).isEmpty() && contributor.resolveIncoming(context).isEmpty(),"cleanup/outside Trial removes contributions immediately");
            verifySoundScheduler(helper,a,spectator,definitions);
            // Exercise Fame Shop's existing aggregate generator without any production special cases.
            var seen=new HashSet<Integer>(); var shop=new com.cosmicpve.economy.fame.FameShopService();
            var generate=shop.getClass().getDeclaredMethod("generate",net.minecraft.server.level.ServerPlayer.class,long.class,net.minecraft.util.RandomSource.class); generate.setAccessible(true);
            for(int seed=0;seed<100 && seen.size()<3;seed++) {
                var catalog=(com.cosmicpve.economy.fame.FameShopCatalog)generate.invoke(shop,a,0L,net.minecraft.util.RandomSource.create(seed));
                for(var offer:catalog.offers()) for(var item:offer.payload()) {
                    var data=item.get(ModDataComponents.TRIAL_TRINKET.get());
                    if(data!=null && data.type()==com.cosmicpve.data.component.TrialTrinketType.MADNESS) seen.add(data.value());
                }
            }
            helper.assertTrue(seen.equals(Set.of(1,2,3)),"all new constructible trinket rows naturally reach Fame Shop through existing catalogs");
            CosmicPVE.LOGGER.info("Madness polish runtime passed: ten GUI descriptions; admin/trinket commands; damage scope/composition/true bypass; exact sound events/pitches 0,1,2; Fame Shop tiers 1,2,3");
        } catch(Exception exception) { throw new RuntimeException(exception); }
        finally {
            content.publish(ValidationResult.success(original));
            if(originalSession.isPresent()) sessions.publish(server,originalSession.get()); else sessions.clear(server);
            for(var player:List.of(a,b,spectator)) { player.closeContainer(); server.getPlayerList().remove(player); }
        }
        helper.succeed();
    }
    private static com.cosmicpve.combat.api.CombatContext combatContext(net.minecraft.server.level.ServerPlayer a,net.minecraft.server.level.ServerPlayer b,
            com.cosmicpve.combat.api.DamageChannel channel,com.cosmicpve.combat.api.AttackCategory category) {
        return new com.cosmicpve.combat.api.CombatContext(a,a,a,b,Optional.of(a.getUUID()),a.damageSources().playerAttack(a),category,channel,Set.of(),
                com.cosmicpve.combat.api.WeaponSnapshot.empty(),com.cosmicpve.equipment.enchantment.EffectiveEnchantments.EMPTY,1,OptionalLong.empty(),com.cosmicpve.combat.api.RecursionPolicy.NORMAL);
    }
    private static void verifySoundScheduler(GameTestHelper helper,net.minecraft.server.level.ServerPlayer player,net.minecraft.server.level.ServerPlayer spectator,List<MadnessDefinition> definitions) throws Exception {
        var stateClass=Class.forName("com.cosmicpve.trial.madness.MadnessRuntime$PlayerState");
        var constructor=stateClass.getDeclaredConstructor(); constructor.setAccessible(true);
        var apply=MadnessRuntime.class.getDeclaredMethod("apply",net.minecraft.server.level.ServerPlayer.class,
                stateClass,MadnessDefinition.class,net.minecraft.resources.Identifier.class); apply.setAccessible(true);
        for(String handler:List.of("owl_gene","statues","rocket_man")) {
            var d=definitions.stream().filter(row -> row.handler().getPath().equals(handler)).findFirst().orElseThrow();
            var state=constructor.newInstance(); packets(player); packets(spectator);
            for(int tick=0;tick<d.interval(0)+(handler.equals("rocket_man")?0:60);tick++)
                apply.invoke(MadnessRuntime.INSTANCE,player,state,d,com.cosmicpve.trial.TrialSessionService.CINDER_WOLF);
            var sounds=packets(player).stream().filter(net.minecraft.network.protocol.game.ClientboundSoundPacket.class::isInstance)
                    .map(net.minecraft.network.protocol.game.ClientboundSoundPacket.class::cast).toList();
            if(handler.equals("rocket_man")) helper.assertTrue(sounds.size()==1 && sounds.getFirst().getSound().value()==net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_BLAST,"one Firework Blast per Rocket activation");
            else {
                int offset=handler.equals("statues")?1:0;
                helper.assertTrue(sounds.size()==3+offset,"one countdown sequence per effect");
                if(offset==1) helper.assertTrue(sounds.getFirst().getSound().value()==net.minecraft.sounds.SoundEvents.WITHER_SPAWN,"Statues starts with Wither Spawn before notes");
                for(int i=0;i<3;i++) helper.assertTrue(sounds.get(i+offset).getSound().value()==net.minecraft.sounds.SoundEvents.NOTE_BLOCK_PLING.value()
                        && sounds.get(i+offset).getPitch()==(float)i,"actual sound packet retains exact PLING pitch "+i+" (including zero)");
            }
            helper.assertTrue(packets(spectator).isEmpty(),"Madness sounds never broadcast to unrelated players");
        }
        MadnessRuntime.INSTANCE.tick(helper.getLevel().getServer(),null);
        helper.assertTrue(packets(player).isEmpty(),"no countdown sounds after session cleanup");
    }
    private static void verifyHotfix(GameTestHelper helper) {
        var server=helper.getLevel().getServer(); var service=com.cosmicpve.trial.TrialRuntime.sessions();
        var sessions=new com.cosmicpve.trial.persistence.TrialSessionRepository();
        var oldSession=sessions.active(server); var content=CosmicContent.repository(); var original=content.snapshot();
        var a=mockPlayer(helper); var b=mockPlayer(helper); var c=mockPlayer(helper);
        try {
            packets(a); packets(b); packets(c);
            int result=server.getCommands().getDispatcher().execute("cosmic trial portal preset @s cosmicpve:skip_10",
                    server.createCommandSourceStack().withEntity(a));
            helper.assertTrue(result==1,"exact registered Skip-10 command must succeed");
            ItemStack portal=ItemStack.EMPTY;
            for(int i=0;i<36;i++) if(a.getInventory().getItem(i).has(ModDataComponents.TRIAL_PORTAL_MODIFIERS.get())) portal=a.getInventory().getItem(i);
            var modifiers=portal.get(ModDataComponents.TRIAL_PORTAL_MODIFIERS.get());
            helper.assertTrue(modifiers!=null && modifiers.skipRooms()==10,"command must actually deliver concrete Skip 10");
            var skipLine=com.cosmicpve.trial.portal.TrialPortalItem.modifierLines(modifiers).getFirst();
            helper.assertTrue(skipLine.getSiblings().stream().anyMatch(component -> component.getContents() instanceof
                    net.minecraft.network.chat.contents.TranslatableContents text && text.getKey().equals("tooltip.cosmicpve.trial_portal.skip")
                    && java.util.Arrays.equals(text.getArgs(),new Object[]{10})),"tooltip must carry Skip 10 Rooms translation and value");
            var progress=com.cosmicpve.trial.TrialProgress.initial(modifiers);
            for(int i=0;i<10;i++) progress=progress.appendSkippedReward(List.of());
            progress=progress.markInitialSkipProcessed();
            helper.assertTrue(progress.nextRoomOrdinal()==11 && progress.phase()==com.cosmicpve.trial.TrialPhase.IMPOSSIBLE
                    && progress.madness().pending()==2,"Skip 10 must queue two ballots before Impossible ordinal 11");
            for(var phase:List.of(com.cosmicpve.trial.TrialPhase.APPRENTICE,com.cosmicpve.trial.TrialPhase.HARDCORE,com.cosmicpve.trial.TrialPhase.IMPOSSIBLE)) {
                long expected=phase==com.cosmicpve.trial.TrialPhase.IMPOSSIBLE?2:4;
                helper.assertTrue(progress.pot().stream().filter(entry -> entry.phase().filter(phase::equals).isPresent()).count()==expected,"per-ordinal skipped reward tiers");
            }
            var offered=progress.madness().offer(List.copyOf(original.madnessDefinitions().values()),5,net.minecraft.util.RandomSource.create(9));
            var session=com.cosmicpve.trial.TrialSession.joining(UUID.randomUUID(),CosmicPVE.id("unused"),net.minecraft.core.BlockPos.ZERO,List.of(),List.of())
                    .addParticipant(a.getUUID()).addParticipant(b.getUUID()).addParticipant(c.getUUID())
                    .withProgress(progress.beginDecision(List.of(a.getUUID(),b.getUUID(),c.getUUID())))
                    .withState(com.cosmicpve.trial.TrialLifecycleState.DECISION,600,Optional.empty(),false,List.of());
            sessions.publish(server,session);
            // The production Decision tick sends countdown feedback before it checks which menu is open.
            service.tick(server);
            for(var player:List.of(a,b,c)) {
                helper.assertTrue(packets(player).stream().anyMatch(net.minecraft.network.protocol.game.ClientboundSoundPacket.class::isInstance),"30-second Deal countdown sends existing sound");
                service.decide(player,com.cosmicpve.trial.TrialDecision.NO_DEAL);
            }
            session=service.active(server).orElseThrow(); offered=session.progress().madness();
            helper.assertTrue(session.stateTicksRemaining()==599 && a.containerMenu instanceof MadnessMenu,"Deal to Madness preserves the same remaining session timer");
            for(int i=0;i<99;i++) service.tick(server);
            helper.assertTrue(service.active(server).orElseThrow().stateTicksRemaining()==500,"open Madness does not pause countdown");
            var oldMenu=(MadnessMenu)a.containerMenu;
            oldMenu.clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,a);
            helper.assertTrue(a.containerMenu==a.inventoryMenu && !oldMenu.stillValid(a),"first submission closes and invalidates GUI immediately");
            // Retain access to the Decision tick for the timeout/race fixture below.
            var decisionTick=com.cosmicpve.trial.TrialSessionService.class.getDeclaredMethod("tickDecision",
                    net.minecraft.server.MinecraftServer.class,com.cosmicpve.trial.TrialSession.class);
            decisionTick.setAccessible(true); decisionTick.invoke(service,server,service.active(server).orElseThrow());
            helper.assertTrue(a.containerMenu==a.inventoryMenu && service.active(server).orElseThrow().stateTicksRemaining()==499,"submitted menu stays closed and session countdown continues");
            for(var player:List.of(a,b,c)) {
                var countdown=packets(player);
                helper.assertTrue(countdown.stream().anyMatch(net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket.class::isInstance)
                        && countdown.stream().anyMatch(net.minecraft.network.protocol.game.ClientboundSoundPacket.class::isInstance),
                        "same Decision title/subtitle and basedrum packets continue for open Madness and submitted waiting players");
            }
            ((MadnessMenu)b.containerMenu).clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,b);
            helper.assertTrue(service.active(server).orElseThrow().progress().madness().active().isEmpty(),"two of three votes must wait");
            ((MadnessMenu)c.containerMenu).clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,c);
            var second=service.active(server).orElseThrow();
            helper.assertTrue(second.progress().madness().pending()==1 && second.progress().madness().active().size()==1
                    && second.progress().madness().votes().isEmpty() && second.progress().madness().ballotSerial()==offered.ballotSerial()+1
                    && second.currentRoom().isEmpty(),"final first-ballot vote resolves early and opens fresh ballot, not room 11");
            helper.assertTrue(a.containerMenu instanceof MadnessMenu && b.containerMenu instanceof MadnessMenu && c.containerMenu instanceof MadnessMenu,"second ballot opens for all continuing players");
            helper.assertTrue(second.progress().madness().options().stream().noneMatch(second.progress().madness().active()::contains),"second ballot excludes first winner");
            for(var player:List.of(a,b,c)) assertAnnouncement(helper,packets(player),second.progress().madness().active().getFirst().name());
            // GameTestServer does not always load the custom instance dimension. Keep this fixture safe there;
            // a normal server instead exercises the real room start below.
            boolean physical=server.getLevel(com.cosmicpve.trial.TrialRuntime.INSTANCE_DIMENSION)!=null;
            if(!physical) content.publish(ValidationResult.success(new ContentSnapshot(0,original.scalingProfiles(),original.stackDefinitions(),
                    original.armorSetDefinitions(),original.rewardTables(),Map.of(),original.maskDefinitions(),original.madnessDefinitions(),original.trialPortalPresets())));
            ((MadnessMenu)a.containerMenu).clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,a);
            ((MadnessMenu)b.containerMenu).clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,b);
            var stale=(MadnessMenu)c.containerMenu;
            stale.clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,c);
            var done=service.active(server).orElseThrow();
            helper.assertTrue(done.progress().madness().pending()==0 && done.progress().madness().active().size()==2
                    && done.progress().nextRoomOrdinal()==11 && done.progress().phase()==com.cosmicpve.trial.TrialPhase.IMPOSSIBLE,"second ballot resolves immediately at ordinal 11 Impossible");
            for(var player:List.of(a,b,c)) assertAnnouncement(helper,packets(player),done.progress().madness().active().getLast().name());
            stale.clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,c);
            helper.assertTrue(service.active(server).orElseThrow().equals(done),"stale final click cannot announce, activate or progress twice");
            helper.assertTrue(packets(c).stream().noneMatch(net.minecraft.network.protocol.game.ClientboundSystemChatPacket.class::isInstance),"stale final click sends no duplicate announcement");
            if(physical) helper.assertTrue(done.state()==com.cosmicpve.trial.TrialLifecycleState.ROOM_INTRO && done.currentRoom().isPresent(),"real ordinal 11 room must begin");
            content.publish(ValidationResult.success(new ContentSnapshot(0,original.scalingProfiles(),original.stackDefinitions(),
                    original.armorSetDefinitions(),original.rewardTables(),Map.of(),original.maskDefinitions(),original.madnessDefinitions(),original.trialPortalPresets())));
            var soloBallot=MadnessState.EMPTY.queue(1).offer(List.copyOf(original.madnessDefinitions().values()),2,net.minecraft.util.RandomSource.create(3));
            var solo=session.removeParticipant(b.getUUID()).removeParticipant(c.getUUID())
                    .withProgress(progress.withMadness(soloBallot)).withStateTicks(1);
            sessions.publish(server,solo); packets(a); packets(b); packets(c); open(a);
            var soloMenu=(MadnessMenu)a.containerMenu; soloMenu.clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,a);
            var soloDone=service.active(server).orElseThrow();
            helper.assertTrue(a.containerMenu==a.inventoryMenu && soloDone.progress().madness().pending()==0
                    && soloDone.progress().madness().active().size()==1,"solo final vote resolves immediately near timeout and closes GUI");
            assertAnnouncement(helper,packets(a),soloDone.progress().madness().active().getFirst().name());
            soloMenu.clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,a);
            helper.assertTrue(service.active(server).orElseThrow().equals(soloDone),"near-timeout stale vote resolves once");
            sessions.publish(server,solo); open(a); packets(a);
            decisionTick.invoke(service,server,solo);
            var timedOut=service.active(server).orElseThrow();
            helper.assertTrue(timedOut.progress().madness().pending()==0 && a.containerMenu==a.inventoryMenu,"unsubmitted timeout resolves independently of GUI");
            assertAnnouncement(helper,packets(a),timedOut.progress().madness().active().getFirst().name());
            var membership=soloBallot.vote(a.getUUID(),soloBallot.options().getFirst().id()).vote(b.getUUID(),soloBallot.options().getFirst().id());
            var continuing=session.removeParticipant(c.getUUID()).withProgress(progress.withMadness(membership)).withStateTicks(200);
            sessions.publish(server,continuing); packets(a); packets(b); packets(c);
            decisionTick.invoke(service,server,continuing);
            var membershipDone=service.active(server).orElseThrow();
            helper.assertTrue(membershipDone.progress().madness().pending()==0,"removed DEAL/departed voter cannot block completion on next tick");
            assertAnnouncement(helper,packets(a),membershipDone.progress().madness().active().getFirst().name());
            assertAnnouncement(helper,packets(b),membershipDone.progress().madness().active().getFirst().name());
            helper.assertTrue(packets(c).isEmpty(),"removed participant is excluded from ballot announcement");
            CosmicPVE.LOGGER.info("Madness hotfix runtime: exact preset command, three-player GUI closure/wait, two early ballots passed; physical instance room verified={}",physical);
        } catch(Exception exception) { throw new RuntimeException(exception); }
        finally {
            content.publish(ValidationResult.success(original));
            if(oldSession.isPresent()) sessions.publish(server,oldSession.get()); else sessions.clear(server);
            for(var player:List.of(a,b,c)) {player.closeContainer(); server.getPlayerList().remove(player);}
        }
        helper.succeed();
    }
    private static void open(net.minecraft.server.level.ServerPlayer player) {
        player.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inventory,ignored) -> new MadnessMenu(id,inventory,player),
                net.minecraft.network.chat.Component.literal("Madness")));
    }
    private static net.minecraft.server.level.ServerPlayer mockPlayer(GameTestHelper helper) {
        Consumer<net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent> configure = event -> {
            if(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)
                net.neoforged.neoforge.network.registration.NetworkRegistry.configureMockConnection(player.connection.getConnection());
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGHEST,configure);
        try { return helper.makeMockServerPlayerInLevel(); }
        finally { net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(configure); }
    }
    private static List<Object> packets(net.minecraft.server.level.ServerPlayer player) {
        var channel=(io.netty.channel.embedded.EmbeddedChannel)player.connection.getConnection().channel();
        channel.runPendingTasks(); var result=new ArrayList<Object>(); Object packet;
        while((packet=channel.readOutbound())!=null) result.add(packet);
        return result;
    }
    private static void assertAnnouncement(GameTestHelper helper,List<Object> packets,String winner) {
        var announcements=packets.stream().filter(net.minecraft.network.protocol.game.ClientboundSystemChatPacket.class::isInstance)
                .map(net.minecraft.network.protocol.game.ClientboundSystemChatPacket.class::cast)
                .filter(packet -> packet.content().getString().startsWith("Madness Modifier Selected:")).toList();
        helper.assertTrue(announcements.size()==1 && announcements.getFirst().content().getString().equals("Madness Modifier Selected: "+winner)
                && announcements.getFirst().content().getStyle().getColor().getValue()==0x8C1708,"exactly one named winner chat packet in canonical Madness color");
    }
    private static void verify(GameTestHelper helper) {
        var repository = CosmicContent.repository(); var original = repository.snapshot();
        helper.assertTrue(original.madnessDefinitions().size()==10,"all ten Madness definitions must reload");
        helper.assertTrue(original.trialPortalPresets().size()==5,"five production presets must reload");
        var portal = TrialPortalPresets.generate(CosmicPVE.id("skip_10"));
        var snapshot = portal.get(ModDataComponents.TRIAL_PORTAL_MODIFIERS.get());
        helper.assertTrue(snapshot.skipRooms()==10 && portal.getMaxStackSize()==1,"preset item must store concrete single-stack values");
        try {
            var presets = new LinkedHashMap<>(original.trialPortalPresets());
            presets.put(CosmicPVE.id("skip_10"),new TrialPortalModifiers(3,0,13,0,0,0));
            repository.publish(ValidationResult.success(new ContentSnapshot(0,original.scalingProfiles(),original.stackDefinitions(),
                    original.armorSetDefinitions(),original.rewardTables(),original.trialRooms(),original.maskDefinitions(),
                    original.madnessDefinitions(),presets)));
            helper.assertTrue(TrialPortalPresets.generate(CosmicPVE.id("skip_10")).get(ModDataComponents.TRIAL_PORTAL_MODIFIERS.get()).skipRooms()==13,
                    "new items must use reloaded preset values");
            helper.assertTrue(portal.get(ModDataComponents.TRIAL_PORTAL_MODIFIERS.get()).skipRooms()==10,"existing item must remain unchanged");
        } finally { repository.publish(ValidationResult.success(original)); }
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (int i=0;i<36;i++) {
            ItemStack item=new ItemStack(Items.PAPER,i+1);
            item.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Inventory identity " + i));
            player.getInventory().setItem(i,item);
        }
        var armor=new ItemStack(Items.IRON_CHESTPLATE); var offhand=new ItemStack(Items.SHIELD);
        player.setItemSlot(EquipmentSlot.CHEST,armor); player.setItemSlot(EquipmentSlot.OFFHAND,offhand);
        var before=new ArrayList<ItemStack>(); for(int i=0;i<36;i++) before.add(player.getInventory().getItem(i).copy());
        MadnessRuntime.shuffle(player);
        var after=new ArrayList<ItemStack>(); for(int i=0;i<36;i++) after.add(player.getInventory().getItem(i));
        for(var item:before) {
            int match=-1; for(int i=0;i<after.size();i++) if(ItemStack.matches(item,after.get(i))) {match=i;break;}
            helper.assertTrue(match>=0,"shuffle must preserve every exact component/count stack"); after.remove(match);
        }
        helper.assertTrue(after.isEmpty(),"shuffle must not duplicate stacks");
        helper.assertTrue(ItemStack.matches(armor,player.getItemBySlot(EquipmentSlot.CHEST)),"armor must not shuffle");
        helper.assertTrue(ItemStack.matches(offhand,player.getItemBySlot(EquipmentSlot.OFFHAND)),"offhand must not shuffle");
        helper.succeed();
    }
}
