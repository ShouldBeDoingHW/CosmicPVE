package com.cosmicpve.network;

import com.cosmicpve.CosmicPVE;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Participant-only immutable Trial owner heading; an empty heading hides it. */
public record TrialOwnerPayload(String heading) implements CustomPacketPayload {
    public static final Type<TrialOwnerPayload> TYPE = new Type<>(CosmicPVE.id("trial_owner"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TrialOwnerPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TrialOwnerPayload::heading, TrialOwnerPayload::new);

    @Override public Type<TrialOwnerPayload> type() { return TYPE; }
}
