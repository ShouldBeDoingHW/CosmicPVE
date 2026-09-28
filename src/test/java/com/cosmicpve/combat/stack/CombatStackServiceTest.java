package com.cosmicpve.combat.stack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.content.ContentSnapshot;
import com.cosmicpve.content.CosmicContentRepository;
import com.cosmicpve.content.definition.stack.StackDefinition;
import com.cosmicpve.content.definition.stack.StackPolarity;
import com.cosmicpve.content.definition.stack.StackRefreshPolicy;
import com.cosmicpve.content.validation.ValidationResult;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class CombatStackServiceTest {
    private static final Identifier INDEPENDENT = CosmicPVE.id("test/independent");
    private static final Identifier REFRESH_ALL = CosmicPVE.id("test/refresh_all");
    private static final Identifier REFRESH_ONE = CosmicPVE.id("test/refresh_one");
    private static final Identifier FIXED = CosmicPVE.id("test/fixed");
    private static final Identifier NEGATIVE = CosmicPVE.id("test/negative");
    private static final Identifier NON_CLEANSABLE = CosmicPVE.id("test/non_cleansable");
    private static final Identifier NON_TRANSFERABLE = CosmicPVE.id("test/non_transferable");
    private static final Identifier PERSISTENT = CosmicPVE.id("test/persistent");
    private static final Identifier BLEED = CosmicPVE.id("bleed");
    private static final Identifier PACIFY = CosmicPVE.id("pacify");

    @Test void pacifyReapplicationReplacesLevelAndSourceWithoutStacking() {
        var fixture = fixture();
        var target = new CombatStackContainer();
        UUID firstSource = UUID.randomUUID();
        UUID secondSource = UUID.randomUUID();
        fixture.service.addStack(target, PACIFY, 1,
                StackApplication.ephemeral(Optional.of(firstSource), Optional.of(firstSource)).withPotency(1), 10);
        fixture.service.addStack(target, PACIFY, 1,
                StackApplication.ephemeral(Optional.of(secondSource), Optional.of(secondSource)).withPotency(4), 30);
        assertEquals(1, target.count(PACIFY));
        var active = target.snapshot().get(PACIFY).getFirst();
        assertEquals(4, active.potency());
        assertEquals(Optional.of(secondSource), active.originalSourceEntityId());
        assertEquals(90L, active.expirationTick());
        assertEquals(0, fixture.service.count(target, PACIFY, 90));
    }

    @Test
    void bleedKeepsTenIndependentAttributedUnitsWithExactLifetimes() {
        var fixture = fixture();
        var container = new CombatStackContainer();
        UUID source = UUID.randomUUID();
        UUID credited = UUID.randomUUID();
        var application = StackApplication.ephemeral(Optional.of(source), Optional.of(credited));

        var result = fixture.service.addStack(container, BLEED, 12, application, 20);

        assertEquals(10, result.added());
        assertEquals(StackMutationStatus.APPLIED, result.status());
        assertEquals(10, container.count(BLEED));
        assertTrue(container.snapshot().get(BLEED).stream().allMatch(stack ->
                stack.applicationTick() == 20
                        && stack.expirationTick() == 120
                        && stack.originalSourceEntityId().equals(Optional.of(source))
                        && stack.creditedPlayerId().equals(Optional.of(credited))));
        assertEquals(10, fixture.service.expireDue(container, 120).expired());
    }

    @Test
    void addsStacksAndEnforcesMaximum() {
        var fixture = fixture();
        var container = new CombatStackContainer();

        var result = fixture.service.addStack(
                container, INDEPENDENT, 5, StackApplication.unattributed(), 0);

        assertEquals(3, result.added());
        assertEquals(3, result.finalCount());
        assertEquals(StackMutationStatus.APPLIED, result.status());
        assertEquals(StackMutationStatus.AT_MAXIMUM, fixture.service.addStack(
                container, INDEPENDENT, 1, StackApplication.unattributed(), 1).status());
    }

    @Test
    void independentStacksExpireSeparatelyAtTheirExactTicks() {
        var fixture = fixture();
        var container = new CombatStackContainer();
        fixture.service.addStack(container, INDEPENDENT, 1, StackApplication.unattributed(), 0);
        fixture.service.addStack(container, INDEPENDENT, 1, StackApplication.unattributed(), 5);

        assertEquals(List.of(10L, 15L), expiries(container, INDEPENDENT));
        assertEquals(1, fixture.service.expireDue(container, 10).expired());
        assertEquals(1, fixture.service.count(container, INDEPENDENT, 10));
        assertEquals(1, fixture.service.expireDue(container, 15).expired());
        assertTrue(container.isEmpty());
    }

    @Test
    void refreshAllRenewsEveryExistingStack() {
        var fixture = fixture();
        var container = new CombatStackContainer();
        fixture.service.addStack(container, REFRESH_ALL, 2, StackApplication.unattributed(), 0);

        var refreshed = fixture.service.addStack(container, REFRESH_ALL, 1, StackApplication.unattributed(), 5);

        assertEquals(1, refreshed.added());
        assertEquals(2, refreshed.refreshed());
        assertEquals(List.of(15L, 15L, 15L), expiries(container, REFRESH_ALL));
        assertEquals(3, fixture.service.count(container, REFRESH_ALL, 10));
    }

    @Test
    void refreshOneRenewsEarliestExpiringStackWhenFull() {
        var fixture = fixture();
        var container = new CombatStackContainer();
        fixture.service.addStack(container, REFRESH_ONE, 1, StackApplication.unattributed(), 0);
        fixture.service.addStack(container, REFRESH_ONE, 1, StackApplication.unattributed(), 2);

        var refreshed = fixture.service.addStack(container, REFRESH_ONE, 1, StackApplication.unattributed(), 5);

        assertEquals(0, refreshed.added());
        assertEquals(1, refreshed.refreshed());
        assertEquals(List.of(12L, 15L), expiries(container, REFRESH_ONE));
    }

    @Test
    void fixedStacksShareFirstLifetimeAndCannotExtendIt() {
        var fixture = fixture();
        var container = new CombatStackContainer();
        fixture.service.addStack(container, FIXED, 1, StackApplication.unattributed(), 0);
        fixture.service.addStack(container, FIXED, 2, StackApplication.unattributed(), 5);
        var fullAttempt = fixture.service.addStack(container, FIXED, 1, StackApplication.unattributed(), 7);

        assertEquals(List.of(10L, 10L, 10L), expiries(container, FIXED));
        assertEquals(StackMutationStatus.AT_MAXIMUM, fullAttempt.status());
        assertEquals(3, fixture.service.expireDue(container, 10).expired());
    }

    @Test
    void polarityQueriesAndCleanseRespectCleansableFlag() {
        var fixture = fixture();
        var container = new CombatStackContainer();
        fixture.service.addStack(container, NEGATIVE, 2, StackApplication.unattributed(), 0);
        fixture.service.addStack(container, NON_CLEANSABLE, 1, StackApplication.unattributed(), 0);
        fixture.service.addStack(container, REFRESH_ALL, 1, StackApplication.unattributed(), 0);

        assertEquals(2, fixture.service.byPolarity(container, StackPolarity.NEGATIVE, 0).size());
        assertEquals(2, fixture.service.cleanseAll(container, StackPolarity.NEGATIVE, 0));
        assertEquals(0, container.count(NEGATIVE));
        assertEquals(1, container.count(NON_CLEANSABLE));
        assertEquals(1, container.count(REFRESH_ALL));
    }

    @Test
    void stealMovesExactlyOneTransferablePositiveAndPreservesProvenanceAndDuration() {
        var fixture = fixture();
        var source = new CombatStackContainer();
        var recipient = new CombatStackContainer();
        UUID originalSource = UUID.randomUUID();
        UUID creditedPlayer = UUID.randomUUID();
        UUID transferActor = UUID.randomUUID();
        var application = StackApplication.ephemeral(Optional.of(originalSource), Optional.of(creditedPlayer));
        fixture.service.addStack(source, REFRESH_ALL, 2, application, 0);
        var before = source.snapshot().get(REFRESH_ALL).getFirst();

        var result = fixture.service.stealOne(source, recipient, Optional.of(transferActor), 4);

        assertEquals(StackMutationStatus.APPLIED, result.status());
        assertEquals(1, source.count(REFRESH_ALL));
        assertEquals(1, recipient.count(REFRESH_ALL));
        var transferred = result.transferred().orElseThrow();
        assertEquals(before.instanceId(), transferred.instanceId());
        assertEquals(before.applicationTick(), transferred.applicationTick());
        assertEquals(before.expirationTick(), transferred.expirationTick());
        assertEquals(Optional.of(originalSource), transferred.originalSourceEntityId());
        assertEquals(Optional.of(creditedPlayer), transferred.creditedPlayerId());
        assertEquals(Optional.of(transferActor), transferred.lastTransferredBy());
        assertEquals(Optional.of(4L), transferred.lastTransferTick());
    }

    @Test
    void transferSkipsNonTransferableAndStopsWhenRecipientIsAtMaximum() {
        var fixture = fixture();
        var source = new CombatStackContainer();
        var recipient = new CombatStackContainer();
        fixture.service.addStack(source, NON_TRANSFERABLE, 1, StackApplication.unattributed(), 0);
        assertEquals(StackMutationStatus.NO_ELIGIBLE_STACK,
                fixture.service.stealOne(source, recipient, Optional.empty(), 1).status());

        fixture.service.addStack(source, REFRESH_ALL, 1, StackApplication.unattributed(), 0);
        fixture.service.addStack(recipient, REFRESH_ALL, 3, StackApplication.unattributed(), 0);
        assertEquals(StackMutationStatus.RECIPIENT_AT_MAXIMUM,
                fixture.service.stealOne(source, recipient, Optional.empty(), 1).status());
        assertEquals(1, source.count(REFRESH_ALL));
    }

    @Test
    void unknownDefinitionFailsSafelyAndExistingUnknownStateStillExpires() {
        var fixture = fixture();
        var container = new CombatStackContainer();
        Identifier missing = CosmicPVE.id("test/missing");
        assertEquals(StackMutationStatus.UNKNOWN_DEFINITION, fixture.service.addStack(
                container, missing, 1, StackApplication.unattributed(), 0).status());
        assertTrue(container.isEmpty());

        var unknown = new CombatStackInstance(
                UUID.randomUUID(), missing, 3, Optional.empty(), Optional.empty(),
                0, 0, 10, CombatStackScope.EPHEMERAL_COMBAT, Optional.empty(), Optional.empty(), Optional.empty());
        var loaded = new CombatStackContainer();
        // Simulate state whose datapack definition was removed after it had been applied.
        loaded.addInternal(unknown);
        assertTrue(fixture.service.activeStacks(loaded, 0).getFirst().definition().isEmpty());
        assertEquals(1, fixture.service.expireDue(loaded, 10).expired());
    }

    @Test
    void entityContainersAreIsolatedAndEmptyExpiryUsesCheapPath() {
        var fixture = fixture();
        var first = new CombatStackContainer();
        var second = new CombatStackContainer();
        fixture.service.addStack(first, INDEPENDENT, 1, StackApplication.unattributed(), 0);

        assertEquals(1, first.count(INDEPENDENT));
        assertEquals(0, second.count(INDEPENDENT));
        var emptyTick = fixture.service.expireDue(second, 1_000);
        assertEquals(0, emptyTick.expired());
        assertFalse(emptyTick.scanned());
        assertEquals(Long.MAX_VALUE, second.nextExpirationTick());
    }

    @Test
    void removeCountsAndScopeClearingAreCentralized() {
        var fixture = fixture();
        var container = new CombatStackContainer();
        var session = CosmicPVE.id("test/session");
        fixture.service.addStack(container, INDEPENDENT, 1, StackApplication.unattributed(), 0);
        fixture.service.addStack(
                container, INDEPENDENT, 2,
                new StackApplication(Optional.empty(), Optional.empty(),
                        CombatStackScope.INSTANCE_SESSION, Optional.of(session)),
                1);

        assertEquals(1, fixture.service.remove(container, INDEPENDENT, 1, 1).removed());
        assertEquals(2, fixture.service.clearScope(
                container, CombatStackScope.INSTANCE_SESSION, Optional.of(session), 1));
        assertTrue(container.isEmpty());
    }

    @Test
    void persistentDefinitionForcesPersistentScopeAndDeathCloneFiltersEphemeralState() {
        var fixture = fixture();
        var original = new CombatStackContainer();
        fixture.service.addStack(original, PERSISTENT, 1, StackApplication.unattributed(), 0);
        fixture.service.addStack(original, REFRESH_ALL, 1, StackApplication.unattributed(), 0);

        var deathClone = fixture.service.copyForClone(original, true);
        var dimensionClone = fixture.service.copyForClone(original, false);

        assertEquals(1, deathClone.totalCount());
        assertEquals(1, deathClone.count(PERSISTENT));
        assertEquals(2, dimensionClone.totalCount());
        assertEquals(CombatStackScope.PERSISTENT_ENTITY,
                deathClone.snapshot().get(PERSISTENT).getFirst().scope());
    }

    private static List<Long> expiries(CombatStackContainer container, Identifier id) {
        return container.snapshot().get(id).stream().map(CombatStackInstance::expirationTick).sorted().toList();
    }

    private static Fixture fixture() {
        var definitions = Map.of(
                INDEPENDENT, definition(INDEPENDENT, StackPolarity.POSITIVE, 3, 10, StackRefreshPolicy.INDEPENDENT, true, true, false),
                REFRESH_ALL, definition(REFRESH_ALL, StackPolarity.POSITIVE, 3, 10, StackRefreshPolicy.REFRESH_ALL, true, true, false),
                REFRESH_ONE, definition(REFRESH_ONE, StackPolarity.POSITIVE, 2, 10, StackRefreshPolicy.REFRESH_ONE, true, true, false),
                FIXED, definition(FIXED, StackPolarity.POSITIVE, 3, 10, StackRefreshPolicy.FIXED, true, true, false),
                NEGATIVE, definition(NEGATIVE, StackPolarity.NEGATIVE, 3, 10, StackRefreshPolicy.INDEPENDENT, false, true, false),
                NON_CLEANSABLE, definition(NON_CLEANSABLE, StackPolarity.NEGATIVE, 2, 10, StackRefreshPolicy.REFRESH_ALL, false, false, false),
                NON_TRANSFERABLE, definition(NON_TRANSFERABLE, StackPolarity.POSITIVE, 2, 10, StackRefreshPolicy.REFRESH_ALL, false, true, false),
                PERSISTENT, definition(PERSISTENT, StackPolarity.POSITIVE, 2, 10, StackRefreshPolicy.REFRESH_ALL, true, true, true));
        definitions = new java.util.HashMap<>(definitions);
        definitions.put(BLEED, definition(
                BLEED, StackPolarity.NEGATIVE, 10, 100, StackRefreshPolicy.INDEPENDENT, false, true, false));
        definitions.put(PACIFY, definition(
                PACIFY, StackPolarity.NEGATIVE, 1, 60, StackRefreshPolicy.REFRESH_ALL, false, true, false));
        var repository = new CosmicContentRepository();
        assertTrue(repository.publish(ValidationResult.success(new ContentSnapshot(0, Map.of(), definitions))));
        return new Fixture(new CombatStackService(repository));
    }

    private static StackDefinition definition(
            Identifier id,
            StackPolarity polarity,
            int maximum,
            int duration,
            StackRefreshPolicy refresh,
            boolean transferable,
            boolean cleansable,
            boolean persistent) {
        return new StackDefinition(id, polarity, maximum, duration, refresh, transferable, cleansable, persistent);
    }

    private record Fixture(CombatStackService service) {}
}
