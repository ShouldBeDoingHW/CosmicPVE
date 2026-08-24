package com.cosmicpve.network;

import com.cosmicpve.CosmicPVE;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Participant-only authoritative Trial phase display; an empty label hides it. */
public record TrialPhasePayload(String label, int color) implements CustomPacketPayload {
    public static final Type<TrialPhasePayload> TYPE = new Type<>(CosmicPVE.id("trial_phase"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TrialPhasePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TrialPhasePayload::label,
            ByteBufCodecs.INT, TrialPhasePayload::color,
            TrialPhasePayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
