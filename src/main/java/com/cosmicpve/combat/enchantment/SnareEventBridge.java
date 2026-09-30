package com.cosmicpve.combat.enchantment;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class SnareEventBridge {
    private final SnareRootService roots;
    private final com.cosmicpve.combat.stack.CombatStackService stacks;

    public SnareEventBridge(SnareRootService roots) {
        this(roots, null);
    }

    public SnareEventBridge(SnareRootService roots, com.cosmicpve.combat.stack.CombatStackService stacks) {
        this.roots = roots;
        this.stacks = stacks;
    }

    public void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living && !living.level().isClientSide()
                && living.level().getServer() != null) {
            roots.tick(living, living.level().getServer().getTickCount());
            if (stacks != null) roots.tickTrap(living,
                    stacks.count(living, TrapBehavior.STACK_ID, living.level().getServer().getTickCount()) > 0);
        }
    }

    public void onTeleport(net.neoforged.neoforge.event.entity.EntityTeleportEvent event) {
        if (event.getEntity() instanceof LivingEntity living && !living.level().isClientSide())
            roots.markTeleport(living.getUUID());
    }
}
