package com.cosmicpve.trial;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record TrialRoomAppearance(Identifier roomId, int count) {
    public static final Codec<TrialRoomAppearance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("room").forGetter(TrialRoomAppearance::roomId),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("count").forGetter(TrialRoomAppearance::count)
    ).apply(instance, TrialRoomAppearance::new));
}
