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
                .map(session -> session.protectedBounds().stream().anyMatch(bounds -> bounds.contains(pos)))
                .orElse(false);
    }

    public boolean denies(ServerLevel level, Player actor, BlockPos pos, InstanceMutationCause cause) {
        if (!protectedPosition(level, pos)) return false;
        var session = TrialRuntime.sessions().active(level.getServer()).orElseThrow();
        var room = session.currentRoom().orElse(com.cosmicpve.CosmicPVE.id("trial/decision_box"));
        return shouldDeny(true, actor != null && actor.isCreative(), session.sessionId(), room, cause, pos, explicitAllows);
    }

    public static boolean shouldDeny(boolean protectedPosition, boolean debugBypass, java.util.UUID sessionId,
            net.minecraft.resources.Identifier roomId, InstanceMutationCause cause, BlockPos pos,
            List<InstanceProtectionPolicy> allows) {
        if (!protectedPosition || debugBypass) return false;
        return allows.stream().noneMatch(policy -> policy.allows(sessionId, roomId, cause, pos));
    }
}
