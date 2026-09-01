package com.cosmicpve.combat.stack;

import com.cosmicpve.content.ContentSnapshot;
import com.cosmicpve.content.CosmicContentRepository;
import com.cosmicpve.content.definition.stack.StackDefinition;
import com.cosmicpve.content.definition.stack.StackPolarity;
import com.cosmicpve.content.definition.stack.StackRefreshPolicy;
import com.cosmicpve.registry.ModAttachments;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/** The sole mutation boundary for LivingEntity combat stacks. It performs no proc or gameplay-effect logic. */
public final class CombatStackService {
    private static final Comparator<CombatStackInstance> INSTANCE_ORDER = CombatStackInstance.EXPIRATION_ORDER;
    private final CosmicContentRepository content;

    public CombatStackService(CosmicContentRepository content) {
        this.content = content;
    }

    public StackAddResult addStack(
            LivingEntity target,
            Identifier definitionId,
            int count,
            StackApplication application,
            long currentTick) {
        return addStack(target, definitionId, count, application, currentTick, 0);
    }

    public StackAddResult addStack(
            LivingEntity target, Identifier definitionId, int count, StackApplication application,
            long currentTick, int durationOverrideTicks) {
        requireServer(target);
        var container = target.getData(ModAttachments.COMBAT_STACKS);
        var result = addStack(container, definitionId, count, application, currentTick, durationOverrideTicks);
        removeAttachmentIfEmpty(target, container);
        return result;
    }

    public StackAddResult addStack(
            CombatStackContainer container,
            Identifier definitionId,
            int count,
            StackApplication application,
            long currentTick) {
        return addStack(container, definitionId, count, application, currentTick, 0);
    }

    public StackAddResult addStack(
            CombatStackContainer container, Identifier definitionId, int count, StackApplication application,
            long currentTick, int durationOverrideTicks) {
        if (count < 1) {
            throw new IllegalArgumentException("Stack add count must be positive");
        }
        expireDue(container, currentTick);
        var resolved = resolve(definitionId);
        if (resolved.isEmpty()) {
            return new StackAddResult(
                    definitionId, count, 0, 0, container.count(definitionId), StackMutationStatus.UNKNOWN_DEFINITION);
        }
        var value = resolved.orElseThrow();
        var definition = value.definition();
        int durationTicks = durationOverrideTicks > 0 ? durationOverrideTicks : definition.durationTicks();
        int added = 0;
        int refreshed = 0;
        for (int index = 0; index < count; index++) {
            switch (definition.refreshPolicy()) {
                case INDEPENDENT -> {
                    if (container.count(definitionId) < definition.maximumStacks()) {
                        container.addInternal(newInstance(value, application, currentTick, expiration(currentTick, durationTicks)));
                        added++;
                    }
                }
                case REFRESH_ALL -> {
                    long expiry = expiration(currentTick, durationTicks);
                    var existing = new ArrayList<>(container.existingInstances(definitionId));
                    if (!existing.isEmpty()) {
                        existing.replaceAll(stack -> stack.refreshed(value.revision(), currentTick, expiry));
                        container.replaceInternal(definitionId, existing);
                        refreshed += existing.size();
                    }
                    if (container.count(definitionId) < definition.maximumStacks()) {
                        container.addInternal(newInstance(value, application, currentTick, expiry));
                        added++;
                    }
                }
                case REFRESH_ONE -> {
                    if (container.count(definitionId) < definition.maximumStacks()) {
                        container.addInternal(newInstance(
                                value, application, currentTick, expiration(currentTick, durationTicks)));
                        added++;
                    } else {
                        var existing = new ArrayList<>(container.existingInstances(definitionId));
                        var selected = existing.stream().min(INSTANCE_ORDER).orElseThrow();
                        existing.set(existing.indexOf(selected), selected.refreshed(
                                value.revision(), currentTick, expiration(currentTick, durationTicks)));
                        container.replaceInternal(definitionId, existing);
                        refreshed++;
                    }
                }
                case FIXED -> {
                    if (container.count(definitionId) < definition.maximumStacks()) {
                        long fixedExpiry = container.existingInstances(definitionId).stream()
                                .mapToLong(CombatStackInstance::expirationTick).min()
                                .orElseGet(() -> expiration(currentTick, durationTicks));
                        container.addInternal(newInstance(value, application, currentTick, fixedExpiry));
                        added++;
                    }
                }
            }
        }
        var status = added > 0 || refreshed > 0 ? StackMutationStatus.APPLIED : StackMutationStatus.AT_MAXIMUM;
        return new StackAddResult(definitionId, count, added, refreshed, container.count(definitionId), status);
    }

