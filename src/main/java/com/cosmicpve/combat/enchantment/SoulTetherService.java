package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Owner-specific tether state. The target slow exists while at least one live relation remains. */
public final class SoulTetherService implements OutgoingDamageContributor {
    public static final Identifier MOVEMENT_MODIFIER = CosmicPVE.id("soul_tether_movement");
    public static final Identifier DAMAGE_SOURCE = CosmicPVE.id("soul_tether_distance");
    private final Map<UUID, Map<UUID, Long>> relations = new HashMap<>();

    public static int durationTicks(int level) { return (5 + Math.max(1, Math.min(3, level))) * 20; }
    public static double damageBonus(double distance) { return Math.max(0, distance) * .05; }
    public synchronized void apply(LivingEntity owner, LivingEntity target, int level, long now) {
        relations.computeIfAbsent(target.getUUID(), ignored -> new HashMap<>())
                .put(owner.getUUID(), now + durationTicks(level));
        reconcileSlow(target, true);
    }
    public synchronized boolean active(UUID owner, LivingEntity target, long now) {
        prune(target, now);
        var owners = relations.get(target.getUUID());
        return owners != null && owners.getOrDefault(owner, 0L) > now;
    }
    public synchronized void tick(LivingEntity entity, long now) {
        if (relations.containsKey(entity.getUUID())) prune(entity, now);
    }
    private void prune(LivingEntity target, long now) {
        var owners = relations.get(target.getUUID());
        if (owners == null) return;
        owners.entrySet().removeIf(entry -> {
            if (entry.getValue() <= now) return true;
            var owner = target.level().getEntityInAnyDimension(entry.getKey());
            return !(owner instanceof LivingEntity living) || living.isDeadOrDying() || living.isRemoved();
        });
        if (owners.isEmpty()) { relations.remove(target.getUUID()); reconcileSlow(target, false); }
    }
    private static void reconcileSlow(LivingEntity target, boolean active) {
        var movement = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement == null) return;
        if (!active) movement.removeModifier(MOVEMENT_MODIFIER);
        else movement.addOrUpdateTransientModifier(new AttributeModifier(
                MOVEMENT_MODIFIER, -.20, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.attacker() == null || context.target() == null) return List.of();
        var server = context.attacker().level().getServer();
        if (server == null || !active(context.attacker().getUUID(), context.target(), server.getTickCount())) return List.of();
        return List.of(new OutgoingDamageContribution(DAMAGE_SOURCE, damageBonus(context.attacker().distanceTo(context.target()))));
    }
}
