package com.cosmicpve.trial;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

public record TrialPlayerDecision(UUID playerId, TrialDecision decision) {
    public static final Codec<TrialPlayerDecision> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("player").forGetter(TrialPlayerDecision::playerId),
            TrialDecision.CODEC.fieldOf("decision").forGetter(TrialPlayerDecision::decision)
    ).apply(instance, TrialPlayerDecision::new));
}
