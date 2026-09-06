package com.cosmicpve.combat.ownership;

import net.minecraft.server.level.ServerPlayer;

/** Activity-owned identity seam for deciding whether two live players are co-participants. */
@FunctionalInterface
public interface ActivityAllyProvider {
    boolean areCoParticipants(ServerPlayer source, ServerPlayer candidate);
}
