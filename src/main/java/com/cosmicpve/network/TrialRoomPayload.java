package com.cosmicpve.network;

import com.cosmicpve.CosmicPVE;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Participant-only lifecycle-driven Trial room ordinal and display name. */
public record TrialRoomPayload(int ordinal, String displayName) implements CustomPacketPayload {
    public static final Type<TrialRoomPayload> TYPE = new Type<>(CosmicPVE.id("trial_room"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TrialRoomPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TrialRoomPayload::ordinal,
            ByteBufCodecs.STRING_UTF8, TrialRoomPayload::displayName,
            TrialRoomPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
