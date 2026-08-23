package com.cosmicpve.instance.protection;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

@FunctionalInterface
public interface InstanceProtectionPolicy {
    boolean allows(UUID sessionId, Identifier roomId, InstanceMutationCause cause, BlockPos pos);
}
