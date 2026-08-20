package com.cosmicpve.combat.stack;

import com.cosmicpve.combat.action.ChildCombatActionService;
import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatFlag;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.enchantment.BleedBehavior;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** The deliberately specific Java runtime module for Bleed's stack-driven effects. */
public final class BleedRuntimeService {
    public static final Identifier MOVEMENT_MODIFIER_ID = BleedBehavior.STACK_ID.withSuffix("_movement");

    private final ChildCombatActionService childActions;
    private final Set<UUID> movementModifiedEntities = ConcurrentHashMap.newKeySet();

    public BleedRuntimeService(ChildCombatActionService childActions) {
        this.childActions = childActions;
    }

    public void tick(LivingEntity target, CombatStackContainer container, long currentTick) {
        var instances = container.snapshot().getOrDefault(BleedBehavior.STACK_ID, java.util.List.of());
        reconcileMovement(target, instances.size());
        if (!(target.level() instanceof ServerLevel level)) {
            return;
        }
        for (var stack : instances) {
            if (!BleedBehavior.isTickDue(stack.applicationTick(), stack.expirationTick(), currentTick)) {
                continue;
            }
            Entity source = stack.originalSourceEntityId().map(level::getEntity).orElse(null);
            LivingEntity attacker = source instanceof LivingEntity living ? living : null;
            Entity credited = stack.creditedPlayerId()
                    .map(id -> level.getServer().getPlayerList().getPlayer(id))
                    .map(Entity.class::cast)
                    .orElse(source);
            childActions.deliverAttributedTrueRoot(
                    target, source, credited, attacker, stack.creditedPlayerId(),
                    AttackCategory.DAMAGE_OVER_TIME, Set.of(CombatFlag.DAMAGE_OVER_TIME),
                    BleedBehavior.tickPacket(), RecursionPolicy.NO_PROCS);
        }
    }

    public void reconcileMovement(LivingEntity target, int activeStacks) {
        var movement = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement == null) {
            return;
        }
        if (activeStacks <= 0) {
            movement.removeModifier(MOVEMENT_MODIFIER_ID);
            movementModifiedEntities.remove(target.getUUID());
            return;
        }
        movement.addOrUpdateTransientModifier(new AttributeModifier(
                MOVEMENT_MODIFIER_ID,
                BleedBehavior.movementMultiplierAmount(activeStacks),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        movementModifiedEntities.add(target.getUUID());
    }

    public void clearMovementIfTracked(LivingEntity target) {
        if (movementModifiedEntities.contains(target.getUUID())) {
            reconcileMovement(target, 0);
        }
    }
}
