package com.cosmicpve.equipment.accessory;

import com.cosmicpve.combat.action.ChildCombatActionService;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.TrueDamagePacket;
import com.cosmicpve.combat.proc.ProcActivation;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/** Semantic delivery boundary for owned Cosmic lightning, excluding vanilla/environmental bolts. */
public final class CosmicLightningService {
    public static final double SHOCK_TRUE_DAMAGE = 1.0;
    public static final float SHOCK_HEAL = 0.25F;
    private final AccessoryResolver accessories;
    private final Set<ProcActivation> healed = Collections.newSetFromMap(new WeakHashMap<>());
    public CosmicLightningService(AccessoryResolver accessories) { this.accessories = accessories; }
    public boolean deliver(ProcActivation activation, LivingEntity owner, LivingEntity target, CombatContext parent,
            Identifier source, double baseDamage, ChildCombatActionService actions) {
        if (new com.cosmicpve.equipment.mask.MaskResolver(com.cosmicpve.content.CosmicContent.repository())
                .resolve(target).stream().anyMatch(mask -> mask.behavior()
                        == com.cosmicpve.content.definition.mask.MaskBehavior.ZEUS)) return false;
        boolean shock = accessories.hasBelt(owner, BeltDefinition.SHOCK_THERAPY);
        var outcome = actions.deliverTrue(parent, target,
                TrueDamagePacket.standard(source, baseDamage + (shock ? SHOCK_TRUE_DAMAGE : 0.0)), RecursionPolicy.NO_PROCS);
        if (shock && outcome.accepted() && healed.add(activation)) owner.heal(SHOCK_HEAL);
        return outcome.accepted();
    }
}