    public StackRemovalResult removeOne(LivingEntity target, Identifier definitionId, long currentTick) {
        return remove(target, definitionId, 1, currentTick);
    }

    public StackRemovalResult remove(LivingEntity target, Identifier definitionId, int count, long currentTick) {
        requireServer(target);
        var container = target.getExistingDataOrNull(ModAttachments.COMBAT_STACKS);
        if (container == null) {
            return new StackRemovalResult(definitionId, count, 0, 0, StackMutationStatus.NOT_FOUND);
        }
        var result = remove(container, definitionId, count, currentTick);
        removeAttachmentIfEmpty(target, container);
        return result;
    }

    public StackRemovalResult remove(
            CombatStackContainer container, Identifier definitionId, int count, long currentTick) {
        if (count < 1) {
            throw new IllegalArgumentException("Stack remove count must be positive");
        }
        expireDue(container, currentTick);
        int removed = 0;
        while (removed < count) {
            var selected = container.existingInstances(definitionId).stream().min(INSTANCE_ORDER);
            if (selected.isEmpty() || !container.removeInternal(selected.orElseThrow())) {
                break;
            }
            removed++;
        }
        return new StackRemovalResult(
                definitionId, count, removed, container.count(definitionId),
                removed > 0 ? StackMutationStatus.APPLIED : StackMutationStatus.NOT_FOUND);
    }

    public StackRemovalResult removeAll(LivingEntity target, Identifier definitionId, long currentTick) {
        requireServer(target);
        var container = target.getExistingDataOrNull(ModAttachments.COMBAT_STACKS);
        if (container == null) {
            return new StackRemovalResult(definitionId, 0, 0, 0, StackMutationStatus.NOT_FOUND);
        }
        var result = removeAll(container, definitionId, currentTick);
        removeAttachmentIfEmpty(target, container);
        return result;
    }

    public StackRemovalResult removeAll(
            CombatStackContainer container, Identifier definitionId, long currentTick) {
        expireDue(container, currentTick);
        int count = container.count(definitionId);
        int removed = container.clearMatching(stack -> stack.definitionId().equals(definitionId));
        return new StackRemovalResult(
                definitionId, count, removed, 0,
                removed > 0 ? StackMutationStatus.APPLIED : StackMutationStatus.NOT_FOUND);
    }

    public int count(LivingEntity target, Identifier definitionId, long currentTick) {
        var container = target.getExistingDataOrNull(ModAttachments.COMBAT_STACKS);
        return container == null ? 0 : count(container, definitionId, currentTick);
    }

    public int count(CombatStackContainer container, Identifier definitionId, long currentTick) {
        expireDue(container, currentTick);
        return container.count(definitionId);
    }

    public List<ActiveCombatStack> activeStacks(LivingEntity target, long currentTick) {
        var container = target.getExistingDataOrNull(ModAttachments.COMBAT_STACKS);
        return container == null ? List.of() : activeStacks(container, currentTick);
    }

