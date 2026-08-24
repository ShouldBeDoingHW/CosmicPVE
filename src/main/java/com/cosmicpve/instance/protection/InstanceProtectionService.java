package com.cosmicpve.instance.protection;

import com.cosmicpve.trial.TrialRuntime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public final class InstanceProtectionService {
    private final List<InstanceProtectionPolicy> explicitAllows = new CopyOnWriteArrayList<>();
    public void addExplicitAllow(InstanceProtectionPolicy policy) { explicitAllows.add(policy); }
    public void clearExplicitAllows() { explicitAllows.clear(); }

    public boolean protectedPosition(ServerLevel level, BlockPos pos) {
        if (!level.dimension().equals(TrialRuntime.INSTANCE_DIMENSION)) return false;
        return TrialRuntime.sessions().active(level.getServer())
                .map(session -> contains(session.protectedBounds(), pos))
                .orElse(false);
    }

    public boolean denies(ServerLevel level, Player actor, BlockPos pos, InstanceMutationCause cause) {
        if (!level.dimension().equals(TrialRuntime.INSTANCE_DIMENSION)) return false;
        var session = TrialRuntime.sessions().active(level.getServer()).orElse(null);
        if (session == null || !contains(session.protectedBounds(), pos)) return false;
        var room = session.currentRoom().orElse(com.cosmicpve.CosmicPVE.id("trial/decision_box"));
        return shouldDeny(true, actor != null && actor.isCreative(), session.sessionId(), room, cause, pos, explicitAllows);
    }

    private static boolean contains(List<com.cosmicpve.instance.InstanceBounds> bounds, BlockPos pos) {
        for (var bound : bounds) if (bound.contains(pos)) return true;
        return false;
    }

    public static boolean shouldDeny(boolean protectedPosition, boolean debugBypass, java.util.UUID sessionId,
            net.minecraft.resources.Identifier roomId, InstanceMutationCause cause, BlockPos pos,
            List<InstanceProtectionPolicy> allows) {
        if (!protectedPosition || debugBypass) return false;
        return allows.stream().noneMatch(policy -> policy.allows(sessionId, roomId, cause, pos));
    }
}
