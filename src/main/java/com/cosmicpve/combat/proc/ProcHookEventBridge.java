package com.cosmicpve.combat.proc;

import com.cosmicpve.combat.execution.ExecutionService;
import com.cosmicpve.combat.enchantment.NutritionFoodService;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Thin authoritative adapters for proc hooks that do not originate in the damage commit bridge. */
public final class ProcHookEventBridge {
    private final ProcEventService events;
    private final ExecutionService executions;
    private final NutritionFoodService nutrition;

    public ProcHookEventBridge(
            ProcEventService events, ExecutionService executions, NutritionFoodService nutrition) {
        this.events = events;
        this.executions = executions;
        this.nutrition = nutrition;
    }

    public void onPreDeath(LivingDeathEvent event) {
        if (!events.hasCandidateSources() || event.getEntity().level().isClientSide()
                || executions.isExecuting(event.getEntity())) {
            return;
        }
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity living ? living : null;
        var result = events.dispatchRoot(ProcHook.ON_PRE_DEATH, event.getEntity(), attacker, event.getEntity());
        if (result.activationCount() > 0 && event.getEntity().getHealth() > 0.0F) {
            event.setCanceled(true);
        }
    }

    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!events.hasCandidateSources() || event.isCanceled() || event.getPlayer().level().isClientSide()) {
            return;
        }
        events.dispatchRoot(ProcHook.ON_BLOCK_BREAK, event.getPlayer(), event.getPlayer(), null);
    }

    public void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity().level().isClientSide() || event.getItem().get(DataComponents.FOOD) == null) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player) nutrition.applyCompletedFood(player);
        if (events.hasCandidateSources()) {
            events.dispatchRoot(ProcHook.ON_FOOD_EATEN, event.getEntity(), event.getEntity(), null);
        }
    }

    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (events.hasCandidateSources() && event.getEntity() instanceof ServerPlayer player) {
            events.dispatchRoot(ProcHook.PERIODIC_TICK, player, player, player);
        }
    }

    public void onEntityTick(EntityTickEvent.Post event) {
        if (events.hasCandidateSources() && event.getEntity() instanceof LivingEntity living
                && !(living instanceof Player) && !living.level().isClientSide()) {
            events.dispatchRoot(ProcHook.PERIODIC_TICK, living, living, living);
        }
    }
}
