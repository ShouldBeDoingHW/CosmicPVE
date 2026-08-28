package com.cosmicpve.conquest;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.PistonEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class ConquestEventBridge {
    private final ConquestEventService service;
    public ConquestEventBridge(ConquestEventService service) { this.service = service; }

    public void onServerTick(ServerTickEvent.Post event) { service.tick(event.getServer()); }

    public void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)
            service.interact(player, event.getPos());
    }

    public void onBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level
                && service.deniesMutation(level, event.getPlayer(), event.getPos(), true)) event.setCanceled(true);
    }

    public void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Player player = event.getEntity() instanceof Player actor ? actor : null;
        if (service.deniesMutation(level, player, event.getPos(), false)) event.setCanceled(true);
    }

    public void onUseItem(UseItemOnBlockEvent event) {
        if (event.getLevel() instanceof ServerLevel level
                && service.deniesMutation(level, event.getPlayer(), event.getPos(), false))
            event.cancelWithResult(InteractionResult.FAIL);
    }

    public void onExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel() instanceof ServerLevel level)
            event.getAffectedBlocks().removeIf(pos -> service.deniesMutation(level, null, pos, false));
    }

    public void onPiston(PistonEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        var resolver = event.getStructureHelper();
        if (resolver == null || !resolver.resolve()) return;
        if (resolver.getToPush().stream().anyMatch(pos -> service.deniesMutation(level, null, pos, false))
                || resolver.getToDestroy().stream().anyMatch(pos -> service.deniesMutation(level, null, pos, false)))
            event.setCanceled(true);
    }

    public void onFluid(BlockEvent.FluidPlaceBlockEvent event) {
        if (event.getLevel() instanceof ServerLevel level
                && service.deniesMutation(level, null, event.getPos(), false)) event.setCanceled(true);
    }

    public void onLivingDestroy(LivingDestroyBlockEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level
                && service.deniesMutation(level, null, event.getPos(), false)) event.setCanceled(true);
    }

    public void onMobGrief(EntityMobGriefingEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level
                && service.protectedPosition(level.getServer(), event.getEntity().blockPosition()))
            event.setCanGrief(false);
    }
}
