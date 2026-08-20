package com.cosmicpve.combat.stack;

import com.cosmicpve.content.definition.stack.StackDefinition;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/** Immutable inspection view. A missing definition is represented explicitly instead of throwing. */
public record ActiveCombatStack(
        Identifier definitionId,
        Optional<StackDefinition> definition,
        long oldestDefinitionRevision,
        List<CombatStackInstance> instances) {
    public ActiveCombatStack {
        definition = definition == null ? Optional.empty() : definition;
        instances = List.copyOf(instances);
    }

    public int count() {
        return instances.size();
    }

    public long minimumRemainingTicks(long currentTick) {
        return instances.stream().mapToLong(stack -> Math.max(0L, stack.expirationTick() - currentTick))
                .min().orElse(0L);
    }

    public long maximumRemainingTicks(long currentTick) {
        return instances.stream().mapToLong(stack -> Math.max(0L, stack.expirationTick() - currentTick))
                .max().orElse(0L);
    }
}
