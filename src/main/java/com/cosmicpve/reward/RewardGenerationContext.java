package com.cosmicpve.reward;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

public record RewardGenerationContext(RegistryAccess registries, RandomSource random, @Nullable ServerPlayer player) {}
