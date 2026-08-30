package com.cosmicpve.combat.enchantment;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

/** Narrow bridge retaining an entity impact point until its corresponding damage calculation. */
public final class ProjectileImpactContextService {
    private final Map<Projectile, Impact> impacts = Collections.synchronizedMap(new WeakHashMap<>());

    public void onImpact(ProjectileImpactEvent event) {
        if (event.getProjectile().level().isClientSide()
                || !(event.getRayTraceResult() instanceof EntityHitResult hit)
                || !(hit.getEntity() instanceof LivingEntity target)) return;
        impacts.put(event.getProjectile(), new Impact(target.getUUID(), hit.getLocation()));
    }

    public Optional<Vec3> consume(Projectile projectile, LivingEntity target) {
        Impact impact = impacts.remove(projectile);
        return impact != null && impact.targetId().equals(target.getUUID())
                ? Optional.of(impact.location()) : Optional.empty();
    }

    private record Impact(UUID targetId, Vec3 location) {}
}
