package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.action.ChildCombatActionService;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.TrueDamagePacket;
import com.cosmicpve.combat.proc.ProcActivation;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.server.level.ServerLevel;

public final class LightningBehavior {
    public static final double TRUE_DAMAGE = 2.0;

    private LightningBehavior() {}

    public static double chance(int level) {
        return Math.min(1.0, 0.05 * Math.max(0, level));
    }

    public static TrueDamagePacket packet() {
        return TrueDamagePacket.standard(CosmicPVE.id("lightning"), TRUE_DAMAGE);
    }

    public static void activate(ProcActivation activation, ChildCombatActionService childActions) {
        var event = activation.event();
        var target = event.target();
        var parent = event.combatResult().orElse(null);
        if (target == null || parent == null || target.isDeadOrDying()
                || !(target.level() instanceof ServerLevel level)) {
            return;
        }
        if (new com.cosmicpve.equipment.mask.MaskResolver(com.cosmicpve.content.CosmicContent.repository())
                .resolve(target).stream().anyMatch(mask -> mask.behavior()
                        == com.cosmicpve.content.definition.mask.MaskBehavior.ZEUS)) return;
        var bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.setVisualOnly(true);
            bolt.setPos(target.getX(), target.getY(), target.getZ());
            level.addFreshEntity(bolt);
        }
        com.cosmicpve.combat.CosmicCombat.lightning().deliver(activation, event.attacker(), target,
                parent.context(), CosmicPVE.id("lightning"), TRUE_DAMAGE, childActions);
    }
}
