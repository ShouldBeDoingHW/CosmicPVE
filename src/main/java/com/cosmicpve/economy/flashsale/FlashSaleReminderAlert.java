package com.cosmicpve.economy.flashsale;

import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

final class FlashSaleReminderAlert {
    private FlashSaleReminderAlert() {}
    static SoundEvent sound() { return SoundEvents.BEACON_ACTIVATE; }
    static void play(Iterable<ServerPlayer> players) { forEach(players, FlashSaleReminderAlert::play); }
    static <T> void forEach(Iterable<T> recipients, Consumer<T> alert) { recipients.forEach(alert); }
    private static void play(ServerPlayer player) {
        player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound()),
                SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0F, 1.0F,
                player.getRandom().nextLong()));
    }
}
