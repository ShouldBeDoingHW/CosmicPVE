package com.cosmicpve.network;

import com.cosmicpve.CosmicPVE;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Participant-only lifecycle-driven Trial room/Decision HUD line. */
public record TrialRoomPayload(String line) implements CustomPacketPayload {
    public static final Type<TrialRoomPayload> TYPE = new Type<>(CosmicPVE.id("trial_room"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TrialRoomPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TrialRoomPayload::line, TrialRoomPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
