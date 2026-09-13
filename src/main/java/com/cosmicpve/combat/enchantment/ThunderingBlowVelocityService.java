package com.cosmicpve.combat.enchantment;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Executes at server-tick tail, after the attack method has applied ordinary knockback. */
public final class ThunderingBlowVelocityService {
    private final Map<LivingEntity, Integer> pending = Collections.synchronizedMap(new WeakHashMap<>());
    public void schedule(LivingEntity target, int serverTick) { pending.put(target, serverTick); }
    public int pendingCount() { return pending.size(); }
    public void onServerTick(ServerTickEvent.Post event) {
        int now = event.getServer().getTickCount();
        synchronized (pending) {
            var iterator = pending.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                var target = entry.getKey();
                if (target == null || target.isRemoved() || target.isDeadOrDying()) { iterator.remove(); continue; }
                if (entry.getValue() <= now) {
                    target.setDeltaMovement(Vec3.ZERO); target.hurtMarked = true; iterator.remove();
                }
            }
        }
    }
}
