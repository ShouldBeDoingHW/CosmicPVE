package com.cosmicpve.trial.room;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.entity.inventor.InventorEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/** Dynamic station bonus and one-shot player charge in the shared ordinary outgoing bucket. */
public final class InventorCombatContributor implements OutgoingDamageContributor {
    public static final double BONUS_PER_ACTIVE_STATION = 0.05D;
    public static final double STRIKE_CHARGE_BONUS = 1.0D;
    public static double stationBonus(int activeStations) {
        return Math.max(0, Math.min(4, activeStations)) * BONUS_PER_ACTIVE_STATION;
    }

    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY) return List.of();
        if (context.attacker() instanceof InventorEntity inventor && inventor.activeStations() > 0)
            return List.of(new OutgoingDamageContribution(CosmicPVE.id("trial/inventor_stations"),
                    stationBonus(inventor.activeStations())));
        if (context.attacker() instanceof Player player && context.target() instanceof InventorEntity inventor
                && context.parentSequenceId().isEmpty() && context.recursionPolicy() == RecursionPolicy.NORMAL
                && inventor.hasStrikeCharge(player.getUUID()))
            return List.of(new OutgoingDamageContribution(CosmicPVE.id("trial/inventor_strike_charge"),
                    STRIKE_CHARGE_BONUS));
        return List.of();
    }
}
