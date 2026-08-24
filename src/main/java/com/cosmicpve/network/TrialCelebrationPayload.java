package com.cosmicpve.network;

import com.cosmicpve.CosmicPVE;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Harmless client-side firework presentation at an already-restored cash-out position. */
public record TrialCelebrationPayload(double x, double y, double z, int color) implements CustomPacketPayload {
    public static final Type<TrialCelebrationPayload> TYPE = new Type<>(CosmicPVE.id("trial_celebration"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TrialCelebrationPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, TrialCelebrationPayload::x,
            ByteBufCodecs.DOUBLE, TrialCelebrationPayload::y,
            ByteBufCodecs.DOUBLE, TrialCelebrationPayload::z,
            ByteBufCodecs.INT, TrialCelebrationPayload::color,
            TrialCelebrationPayload::new);

    @Override public Type<TrialCelebrationPayload> type() { return TYPE; }
}
