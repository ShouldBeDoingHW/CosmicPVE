package com.cosmicpve.instance.protection;

import com.cosmicpve.trial.TrialRuntime;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.PistonEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;

public final class InstanceProtectionEventBridge {
    private final InstanceProtectionService service;
    public InstanceProtectionEventBridge(InstanceProtectionService service) { this.service = service; }

    public void onBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level
                && service.denies(level, event.getPlayer(), event.getPos(), InstanceMutationCause.BREAK)) event.setCanceled(true);
    }

    public void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            Player actor = event.getEntity() instanceof Player player ? player : null;
            if (actor instanceof ServerPlayer serverPlayer
                    && TrialRuntime.sessions().allowsCircuitPlacement(serverPlayer, event.getPos(), event.getPlacedBlock())) {
                TrialRuntime.sessions().onCircuitPlaced(serverPlayer, event.getPos(), event.getPlacedBlock());
                return;
            }
            if (service.denies(level, actor, event.getPos(), InstanceMutationCause.PLACE)) event.setCanceled(true);
        }
    }

    public void onUseItem(UseItemOnBlockEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            BlockPos intendedPlacement = new BlockPlaceContext(event.getUseOnContext()).getClickedPos();
            boolean circuitPlacement = TrialRuntime.sessions().allowsCircuitPlacementUse(
                    player, intendedPlacement, event.getItemStack());
            boolean roomUse = TrialRuntime.sessions().allowsProtectedRoomUse(player, event.getPos());
            boolean bombEgg = TrialRuntime.sessions().allowsBombSquadEggUse(player, event.getPos(), event.getItemStack());
            if (explicitAllowOverridesDefaultDeny(circuitPlacement, roomUse) || bombEgg) return;
        }
        if (event.getLevel() instanceof ServerLevel level
                && service.denies(level, event.getPlayer(), event.getPos(), InstanceMutationCause.USE)) {
            event.cancelWithResult(InteractionResult.FAIL);
        }
    }

    public static boolean explicitAllowOverridesDefaultDeny(boolean circuitPlacement, boolean circuitLever) {
        return circuitPlacement || circuitLever;
    }

    public void onExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel() instanceof ServerLevel level) {
            event.getAffectedBlocks().removeIf(pos -> !TrialRuntime.sessions().allowsBombSquadExplosion(
                    level, event.getExplosion(), pos)
                    && service.denies(level, null, pos, InstanceMutationCause.EXPLOSION));
        }
    }

    public void onPiston(PistonEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        var resolver = event.getStructureHelper();
        if (resolver == null || !resolver.resolve()) return;
        boolean denied = resolver.getToPush().stream().anyMatch(pos -> service.denies(level, null, pos, InstanceMutationCause.PISTON))
                || resolver.getToDestroy().stream().anyMatch(pos -> service.denies(level, null, pos, InstanceMutationCause.PISTON));
        if (denied) event.setCanceled(true);
    }

    public void onFluidPlace(BlockEvent.FluidPlaceBlockEvent event) {
        if (event.getLevel() instanceof ServerLevel level
                && service.denies(level, null, event.getPos(), InstanceMutationCause.FLUID)) event.setCanceled(true);
    }

    public void onLivingDestroy(LivingDestroyBlockEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level
                && service.denies(level, null, event.getPos(), InstanceMutationCause.MOB_GRIEF)) event.setCanceled(true);
    }

    public void onMobGrief(EntityMobGriefingEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level
                && service.protectedPosition(level, event.getEntity().blockPosition())) {
            event.setCanGrief(TrialRuntime.sessions().allowsBombSquadMobGrief(event.getEntity()));
        }
    }
}
