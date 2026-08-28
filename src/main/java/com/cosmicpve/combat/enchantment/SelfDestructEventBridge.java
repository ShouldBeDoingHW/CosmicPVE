package com.cosmicpve.combat.enchantment;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

/** Scoped damage and terrain policy for Self Destruct's four event-owned vanilla TNT entities. */
public final class SelfDestructEventBridge {
    public void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (SelfDestructBehavior.isImmuneOwner(event.getEntity(), event.getSource().getDirectEntity())) {
            event.setCanceled(true);
        }
    }

    public void onExplosion(ExplosionEvent.Detonate event) {
        if (SelfDestructBehavior.isEventTnt(event.getExplosion().getDirectSourceEntity())) {
            event.getAffectedBlocks().clear();
        }
    }
}
