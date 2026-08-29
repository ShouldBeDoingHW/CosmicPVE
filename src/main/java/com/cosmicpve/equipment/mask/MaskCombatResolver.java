package com.cosmicpve.equipment.mask;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.cooldown.CooldownScope;
import com.cosmicpve.combat.pipeline.IncomingDamageContribution;
import com.cosmicpve.combat.pipeline.IncomingDamageContributor;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.content.definition.mask.MaskBehavior;
import java.util.List;
import net.minecraft.tags.DamageTypeTags;

public final class MaskCombatResolver implements OutgoingDamageContributor, IncomingDamageContributor {
    /** Legacy diagnostic ID retained for compatibility; Turkey now contributes to Dodge's single roll. */
    public static final net.minecraft.resources.Identifier TURKEY_DODGE = CosmicPVE.id("turkey_mask_dodge");
    private final MaskResolver masks;
    public MaskCombatResolver(MaskResolver masks) { this.masks = masks; }

    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.attacker() == null || context.channel() != DamageChannel.ORDINARY) return List.of();
        double bonus = masks.resolve(context.attacker()).stream().mapToDouble(definition -> switch (definition.behavior()) {
            case PURGE -> .03; case PARTY -> .01; case DRAGON -> .02; default -> 0.0;
        }).sum();
        return bonus == 0.0 ? List.of() : List.of(new OutgoingDamageContribution(CosmicPVE.id("mask_loadout"), bonus));
    }

    @Override public List<IncomingDamageContribution> resolveIncoming(CombatContext context) {
        if (context.target() == null) return List.of();
        var equipped = masks.resolve(context.target());
        if (context.damageSource() != null) {
            if (equipped.stream().anyMatch(d -> d.behavior() == MaskBehavior.DRAGON)
                    && context.damageSource().is(DamageTypeTags.IS_FIRE))
                return List.of(new IncomingDamageContribution(CosmicPVE.id("dragon_mask_fire_immunity"), 0.0));
            if (equipped.stream().anyMatch(d -> d.behavior() == MaskBehavior.ZEUS)
                    && context.damageSource().is(net.minecraft.world.damagesource.DamageTypes.LIGHTNING_BOLT))
                return List.of(new IncomingDamageContribution(CosmicPVE.id("zeus_mask_lightning_immunity"), 0.0));
        }
        return equipped.stream().anyMatch(d -> d.behavior() == MaskBehavior.PARTY) && context.channel() == DamageChannel.ORDINARY
                ? List.of(new IncomingDamageContribution(CosmicPVE.id("party_mask"), .99)) : List.of();
    }

}
