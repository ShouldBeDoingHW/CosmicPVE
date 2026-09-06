package com.cosmicpve.combat.ownership;

import com.cosmicpve.adventure.AdventureSavedData;
import com.cosmicpve.adventure.AdventureSession;
import com.cosmicpve.trial.TrialRuntime;
import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/** Canonical general-ally relationship. Owned-only enchantments deliberately do not use this resolver. */
public final class GeneralAllyResolver {
    private final List<ActivityAllyProvider> activities;

    public GeneralAllyResolver(List<ActivityAllyProvider> activities) {
        this.activities = List.copyOf(activities);
    }

    public static GeneralAllyResolver production() {
        return new GeneralAllyResolver(List.of(GeneralAllyResolver::sameTrial, GeneralAllyResolver::sameAdventure));
    }

    public boolean isAlly(LivingEntity source, LivingEntity candidate) {
        if (!qualifies(candidate) || source == candidate) return false;
        var resolvedOwner = OwnedAllyResolver.owner(candidate).orElse(null);
        if (resolvedOwner == source) return true;
        if (!(source instanceof ServerPlayer sourcePlayer)) return false;
        if (candidate instanceof ServerPlayer candidatePlayer)
            return coParticipants(sourcePlayer, candidatePlayer);
        return resolvedOwner instanceof ServerPlayer ownerPlayer
                && qualifies(ownerPlayer) && coParticipants(sourcePlayer, ownerPlayer);
    }

    public List<LivingEntity> alliesWithin(LivingEntity source, double radius) {
        if (!(radius >= 0.0) || !Double.isFinite(radius)) throw new IllegalArgumentException("Invalid ally radius");
        var result = new LinkedHashSet<LivingEntity>();
        AABB bounds = source.getBoundingBox().inflate(radius);
        for (LivingEntity candidate : source.level().getEntitiesOfClass(LivingEntity.class, bounds,
                entity -> entity != source && qualifies(entity))) {
            if (within(source, candidate, radius) && isAlly(source, candidate)) result.add(candidate);
        }
        return List.copyOf(result);
    }

    public boolean hasAllyWithin(LivingEntity source, double radius) {
        return !alliesWithin(source, radius).isEmpty();
    }

    public boolean coParticipants(ServerPlayer source, ServerPlayer candidate) {
        if (source == candidate || !qualifies(source) || !qualifies(candidate)
                || source.level().getServer() == null
                || candidate.level().getServer() != source.level().getServer()) return false;
        var players = source.level().getServer().getPlayerList();
        if (players.getPlayer(source.getUUID()) != source || players.getPlayer(candidate.getUUID()) != candidate)
            return false;
        return activities.stream().anyMatch(provider -> provider.areCoParticipants(source, candidate));
    }

    public static boolean within(LivingEntity origin, LivingEntity candidate, double radius) {
        return origin.distanceToSqr(candidate) <= radius * radius;
    }

    private static boolean qualifies(LivingEntity entity) {
        return entity.isAlive() && !entity.isDeadOrDying() && !entity.isRemoved();
    }

    private static boolean sameTrial(ServerPlayer first, ServerPlayer second) {
        return TrialRuntime.sessions().active(first.level().getServer())
                .filter(session -> sameTrialSession(session, first.getUUID(), second.getUUID()))
                .isPresent();
    }

    private static boolean sameAdventure(ServerPlayer first, ServerPlayer second) {
        AdventureSavedData data = AdventureSavedData.get(first.level().getServer());
        return sameAdventureContext(data.get(first.getUUID()), data.get(second.getUUID()));
    }

    public static boolean sameTrialSession(com.cosmicpve.trial.TrialSession session,
            java.util.UUID first, java.util.UUID second) {
        return !first.equals(second) && session.activeParticipant(first) && session.activeParticipant(second);
    }

    /** Adventure session UUIDs are per player; active Dense Woodlands participation is the shared context. */
    public static boolean sameAdventureContext(AdventureSession first, AdventureSession second) {
        return first != null && second != null && !first.owner().equals(second.owner())
                && participatingAdventure(first) && participatingAdventure(second);
    }

    private static boolean participatingAdventure(AdventureSession session) {
        return session != null && (session.phase() == AdventureSession.Phase.TRANSITION
                || session.phase() == AdventureSession.Phase.ACTIVE);
    }
}
