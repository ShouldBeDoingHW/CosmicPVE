package com.cosmicpve.vkit;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** Player-local feedback for an authoritative V-Kit Crystal redemption. */
final class VKitCrystalFeedback {
    private VKitCrystalFeedback() {}

    static SoundEvent successSound() {
        return SoundEvents.PLAYER_LEVELUP;
    }

    static ClientboundSoundPacket successPacket(ServerPlayer player) {
        return new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(successSound()),
                SoundSource.PLAYERS,
                player.getX(), player.getY(), player.getZ(),
                1.0F, 1.0F, player.getRandom().nextLong());
    }

    static void playSuccess(ServerPlayer player) {
        player.connection.send(successPacket(player));
    }
}
