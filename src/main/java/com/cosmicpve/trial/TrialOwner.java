package com.cosmicpve.trial;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

/** Immutable identity of the player who created the Trial Portal. */
public record TrialOwner(UUID playerId, String nameSnapshot) {
    public static final TrialOwner DEVELOPMENT = new TrialOwner(new UUID(0L, 0L), "Development");
    public static final Codec<TrialOwner> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("player_id").forGetter(TrialOwner::playerId),
            Codec.STRING.fieldOf("name_snapshot").forGetter(TrialOwner::nameSnapshot)
    ).apply(instance, TrialOwner::new));

    public TrialOwner {
        if (nameSnapshot == null || nameSnapshot.isBlank() || nameSnapshot.length() > 64) {
            throw new IllegalArgumentException("Trial owner name must contain 1-64 characters");
        }
    }

    public String header() { return nameSnapshot + "'s Trial"; }
}