    public List<ActiveCombatStack> activeStacks(CombatStackContainer container, long currentTick) {
        expireDue(container, currentTick);
        ContentSnapshot snapshot = content.snapshot();
        return container.snapshot().entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey(Comparator.comparing(Identifier::toString)))
                .map(entry -> new ActiveCombatStack(
                        entry.getKey(), Optional.ofNullable(snapshot.stackDefinitions().get(entry.getKey())),
                        entry.getValue().stream().mapToLong(CombatStackInstance::definitionRevision).min().orElse(0L),
                        entry.getValue()))
                .toList();
    }

    public List<ActiveCombatStack> byPolarity(
            CombatStackContainer container, StackPolarity polarity, long currentTick) {
        return activeStacks(container, currentTick).stream()
                .filter(stack -> stack.definition().map(definition -> definition.polarity() == polarity).orElse(false))
                .toList();
    }

    public List<ActiveCombatStack> byPolarity(
            LivingEntity target, StackPolarity polarity, long currentTick) {
        var container = target.getExistingDataOrNull(ModAttachments.COMBAT_STACKS);
        return container == null ? List.of() : byPolarity(container, polarity, currentTick);
    }

    public StackExpirationResult expireDue(LivingEntity target, long currentTick) {
        var container = target.getExistingDataOrNull(ModAttachments.COMBAT_STACKS);
        return container == null ? new StackExpirationResult(0, false) : expireDue(container, currentTick);
    }

    public StackExpirationResult expireDue(CombatStackContainer container, long currentTick) {
        if (!container.isExpirationDue(currentTick)) {
            return new StackExpirationResult(0, false);
        }
        int expired = container.clearMatching(stack -> stack.expirationTick() <= currentTick);
        return new StackExpirationResult(expired, true);
    }

    public int clearScope(LivingEntity target, CombatStackScope scope, Optional<Identifier> scopeId, long currentTick) {
        var container = target.getExistingDataOrNull(ModAttachments.COMBAT_STACKS);
        if (container == null) {
            return 0;
        }
        int removed = clearScope(container, scope, scopeId, currentTick);
        removeAttachmentIfEmpty(target, container);
        return removed;
    }

    public int clearScope(
            CombatStackContainer container, CombatStackScope scope, Optional<Identifier> scopeId, long currentTick) {
        expireDue(container, currentTick);
        return container.clearMatching(stack -> stack.scope() == scope && stack.scopeId().equals(scopeId));
    }

    public int cleanseOne(CombatStackContainer container, StackPolarity polarity, long currentTick) {
        expireDue(container, currentTick);
        var selected = eligibleDefinitions(container, content.snapshot(), polarity, true, false).stream().findFirst();
        if (selected.isEmpty()) {
            return 0;
        }
        return remove(container, selected.orElseThrow(), 1, currentTick).removed();
    }

    public int cleanseOne(LivingEntity target, StackPolarity polarity, long currentTick) {
        requireServer(target);
        var container = target.getExistingDataOrNull(ModAttachments.COMBAT_STACKS);
        if (container == null) {
            return 0;
        }
        int removed = cleanseOne(container, polarity, currentTick);
        removeAttachmentIfEmpty(target, container);
        return removed;
    }

    public int cleanseAll(LivingEntity target, StackPolarity polarity, long currentTick) {
        requireServer(target);
        var container = target.getExistingDataOrNull(ModAttachments.COMBAT_STACKS);
        if (container == null) {
            return 0;
        }
        int removed = cleanseAll(container, polarity, currentTick);
        removeAttachmentIfEmpty(target, container);
        return removed;
    }

    public int cleanseAll(CombatStackContainer container, StackPolarity polarity, long currentTick) {
        expireDue(container, currentTick);
        var eligible = eligibleDefinitions(container, content.snapshot(), polarity, true, false);
        return container.clearMatching(stack -> eligible.contains(stack.definitionId()));
    }

    public StackTransferResult stealOne(
            LivingEntity source,
            LivingEntity recipient,
            Optional<UUID> transferActorId,
            long currentTick) {
        requireServer(source);
        requireServer(recipient);
        var sourceContainer = source.getExistingDataOrNull(ModAttachments.COMBAT_STACKS);
        if (sourceContainer == null) {
            return new StackTransferResult(StackMutationStatus.NO_ELIGIBLE_STACK, Optional.empty(), 0, 0);
        }
        if (source == recipient) {
            return new StackTransferResult(
                    StackMutationStatus.NO_ELIGIBLE_STACK, Optional.empty(),
                    sourceContainer.totalCount(), sourceContainer.totalCount());
        }
        var recipientContainer = recipient.getData(ModAttachments.COMBAT_STACKS);
        var result = stealOne(sourceContainer, recipientContainer, transferActorId, currentTick);
        removeAttachmentIfEmpty(source, sourceContainer);
        removeAttachmentIfEmpty(recipient, recipientContainer);
        return result;
    }

    /** Read-only eligibility check used to keep impossible steal candidates out of the proc RNG stream. */
    public boolean canStealOne(LivingEntity source, LivingEntity recipient, long currentTick) {
        requireServer(source);
        requireServer(recipient);
        if (source == recipient) return false;
        var sourceContainer = source.getExistingDataOrNull(ModAttachments.COMBAT_STACKS);
        if (sourceContainer == null) return false;
        expireDue(sourceContainer, currentTick);
        var recipientContainer = recipient.getExistingDataOrNull(ModAttachments.COMBAT_STACKS);
        if (recipientContainer != null) expireDue(recipientContainer, currentTick);
        removeAttachmentIfEmpty(source, sourceContainer);
        if (recipientContainer != null) removeAttachmentIfEmpty(recipient, recipientContainer);
        if (sourceContainer.isEmpty()) return false;
        ContentSnapshot snapshot = content.snapshot();
        for (Identifier id : eligibleDefinitions(
                sourceContainer, snapshot, StackPolarity.POSITIVE, false, true)) {
            StackDefinition definition = snapshot.stackDefinitions().get(id);
            int recipientCount = recipientContainer == null ? 0 : recipientContainer.count(id);
            if (definition != null && recipientCount < definition.maximumStacks()) return true;
        }
        return false;
    }

    public StackTransferResult transferOne(
            LivingEntity source,
            LivingEntity recipient,
            Identifier definitionId,
            Optional<UUID> transferActorId,
            long currentTick) {
        requireServer(source);
        requireServer(recipient);
        var sourceContainer = source.getExistingDataOrNull(ModAttachments.COMBAT_STACKS);
        if (sourceContainer == null || source == recipient) {
            return new StackTransferResult(StackMutationStatus.NO_ELIGIBLE_STACK, Optional.empty(), 0, 0);
        }
        var recipientContainer = recipient.getData(ModAttachments.COMBAT_STACKS);
        var result = transferOne(sourceContainer, recipientContainer, definitionId, transferActorId, currentTick);
        removeAttachmentIfEmpty(source, sourceContainer);
        removeAttachmentIfEmpty(recipient, recipientContainer);
        return result;
    }

    public StackTransferResult transferOne(
            CombatStackContainer source,
            CombatStackContainer recipient,
            Identifier definitionId,
            Optional<UUID> transferActorId,
            long currentTick) {
        expireDue(source, currentTick);
        expireDue(recipient, currentTick);
        StackDefinition definition = content.snapshot().stackDefinitions().get(definitionId);
        if (definition == null) {
            return new StackTransferResult(
                    StackMutationStatus.UNKNOWN_DEFINITION, Optional.empty(),
                    source.totalCount(), recipient.totalCount());
        }
        if (!definition.transferable() || source.count(definitionId) == 0) {
            return new StackTransferResult(
                    StackMutationStatus.NO_ELIGIBLE_STACK, Optional.empty(),
                    source.totalCount(), recipient.totalCount());
        }
        if (recipient.count(definitionId) >= definition.maximumStacks()) {
            return new StackTransferResult(
                    StackMutationStatus.RECIPIENT_AT_MAXIMUM, Optional.empty(),
                    source.totalCount(), recipient.totalCount());
        }
        var selected = source.existingInstances(definitionId).stream().min(INSTANCE_ORDER).orElseThrow();
        return transferSelected(source, recipient, definition, selected, transferActorId, currentTick);
    }

    public StackTransferResult stealOne(
            CombatStackContainer source,
            CombatStackContainer recipient,
            Optional<UUID> transferActorId,
            long currentTick) {
        expireDue(source, currentTick);
        expireDue(recipient, currentTick);
        ContentSnapshot snapshot = content.snapshot();
        boolean hadTransferable = false;
        for (Identifier id : eligibleDefinitions(source, snapshot, StackPolarity.POSITIVE, false, true)) {
            StackDefinition definition = snapshot.stackDefinitions().get(id);
            if (definition == null) {
                continue;
            }
            hadTransferable = true;
            if (recipient.count(id) >= definition.maximumStacks()) {
                continue;
            }
            var selected = source.existingInstances(id).stream().min(INSTANCE_ORDER).orElseThrow();
            return transferSelected(source, recipient, definition, selected, transferActorId, currentTick);
        }
        return new StackTransferResult(
                hadTransferable ? StackMutationStatus.RECIPIENT_AT_MAXIMUM : StackMutationStatus.NO_ELIGIBLE_STACK,
                Optional.empty(), source.totalCount(), recipient.totalCount());
    }

    public CombatStackContainer copyForClone(CombatStackContainer original, boolean deathClone) {
        return original.copy(deathClone);
    }

    private StackTransferResult transferSelected(
            CombatStackContainer source,
            CombatStackContainer recipient,
            StackDefinition definition,
            CombatStackInstance selected,
            Optional<UUID> transferActorId,
            long currentTick) {
        long preservedExpiry = selected.expirationTick();
        if (definition.refreshPolicy() == StackRefreshPolicy.FIXED && recipient.count(definition.id()) > 0) {
            preservedExpiry = Math.min(
                    preservedExpiry,
                    recipient.existingInstances(definition.id()).stream()
                            .mapToLong(CombatStackInstance::expirationTick).min().orElse(preservedExpiry));
        }
        source.removeInternal(selected);
        var transferred = selected.transferred(transferActorId, currentTick, preservedExpiry);
        recipient.addInternal(transferred);
        return new StackTransferResult(
                StackMutationStatus.APPLIED, Optional.of(transferred), source.totalCount(), recipient.totalCount());
    }

    private List<Identifier> eligibleDefinitions(
            CombatStackContainer container,
            ContentSnapshot snapshot,
            StackPolarity polarity,
            boolean requireCleansable,
            boolean requireTransferable) {
        return container.snapshot().keySet().stream()
                .filter(id -> {
                    StackDefinition definition = snapshot.stackDefinitions().get(id);
                    return definition != null
                            && definition.polarity() == polarity
                            && (!requireCleansable || definition.cleansable())
                            && (!requireTransferable || definition.transferable());
                })
                .sorted(Comparator.comparing(Identifier::toString))
                .toList();
    }

    private Optional<ResolvedDefinition> resolve(Identifier definitionId) {
        ContentSnapshot snapshot = content.snapshot();
        StackDefinition definition = snapshot.stackDefinitions().get(definitionId);
        return definition == null
                ? Optional.empty()
                : Optional.of(new ResolvedDefinition(snapshot.revision(), definition));
    }

    private static CombatStackInstance newInstance(
            ResolvedDefinition resolved,
            StackApplication application,
            long currentTick,
            long expirationTick) {
        CombatStackScope scope = resolved.definition().persistent()
                ? CombatStackScope.PERSISTENT_ENTITY
                : application.scope() == CombatStackScope.PERSISTENT_ENTITY
                        ? CombatStackScope.EPHEMERAL_COMBAT : application.scope();
        Optional<Identifier> scopeId = scope == CombatStackScope.INSTANCE_SESSION
                ? application.scopeId() : Optional.empty();
        return new CombatStackInstance(
                UUID.randomUUID(), resolved.definition().id(), resolved.revision(), application.sourceEntityId(),
                application.creditedPlayerId(), currentTick, currentTick, expirationTick,
                scope, scopeId, Optional.empty(), Optional.empty());
    }

    private static long expiration(long currentTick, int durationTicks) {
        try {
            return Math.addExact(currentTick, durationTicks);
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }

    private static void requireServer(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            throw new IllegalStateException("Combat stacks are server-authoritative");
        }
    }

    private static void removeAttachmentIfEmpty(LivingEntity entity, CombatStackContainer container) {
        if (container.isEmpty()) {
            entity.removeData(ModAttachments.COMBAT_STACKS);
        }
    }

    private record ResolvedDefinition(long revision, StackDefinition definition) {}
}
