package com.cosmicpve.equipment.armor;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** Maps one authoritative transaction outcome to its one feedback dispatch. */
public final class ArmorCrystalFeedback {
    private ArmorCrystalFeedback() {}

    public static List<SoundEvent> soundsFor(ArmorCrystalApplicationService.Outcome outcome) {
        return switch (outcome) {
            case SUCCESS -> List.of(SoundEvents.PLAYER_LEVELUP);
            case FAILED_DESTROYED -> List.of(SoundEvents.LAVA_AMBIENT, SoundEvents.ANVIL_DESTROY);
            case FAILED_PROTECTED -> List.of(SoundEvents.LAVA_AMBIENT);
            default -> List.of();
        };
    }

    public static void play(ServerPlayer player, ArmorCrystalApplicationService.Outcome outcome) {
        soundsFor(outcome).forEach(sound -> player.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), SoundSource.PLAYERS,
                player.getX(), player.getY(), player.getZ(), 1.0F, 1.0F, 0L)));
    }
}
