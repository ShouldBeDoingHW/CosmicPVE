package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.action.ChildCombatActionService;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.TrueDamagePacket;
import com.cosmicpve.combat.cooldown.CooldownScope;
import com.cosmicpve.combat.cooldown.CooldownService;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.combat.proc.ProcActivation;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/** Offensive proc plus a fixed, non-stacking five-second outgoing buff. */
public final class DivineImmolationBehavior implements OutgoingDamageContributor {
    public static final Identifier COOLDOWN_KEY = CosmicPVE.id("divine_immolation");
    public static final Identifier BUFF_KEY = CosmicPVE.id("divine_immolation_buff");
    public static final long COOLDOWN_TICKS = 600L;
    public static final long BUFF_TICKS = 100L;
    public static final int FIRE_SECONDS = 5;
    public static final double TRUE_SELF_DAMAGE_HP = 2.0;
    public static final double OUTGOING_BONUS = 0.10;
    private final CooldownService cooldowns;

    public DivineImmolationBehavior(CooldownService cooldowns) {
        this.cooldowns = cooldowns;
    }

    public static double chance(int level) {
        return 0.03 * Math.max(0, Math.min(4, level));
    }

    public static TrueDamagePacket packet() {
        return TrueDamagePacket.standard(CosmicPVE.id("divine_immolation"), TRUE_SELF_DAMAGE_HP);
    }

    public static void activate(
            ProcActivation activation, ChildCombatActionService childActions, CooldownService cooldowns) {
        var wielder = activation.event().attacker();
        if (wielder == null || wielder.isDeadOrDying()) return;
        wielder.igniteForSeconds(FIRE_SECONDS);
        childActions.deliverTrueRoot(wielder, wielder, packet(), RecursionPolicy.NO_PROCS);
        cooldowns.set(wielder.getUUID(), BUFF_KEY, BUFF_TICKS, activation.event().serverTick(),
                CooldownScope.EPHEMERAL_COMBAT, Optional.empty());
    }

    @Override
    public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.attacker() == null) return List.of();
        var server = context.attacker().level().getServer();
        if (server == null || cooldowns.remainingTicks(
                context.attacker().getUUID(), BUFF_KEY, server.getTickCount()) == 0) return List.of();
        return List.of(new OutgoingDamageContribution(BUFF_KEY, OUTGOING_BONUS));
    }
}
