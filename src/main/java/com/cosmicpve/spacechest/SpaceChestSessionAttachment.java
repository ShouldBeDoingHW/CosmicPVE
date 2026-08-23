package com.cosmicpve.spacechest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

public record SpaceChestSessionAttachment(Optional<SpaceChestSession> session) {
    public static final com.mojang.serialization.MapCodec<SpaceChestSessionAttachment> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SpaceChestSession.CODEC.optionalFieldOf("session").forGetter(SpaceChestSessionAttachment::session)
    ).apply(instance, SpaceChestSessionAttachment::new));
    public static SpaceChestSessionAttachment empty() { return new SpaceChestSessionAttachment(Optional.empty()); }
    public static SpaceChestSessionAttachment of(SpaceChestSession session) {
        return new SpaceChestSessionAttachment(Optional.of(session));
    }
}
