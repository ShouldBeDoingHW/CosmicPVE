package com.cosmicpve.adventure;

import com.cosmicpve.activity.ActivityType;
import com.cosmicpve.combat.CosmicCombat;
import com.cosmicpve.registry.ModItems;
import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.*;
import net.minecraft.sounds.*;
import java.util.*;
import static com.cosmicpve.adventure.AdventureSession.Phase.*;

/** Per-player durable entry, extraction, and failure transaction owner. */
public final class DenseWoodlandsSessionService {
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
            com.cosmicpve.CosmicPVE.id("dense_woodlands"));
    public static final int TRANSITION_TICKS=100;
    public static final int COMPASS_COOLDOWN_TICKS=100;
    public static final String COMPASS_NEXT_HINT="cosmic_adventure_next_hint";
    public AdventureSession session(ServerPlayer p) { return AdventureSavedData.get(p.level().getServer()).get(p.getUUID()); }
    private long now(MinecraftServer server) { return server.overworld().getGameTime(); }
    public boolean enter(ServerPlayer player, int minutes, net.minecraft.world.InteractionHand hand) {
        var server=player.level().getServer();var data=AdventureSavedData.get(server);
        if(data.get(player.getUUID())!=null || !player.isAlive() || player.isSpectator()
                || CosmicCombat.activities().current(player)!=ActivityType.NONE
                || player.level().dimension().equals(DIMENSION) || server.getLevel(DIMENSION)==null
                || com.cosmicpve.trial.TrialRuntime.sessions().active(server).map(s->s.activeParticipant(player.getUUID())).orElse(false)) return false;
        int slot=hand==net.minecraft.world.InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot():40;
        var call=player.getInventory().getItem(slot);
        if(!call.has(ModDataComponents.CALL_OF_FOREST.get()))return false;
        var home=new AdventureSession.ReturnPoint(player.level().dimension(),player.position(),player.getYRot(),player.getXRot(),player.gameMode.getGameModeForPlayer());
        var session=new AdventureSession(player.getUUID(),UUID.randomUUID(),minutes,PREPARED,home,BlockPos.ZERO,BlockPos.ZERO,
                now(server)+TRANSITION_TICKS,slot,call.getCount());
        player.closeContainer();
        data.put(server,session);
        consume(player,session);
        return true;
    }
    private void consume(ServerPlayer player, AdventureSession s) {
        var server=player.level().getServer();var stack=player.getInventory().getItem(s.callSlot());
        // PREPARED is journaled before consumption; saved count reconciles a restart at either checkpoint.
        if(stack.getCount()==s.callCount() && stack.has(ModDataComponents.CALL_OF_FOREST.get())) stack.shrink(1);
        else if(stack.getCount()!=s.callCount()-1) throw new IllegalStateException("Adventure Call checkpoint mismatch for "+s.owner());
        server.getPlayerList().getPlayerIo().save(player);
        AdventureSavedData.get(server).put(server,s.phase(TRANSITION,now(server)+TRANSITION_TICKS));
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS,TRANSITION_TICKS,0,false,false,true));
        player.addEffect(new MobEffectInstance(MobEffects.NAUSEA,TRANSITION_TICKS,0,false,false,true));
        player.level().playSound(null,player.blockPosition(),SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(5).value(),SoundSource.PLAYERS,1F,1F);
    }
    public void tick(MinecraftServer server) {
        for(var player:server.getPlayerList().getPlayers())tickPlayer(player);
    }
    public void tickPlayer(ServerPlayer player) {
        var server=player.level().getServer();var data=AdventureSavedData.get(server);long time=now(server);
        var s=data.get(player.getUUID());if(s==null)return;
            switch(s.phase()) {
                case PREPARED -> consume(player,s);
                case TRANSITION -> { if(!player.isAlive()) data.put(server,s.phase(DEAD,time)); else if(time>=s.deadline()) activate(player,s); }
                case ACTIVE -> {
                    if(!player.isAlive()) { died(player); return; }
                    if(!player.level().dimension().equals(DIMENSION)) arrive(player,s);
                    CosmicCombat.activities().set(s.owner(),ActivityType.ADVENTURE);
                    player.setGameMode(GameType.ADVENTURE);
                    ensureCompass(player,s);
                    if(time>=s.deadline()) {
                        CosmicCombat.executions().execute(player,new com.cosmicpve.combat.execution.ExecutionCause(com.cosmicpve.CosmicPVE.id("adventure_timeout")),null,null);
                    } else if(time%20==0) {
                        long seconds=(s.deadline()-time+19)/20;
                        player.displayClientMessage(Component.literal("Dense Woodlands: "+seconds/60+":"+String.format(java.util.Locale.ROOT,"%02d",seconds%60)),true);
                    }
                }
                case RETURNING -> restore(player,s);
                case DEAD -> { if(player.isAlive())restore(player,s); }
            }
    }
    private void activate(ServerPlayer player, AdventureSession s) {
        var server=player.level().getServer();var level=server.getLevel(DIMENSION);
        if(level==null)return;
        BlockPos entry=null,exit=null;
        for(int attempt=0;attempt<8 && entry==null;attempt++) {
            int x=player.getRandom().nextIntBetweenInclusive(-100000,100000),z=player.getRandom().nextIntBetweenInclusive(-100000,100000);
            entry=AdventureSurface.nearby(level,x,z,12).orElse(null);
        }
        if(entry!=null)for(int attempt=0;attempt<16 && exit==null;attempt++) {
            double angle=player.getRandom().nextDouble()*Math.PI*2;
            double distance=s.minutes()*100*(.5+.4*player.getRandom().nextDouble());
            BlockPos proposed=AdventureExit.find(level,entry.getX()+(int)(Math.cos(angle)*distance),entry.getZ()+(int)(Math.sin(angle)*distance)).orElse(null);
            if(proposed!=null && AdventureExit.valid(level,proposed))exit=proposed;
        }
        if(entry==null || exit==null) {
            com.cosmicpve.CosmicPVE.LOGGER.warn("Adventure destination failed owner={} entry={} exit={}",s.owner(),entry,exit);
            player.displayClientMessage(Component.literal("No safe woodland destination found; your Call has been returned."),false);
            var item=s.minutes()==10?ModItems.CALL_OF_FOREST_10:s.minutes()==20?ModItems.CALL_OF_FOREST_20:ModItems.CALL_OF_FOREST_30;
            // Bounded-generation failure is recovered in the source world.
            if(!player.getInventory().add(new ItemStack(item.get())))player.drop(new ItemStack(item.get()),false);
            AdventureSavedData.get(server).remove(server,s.owner());return;
        }
        var active=s.activate(entry,exit,now(server)+s.minutes()*1200L);
        AdventureSavedData.get(server).put(server,active);
        AdventureExit.place(level,exit);
        arrive(player,active);
    }
    private void arrive(ServerPlayer player,AdventureSession s) {
        var level=player.level().getServer().getLevel(DIMENSION); if(level==null)return;
        AdventureExit.place(level,s.exit());
        player.closeContainer();player.setGameMode(GameType.ADVENTURE);
        player.teleportTo(level,s.entry().getX()+.5,s.entry().getY(),s.entry().getZ()+.5,Set.of(),player.getYRot(),player.getXRot(),true);
        CosmicCombat.activities().set(s.owner(),ActivityType.ADVENTURE);
        ensureCompass(player,s);player.level().getServer().getPlayerList().getPlayerIo().save(player);
    }
    public boolean extract(ServerPlayer player, BlockPos portal) {
        var s=session(player);
        if(s==null || s.phase()!=ACTIVE || !player.level().dimension().equals(DIMENSION)
                || !(portal.equals(s.exit()) || portal.equals(s.exit().above())))return false;
        var closing=s.phase(RETURNING,now(player.level().getServer()));
        AdventureSavedData.get(player.level().getServer()).put(player.level().getServer(),closing);restore(player,closing);return true;
    }
    public void died(ServerPlayer player) {
        var s=session(player);if(s==null)return;
        removeCompasses(player);
        CosmicCombat.activities().clear(s.owner());player.getPersistentData().remove(COMPASS_NEXT_HINT);
        var server=player.level().getServer();
        if(s.phase()==ACTIVE) {
            if(Boolean.TRUE.equals(player.level().getGameRules().get(net.minecraft.world.level.gamerules.GameRules.KEEP_INVENTORY)))
                player.getInventory().dropAll();
            AdventureExit.remove(server.getLevel(DIMENSION),s.exit());AdventureZombies.cleanup(server.getLevel(DIMENSION),s.id());
        }
        AdventureSavedData.get(server).put(server,s.phase(DEAD,now(server)));
    }
    private void restore(ServerPlayer player,AdventureSession s) {
        var server=player.level().getServer();var level=server.getLevel(s.home().dimension());
        if(level==null) { player.displayClientMessage(Component.literal("Adventure return dimension unavailable; recovery is pending."),true);return; }
        removeCompasses(player);CosmicCombat.activities().clear(s.owner());player.getPersistentData().remove(COMPASS_NEXT_HINT);
        if(!s.exit().equals(BlockPos.ZERO)) { AdventureExit.remove(server.getLevel(DIMENSION),s.exit());AdventureZombies.cleanup(server.getLevel(DIMENSION),s.id()); }
        var p=s.home().position();player.setGameMode(s.home().mode());
        player.teleportTo(level,p.x,p.y,p.z,Set.of(),s.home().yaw(),s.home().pitch(),true);
        server.getPlayerList().getPlayerIo().save(player);AdventureSavedData.get(server).remove(server,s.owner());
    }
    public boolean hint(ServerPlayer player,ItemStack stack) {
        var s=session(player);long time=now(player.level().getServer());
        if(s==null || s.phase()!=ACTIVE || !player.isAlive() || !player.level().dimension().equals(DIMENSION)
                || !owned(stack,s) || time<player.getPersistentData().getLongOr(COMPASS_NEXT_HINT,0L))return false;
        int distance=(int)Math.ceil(Math.sqrt(player.position().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(s.exit()))));
        player.sendSystemMessage(Component.literal("Hurry up Cosmonaut! The exit portal is "+distance+" blocks away!"));
        player.getPersistentData().putLong(COMPASS_NEXT_HINT,time+COMPASS_COOLDOWN_TICKS);
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.EXPERIENCE_ORB_PICKUP),
                SoundSource.PLAYERS,player.getX(),player.getY(),player.getZ(),1F,1F,player.getRandom().nextLong()));
        return true;
    }
    public static boolean owned(ItemStack stack,AdventureSession s) {
        var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        return stack.is(ModItems.ADVENTURE_COMPASS.get()) && tag.getStringOr("adventure_session","").equals(s.id().toString())
                && tag.getStringOr("adventure_owner","").equals(s.owner().toString());
    }
    private void ensureCompass(ServerPlayer player,AdventureSession s) {
        boolean found=false;
        for(int i=0;i<player.getInventory().getContainerSize();i++) {
            var stack=player.getInventory().getItem(i);
            if(!stack.is(ModItems.ADVENTURE_COMPASS.get()))continue;
            if(!found && owned(stack,s)) {
                found=true;
                stack.set(DataComponents.LODESTONE_TRACKER,compassTarget(s));
            } else player.getInventory().setItem(i,ItemStack.EMPTY);
        }
        if(found)return;
        var compass=new ItemStack(ModItems.ADVENTURE_COMPASS.get());var tag=new CompoundTag();
        tag.putString("adventure_owner",s.owner().toString());tag.putString("adventure_session",s.id().toString());
        compass.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        compass.set(DataComponents.LODESTONE_TRACKER,compassTarget(s));
        if(!player.getInventory().add(compass)) {
            var displaced=player.getInventory().removeItemNoUpdate(8);player.getInventory().setItem(8,compass);player.drop(displaced,false);
        }
    }
    public static LodestoneTracker compassTarget(AdventureSession session) {
        return new LodestoneTracker(Optional.of(GlobalPos.of(DIMENSION,session.exit())),false);
    }
    public static void removeCompasses(ServerPlayer player) {
        for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i).is(ModItems.ADVENTURE_COMPASS.get()))player.getInventory().setItem(i,ItemStack.EMPTY);
        if(player.containerMenu.getCarried().is(ModItems.ADVENTURE_COMPASS.get()))player.containerMenu.setCarried(ItemStack.EMPTY);
    }
}
