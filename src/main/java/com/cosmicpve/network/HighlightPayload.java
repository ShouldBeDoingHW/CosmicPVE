package com.cosmicpve.network;

import com.cosmicpve.CosmicPVE;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Bounded, player-private result of one successful Highlight proc. */
public record HighlightPayload(List<BlockPos> positions, int durationTicks) implements CustomPacketPayload {
    public static final Type<HighlightPayload> TYPE = new Type<>(CosmicPVE.id("highlight"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HighlightPayload> STREAM_CODEC = StreamCodec.of(
            (RegistryFriendlyByteBuf buffer, HighlightPayload payload) -> {
                buffer.writeVarInt(payload.positions.size());
                for (BlockPos pos : payload.positions) buffer.writeLong(pos.asLong());
                buffer.writeVarInt(payload.durationTicks);
            },
            (RegistryFriendlyByteBuf buffer) -> {
                int count = buffer.readVarInt();
                if (count < 0 || count > 4096) throw new IllegalArgumentException("Invalid Highlight position count");
                var positions = new ArrayList<BlockPos>(count);
                for (int i = 0; i < count; i++) positions.add(BlockPos.of(buffer.readLong()));
                return new HighlightPayload(positions, buffer.readVarInt());
            });

    public HighlightPayload {
        positions = List.copyOf(positions);
        if (positions.size() > 4096 || durationTicks < 0 || durationTicks > 300)
            throw new IllegalArgumentException("Highlight payload exceeds its bounds");
    }

    @Override public Type<HighlightPayload> type() { return TYPE; }
}
