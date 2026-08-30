package com.cosmicpve.combat.enchantment;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class SnareEventBridge {
    private final SnareRootService roots;

    public SnareEventBridge(SnareRootService roots) {
        this.roots = roots;
    }

    public void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living && !living.level().isClientSide()
                && living.level().getServer() != null) {
            roots.tick(living, living.level().getServer().getTickCount());
        }
    }
}
