package com.cosmicpve.equipment.accessory;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.CombatResult;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.IncomingDamageContribution;
import com.cosmicpve.combat.pipeline.IncomingDamageContributor;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import java.util.List;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;

public final class BeltCombatService implements OutgoingDamageContributor, IncomingDamageContributor {
    public static final double BANDOLIER_BONUS = 0.12;
    public static final double JELLY_INCOMING = 0.98;
    public static final double CINDERWOLF_FIRE_INCOMING = 0.85;
    private final AccessoryResolver accessories;
    private final BandolierStateService bandolier;
    public BeltCombatService(AccessoryResolver accessories, BandolierStateService bandolier) {
        this.accessories = accessories; this.bandolier = bandolier;
    }
    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (!(context.attacker() instanceof Player player) || !qualifyingSword(context)) return List.of();
        if (!accessories.hasBelt(player, BeltDefinition.BANDOLIER)) { bandolier.reset(player); return List.of(); }
        return bandolier.charged(player)
                ? List.of(new OutgoingDamageContribution(BeltDefinition.BANDOLIER.id(), BANDOLIER_BONUS)) : List.of();
    }
    @Override public List<IncomingDamageContribution> resolveIncoming(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.target() == null) return List.of();
        var result = new java.util.ArrayList<IncomingDamageContribution>();
        if (accessories.hasBelt(context.target(), BeltDefinition.JELLY_ROLL))
            result.add(new IncomingDamageContribution(BeltDefinition.JELLY_ROLL.id(), JELLY_INCOMING));
        if (accessories.hasBelt(context.target(), BeltDefinition.CINDERWOLF)
                && context.damageSource() != null
                && context.damageSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE))
            result.add(new IncomingDamageContribution(BeltDefinition.CINDERWOLF.id(), CINDERWOLF_FIRE_INCOMING));
        return List.copyOf(result);
    }
    public void onCommitted(CombatResult result) {
        if (!(result.context().attacker() instanceof Player player)) return;
        if (!accessories.hasBelt(player, BeltDefinition.BANDOLIER)) { bandolier.reset(player); return; }
        if (result.isCommittedDamagingHit() && qualifyingSword(result.context())) bandolier.committed(player);
    }
    public void reconcile(Player player) {
        if (!accessories.hasBelt(player, BeltDefinition.BANDOLIER)) bandolier.reset(player);
    }
    public void reset(Player player) { bandolier.reset(player); }
    public void afterDevour(Player player) {
        if (!accessories.hasBelt(player, BeltDefinition.JELLY_ROLL)) return;
        var food = player.getFoodData();
        float saturation = food.getSaturationLevel();
        food.setSaturation(Math.min(food.getFoodLevel(), saturation * 1.20F));
    }
    public static boolean qualifyingSword(CombatContext context) {
        return context.channel() == DamageChannel.ORDINARY && context.category() == AttackCategory.MELEE
                && context.parentSequenceId().isEmpty() && context.weaponSnapshot().stack().is(ItemTags.SWORDS);
    }
}
