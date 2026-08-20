package com.cosmicpve.combat.stack;

import com.cosmicpve.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Cheap server tick/clone adapter; stack mutation remains in CombatStackService. */
public final class CombatStackEventBridge {
    private final CombatStackService stacks;
    private final BleedRuntimeService bleed;

    public CombatStackEventBridge(CombatStackService stacks, BleedRuntimeService bleed) {
        this.stacks = stacks;
        this.bleed = bleed;
    }

    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            expireExisting(player);
        }
    }

    public void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living
                && !(living instanceof Player) && !living.level().isClientSide()) {
            expireExisting(living);
        }
    }

    public void onPlayerClone(PlayerEvent.Clone event) {
        var original = event.getOriginal().getExistingDataOrNull(ModAttachments.COMBAT_STACKS);
        if (original == null || original.isEmpty()) {
            bleed.clearMovementIfTracked(event.getEntity());
            return;
        }
        var server = event.getEntity().level().getServer();
        if (server != null) {
            stacks.expireDue(original, server.getTickCount());
            if (original.isEmpty()) {
                bleed.clearMovementIfTracked(event.getEntity());
                return;
            }
        }
        var copied = stacks.copyForClone(original, event.isWasDeath());
        if (!copied.isEmpty()) {
            event.getEntity().setData(ModAttachments.COMBAT_STACKS, copied);
        }
        bleed.reconcileMovement(event.getEntity(), copied.count(com.cosmicpve.combat.enchantment.BleedBehavior.STACK_ID));
    }

    private void expireExisting(LivingEntity entity) {
        var container = entity.getExistingDataOrNull(ModAttachments.COMBAT_STACKS);
        if (container == null) {
            bleed.clearMovementIfTracked(entity);
            return;
        }
        long tick = entity.level().getServer().getTickCount();
        if (container.isExpirationDue(tick)) {
            stacks.expireDue(container, tick);
            if (container.isEmpty()) {
                bleed.reconcileMovement(entity, 0);
                entity.removeData(ModAttachments.COMBAT_STACKS);
                return;
            }
        }
        bleed.tick(entity, container, tick);
    }
}
