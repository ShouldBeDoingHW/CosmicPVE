package com.cosmicpve.network;

import com.cosmicpve.CosmicPVE;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.HitResult;

/** No client-controlled source ID or outcome data; resolves only the server's current main-hand stack. */
public record LootPreviewPayload() implements CustomPacketPayload {
    public static final Type<LootPreviewPayload> TYPE = new Type<>(CosmicPVE.id("loot_preview"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LootPreviewPayload> STREAM_CODEC = StreamCodec.unit(new LootPreviewPayload());
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static boolean leftClickAir(ServerPlayer player) {
        if (player.pick(player.blockInteractionRange(), 1, false).getType() != HitResult.Type.MISS) return false;
        var start = player.getEyePosition();
        var delta = player.getViewVector(1).scale(player.entityInteractionRange());
        return ProjectileUtil.getEntityHitResult(player, start, start.add(delta),
                player.getBoundingBox().expandTowards(delta).inflate(1),
                entity -> !entity.isSpectator() && entity.isPickable(), delta.lengthSqr()) == null;
    }
}
