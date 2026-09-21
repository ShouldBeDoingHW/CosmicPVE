package com.cosmicpve.adventure;

import com.cosmicpve.registry.ModItems;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.minecraft.server.level.ServerPlayer;

public final class DenseWoodlandsBootstrap {
    public static final DenseWoodlandsSessionService SESSIONS = new DenseWoodlandsSessionService();
    private DenseWoodlandsBootstrap() {}
    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, WoodlandsPotInteractions::onRightClick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, com.cosmicpve.adventure.ranger.WoodlandsArenaService::onRightClick);
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> SESSIONS.tick(event.getServer()));
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,(LivingDeathEvent event) -> {
            if(!event.isCanceled() && event.getEntity() instanceof ServerPlayer p)SESSIONS.died(p);
        });
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,(LivingDropsEvent event) -> {
            if(event.getEntity() instanceof ServerPlayer)event.getDrops().removeIf(e -> e.getItem().is(ModItems.ADVENTURE_COMPASS.get()));
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent event) -> {
            if(event.getEntity() instanceof ServerPlayer p)DenseWoodlandsSessionService.removeCompasses(p);
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if(event.getEntity() instanceof ServerPlayer p && SESSIONS.session(p)==null){
                DenseWoodlandsSessionService.removeCompasses(p);
                if(com.cosmicpve.combat.CosmicCombat.activities().current(p)==com.cosmicpve.activity.ActivityType.ADVENTURE)
                    com.cosmicpve.combat.CosmicCombat.activities().clear(p.getUUID());
            }
        });
        NeoForge.EVENT_BUS.addListener((ItemTossEvent event) -> {
            if(event.getEntity().getItem().is(ModItems.ADVENTURE_COMPASS.get()))event.setCanceled(true);
        });
    }
}
