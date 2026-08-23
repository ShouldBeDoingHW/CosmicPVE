package com.cosmicpve.trial;

import net.minecraft.resources.Identifier;

@FunctionalInterface
public interface TrialRoomWeightModifier {
    int modify(TrialSession session, Identifier roomId);
}
