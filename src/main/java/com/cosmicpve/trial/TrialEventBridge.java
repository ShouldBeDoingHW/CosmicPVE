package com.cosmicpve.trial;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Creeper;
import com.cosmicpve.entity.undeadcorpse.UndeadCorpseEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.minecraft.world.damagesource.DamageTypes;

public final class TrialEventBridge {
    public void onServerStarted(ServerStartedEvent event) { TrialRuntime.sessions().recoverInterrupted(event.getServer()); }
    public void onServerTick(ServerTickEvent.Post event) { TrialRuntime.sessions().tick(event.getServer()); }
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) TrialRuntime.sessions().onLogin(player);
    }
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) TrialRuntime.sessions().onDisconnect(player);
    }
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) TrialRuntime.sessions().onRespawn(player);
    }
    public void onDeath(LivingDeathEvent event) {
        if (event.isCanceled()) return;
        if (event.getEntity() instanceof Zombie zombie) TrialRuntime.sessions().onRainbowZombieDeath(zombie);
        if (event.getEntity() instanceof Zombie zombie) TrialRuntime.sessions().onWarzoneGiantDeath(zombie);
        if (event.getEntity() instanceof UndeadCorpseEntity corpse) TrialRuntime.sessions().onUndeadCorpseDeath(corpse);
        if (event.getEntity() instanceof ServerPlayer player) TrialRuntime.sessions().onDeath(player);
    }
    public void onDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof Zombie zombie
                && com.cosmicpve.trial.room.RaidingRainbowService.encounterZombie(zombie)) event.getDrops().clear();
        if (event.getEntity() instanceof Zombie zombie
                && com.cosmicpve.trial.room.WarzoneGiantsService.encounterGiant(zombie)) event.getDrops().clear();
        if (event.getEntity() instanceof Shulker shulker
                && com.cosmicpve.trial.room.ZeroGService.encounterShulker(shulker)) event.getDrops().clear();
        if (event.getEntity() instanceof Creeper creeper
                && com.cosmicpve.trial.room.BombSquadService.encounterCreeper(creeper)) event.getDrops().clear();
        if (event.getEntity() instanceof UndeadCorpseEntity corpse && corpse.trialSession().isPresent()) event.getDrops().clear();
    }
    public void onExperience(LivingExperienceDropEvent event) {
        if (event.getEntity() instanceof Zombie zombie
                && com.cosmicpve.trial.room.RaidingRainbowService.encounterZombie(zombie)) event.setDroppedExperience(0);
        if (event.getEntity() instanceof Zombie zombie
                && com.cosmicpve.trial.room.WarzoneGiantsService.encounterGiant(zombie)) event.setDroppedExperience(0);
        if (event.getEntity() instanceof Shulker shulker
                && com.cosmicpve.trial.room.ZeroGService.encounterShulker(shulker)) event.setDroppedExperience(0);
        if (event.getEntity() instanceof Creeper creeper
                && com.cosmicpve.trial.room.BombSquadService.encounterCreeper(creeper)) event.setDroppedExperience(0);
        if (event.getEntity() instanceof UndeadCorpseEntity corpse && corpse.trialSession().isPresent()) event.setDroppedExperience(0);
    }
    public void onBlockDrops(BlockDropsEvent event) { TrialRuntime.sessions().onCaveDivingDrops(event); }
    public void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof AbstractArrow arrow && arrow.getOwner() instanceof ServerPlayer player
                && event.getRayTraceResult() instanceof BlockHitResult hit) {
            boolean accepted = TrialRuntime.sessions().onCircuitTarget(player, hit.getBlockPos())
                    || TrialRuntime.sessions().onDeadeyeTarget(player, hit.getBlockPos());
            if (accepted) { arrow.discard(); event.setCanceled(true); }
        }
    }
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TrialRuntime.sessions().onCircuitLever(player, event.getPos());
            TrialRuntime.sessions().onFireColonyLever(player, event.getPos());
            TrialRuntime.sessions().onColdSnapLever(player, event.getPos());
            TrialRuntime.sessions().onDeadeyeLever(player, event.getPos());
        }
    }
    public void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel level) {
            TrialRuntime.sessions().onColdSnapPlate(level, event.getPos(), event.getState());
            TrialRuntime.sessions().onBombSquadPlate(level, event.getPos(), event.getState());
        }
    }
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel level
                && event.getEntity() instanceof Creeper creeper
                && com.cosmicpve.trial.room.BombSquadService.encounterCreeper(creeper)
                && !TrialRuntime.sessions().onBombSquadCreeperJoin(level, creeper)) event.setCanceled(true);
    }
    public void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Zombie zombie)
                || !com.cosmicpve.trial.room.WarzoneGiantsService.encounterGiant(zombie)) return;
        if (event.getSource().is(DamageTypes.IN_WALL)) {
            TrialRuntime.sessions().recoverWarzoneGiantFromSuffocation(zombie);
            event.setCanceled(true);
        } else if (!TrialRuntime.sessions().allowsWarzoneGiantDamage(zombie)) event.setCanceled(true);
    }
}
