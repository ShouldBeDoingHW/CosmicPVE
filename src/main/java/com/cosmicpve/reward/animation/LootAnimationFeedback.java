package com.cosmicpve.reward.animation;

import com.cosmicpve.network.TrialCelebrationPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class LootAnimationFeedback {
    public static final float REVEAL_PITCH = 0.1F;
    private LootAnimationFeedback() {}
    public static void preview(ServerPlayer player, float pitch) { sound(player, SoundEvents.ARROW_HIT_PLAYER, pitch); }
    public static void reveal(ServerPlayer player) {
        sound(player, SoundEvents.PLAYER_LEVELUP, REVEAL_PITCH);
        cosmeticFirework(player, 0xF7658D);
    }
    public static void bundle(ServerPlayer player) {
        sound(player, SoundEvents.PLAYER_LEVELUP, 1.0F);
        cosmeticFirework(player, 0xFA0246);
    }
    static void cosmeticFirework(ServerPlayer player, int color) {
        player.connection.send(new ClientboundCustomPayloadPacket(
                new TrialCelebrationPayload(player.getX(), player.getY() + 2.2, player.getZ(), color)));
    }
    static ClientboundSoundPacket packet(ServerPlayer player, SoundEvent event, float pitch) {
        return new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(event), SoundSource.PLAYERS,
                player.getX(), player.getY(), player.getZ(), 1.0F, pitch, player.getRandom().nextLong());
    }
    private static void sound(ServerPlayer player, SoundEvent event, float pitch) { player.connection.send(packet(player, event, pitch)); }
}
