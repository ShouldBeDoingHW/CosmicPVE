package com.cosmicpve.combat.stack;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;

/** Entity-owned stack state. Mutation methods are package-private and used only by CombatStackService. */
public final class CombatStackContainer {
    public static final MapCodec<CombatStackContainer> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CombatStackInstance.CODEC.listOf().optionalFieldOf("persistent_stacks", List.of())
                    .forGetter(CombatStackContainer::persistentInstances)
    ).apply(instance, CombatStackContainer::new));

    private final Map<Identifier, List<CombatStackInstance>> active = new HashMap<>();
    private long nextExpirationTick = Long.MAX_VALUE;

    public CombatStackContainer() {}

    public CombatStackContainer(List<CombatStackInstance> serializedInstances) {
        for (var stack : List.copyOf(serializedInstances)) {
            if (stack.scope() == CombatStackScope.PERSISTENT_ENTITY) {
                addInternal(stack);
            }
        }
    }

    public boolean isEmpty() {
        return active.isEmpty();
    }

    public int count(Identifier definitionId) {
        var instances = active.get(definitionId);
        return instances == null ? 0 : instances.size();
    }

    public int totalCount() {
        return active.values().stream().mapToInt(List::size).sum();
    }

    public long nextExpirationTick() {
        return nextExpirationTick;
    }

    public boolean isExpirationDue(long currentTick) {
        return nextExpirationTick <= currentTick;
    }

    public Map<Identifier, List<CombatStackInstance>> snapshot() {
        var snapshot = new LinkedHashMap<Identifier, List<CombatStackInstance>>();
        active.keySet().stream().sorted(java.util.Comparator.comparing(Identifier::toString)).forEach(id -> {
            var sorted = new ArrayList<>(active.get(id));
            sorted.sort(CombatStackInstance.EXPIRATION_ORDER);
            snapshot.put(id, List.copyOf(sorted));
        });
        return Map.copyOf(snapshot);
    }

    List<CombatStackInstance> instances(Identifier definitionId) {
        return active.computeIfAbsent(definitionId, ignored -> new ArrayList<>());
    }

    List<CombatStackInstance> existingInstances(Identifier definitionId) {
        return active.getOrDefault(definitionId, List.of());
    }

    void addInternal(CombatStackInstance stack) {
        instances(stack.definitionId()).add(stack);
        nextExpirationTick = Math.min(nextExpirationTick, stack.expirationTick());
    }

    boolean removeInternal(CombatStackInstance stack) {
        var instances = active.get(stack.definitionId());
        if (instances == null || !instances.remove(stack)) {
            return false;
        }
        removeEmptyGroup(stack.definitionId());
        recalculateNextExpiration();
        return true;
    }

    void replaceInternal(Identifier definitionId, List<CombatStackInstance> replacements) {
        if (replacements.isEmpty()) {
            active.remove(definitionId);
        } else {
            active.put(definitionId, new ArrayList<>(replacements));
        }
        recalculateNextExpiration();
    }

    int clearMatching(java.util.function.Predicate<CombatStackInstance> predicate) {
        int before = totalCount();
        active.values().forEach(instances -> instances.removeIf(predicate));
        active.keySet().removeIf(id -> active.get(id).isEmpty());
        recalculateNextExpiration();
        return before - totalCount();
    }

    CombatStackContainer copy(boolean persistentOnly) {
        var copied = new CombatStackContainer();
        active.values().stream().flatMap(List::stream)
                .filter(stack -> !persistentOnly || stack.scope() == CombatStackScope.PERSISTENT_ENTITY)
                .forEach(copied::addInternal);
        return copied;
    }

    private List<CombatStackInstance> persistentInstances() {
        return active.values().stream().flatMap(List::stream)
                .filter(stack -> stack.scope() == CombatStackScope.PERSISTENT_ENTITY)
                .sorted(ComparatorHolder.SERIALIZATION_ORDER)
                .toList();
    }

    private void removeEmptyGroup(Identifier definitionId) {
        var instances = active.get(definitionId);
        if (instances != null && instances.isEmpty()) {
            active.remove(definitionId);
        }
    }

    private void recalculateNextExpiration() {
        nextExpirationTick = active.values().stream().flatMap(List::stream)
                .mapToLong(CombatStackInstance::expirationTick).min().orElse(Long.MAX_VALUE);
    }

    private static final class ComparatorHolder {
        private static final java.util.Comparator<CombatStackInstance> SERIALIZATION_ORDER =
                java.util.Comparator.comparing((CombatStackInstance stack) -> stack.definitionId().toString())
                        .thenComparing(CombatStackInstance.EXPIRATION_ORDER);
    }
}
