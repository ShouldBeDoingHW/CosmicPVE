package com.cosmicpve.personalvault;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

final class PersonalVaultUnlockFeedback {
    private PersonalVaultUnlockFeedback() {}
    static net.minecraft.sounds.SoundEvent sound() { return SoundEvents.ARROW_HIT_PLAYER; }
    static ClientboundSoundPacket packet(ServerPlayer player) {
        return new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound()),
                SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(),
                1.0F, 1.0F, player.getRandom().nextLong());
    }
    static void play(ServerPlayer player) { player.connection.send(packet(player)); }
}
