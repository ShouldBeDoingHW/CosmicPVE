package com.cosmicpve.network;

import com.cosmicpve.CosmicPVE;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Participant-only authoritative Trial timer update; -1 hides the HUD. */
public record TrialTimerPayload(int seconds, boolean obfuscated) implements CustomPacketPayload {
    public TrialTimerPayload(int seconds) { this(seconds,false); }
    public static final Type<TrialTimerPayload> TYPE = new Type<>(CosmicPVE.id("trial_timer"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TrialTimerPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TrialTimerPayload::seconds,
            ByteBufCodecs.BOOL, TrialTimerPayload::obfuscated, TrialTimerPayload::new);

    @Override public Type<TrialTimerPayload> type() { return TYPE; }
}
