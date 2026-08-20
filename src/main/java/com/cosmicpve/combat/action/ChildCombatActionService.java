package com.cosmicpve.combat.action;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.TrueDamagePacket;
import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.WeaponSnapshot;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantments;
import java.util.Optional;
import java.util.Set;
import com.cosmicpve.combat.pipeline.AttackSequenceService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/** The only effect-facing delivery API for ordinary and true child damage. */
public final class ChildCombatActionService {
    private final AttackSequenceService sequences;
    private final TrueDamageDeliveryService trueDamage;

    public ChildCombatActionService(AttackSequenceService sequences, TrueDamageDeliveryService trueDamage) {
        this.sequences = sequences;
        this.trueDamage = trueDamage;
    }

    public CombatActionOutcome deliverTrue(
            CombatContext parent, LivingEntity target, TrueDamagePacket packet, RecursionPolicy policy) {
        var sequence = sequences.nextChild(parent.attackSequenceId());
        var child = parent.child(target, DamageChannel.TRUE, policy, sequence);
        return trueDamage.deliver(child, packet);
    }

    public CombatActionOutcome deliverTrueRoot(
            LivingEntity target,
            @Nullable LivingEntity actor,
            TrueDamagePacket packet,
            RecursionPolicy policy) {
        var rootSequence = sequences.nextRoot();
        var root = new CombatContext(
                actor, actor, actor, target,
                actor instanceof Player player ? Optional.of(player.getUUID()) : Optional.empty(),
                null, AttackCategory.UNKNOWN, DamageChannel.ORDINARY, Set.of(),
                actor == null ? WeaponSnapshot.empty() : new WeaponSnapshot(actor.getWeaponItem()),
                EffectiveEnchantments.EMPTY,
                rootSequence.id(), rootSequence.parentId(), RecursionPolicy.NORMAL);
        return deliverTrue(root, target, packet, policy);
    }

    public CombatActionOutcome deliverOrdinary(
            CombatContext parent, LivingEntity target, double amount, RecursionPolicy policy) {
        if (!Double.isFinite(amount) || amount <= 0.0) {
            throw new IllegalArgumentException("Ordinary child damage must be finite and positive");
        }
        var sequence = sequences.nextChild(parent.attackSequenceId());
        var child = parent.child(target, DamageChannel.ORDINARY, policy, sequence);
        if (!(target.level() instanceof ServerLevel level) || target.isDeadOrDying() || child.damageSource() == null) {
            return new CombatActionOutcome(sequence.id(), parent.attackSequenceId(), DamageChannel.ORDINARY, amount, 0.0, false);
        }
        float before = target.getHealth();
        boolean accepted = CombatDeliveryScope.call(
                child, null, () -> target.hurtServer(level, child.damageSource(), (float) amount));
        return new CombatActionOutcome(
                sequence.id(), parent.attackSequenceId(), DamageChannel.ORDINARY, amount,
                Math.max(0.0, before - target.getHealth()), accepted);
    }
}
