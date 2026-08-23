package com.cosmicpve.instance.structure;

import com.cosmicpve.instance.InstanceBounds;
import net.minecraft.core.BlockPos;

public record InstanceStructurePlacement(InstanceBounds bounds, BlockPos participantSpawn) {}
