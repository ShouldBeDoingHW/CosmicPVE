package com.cosmicpve.equipment.enchantment;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** Small shared mapping from authoritative item-application outcomes to vanilla sounds. */
public final class ItemApplicationFeedback {
    public enum Cue { SUCCESS, FAILED_SURVIVED, FAILED_PROTECTED, FAILED_DESTROYED, REJECTED }
    private ItemApplicationFeedback() {}

    public static List<SoundEvent> soundsFor(Cue cue) {
        return switch (cue) {
            case SUCCESS -> List.of(SoundEvents.PLAYER_LEVELUP);
            case FAILED_SURVIVED, FAILED_PROTECTED -> List.of(SoundEvents.LAVA_AMBIENT);
            case FAILED_DESTROYED -> List.of(SoundEvents.LAVA_AMBIENT, SoundEvents.ANVIL_DESTROY);
            case REJECTED -> List.of();
        };
    }

    public static Cue cueFor(CosmicBookApplicationResult.Outcome outcome) {
        return switch (outcome) {
            case SUCCESS -> Cue.SUCCESS;
            case FAILED_SURVIVED -> Cue.FAILED_SURVIVED;
            case FAILED_PROTECTED -> Cue.FAILED_PROTECTED;
            case FAILED_DESTROYED -> Cue.FAILED_DESTROYED;
            default -> Cue.REJECTED;
        };
    }

    public static Cue cueFor(OrbApplicationResult.Outcome outcome) {
        return switch (outcome) {
            case SUCCESS -> Cue.SUCCESS;
            case FAILED_SURVIVED -> Cue.FAILED_SURVIVED;
            case FAILED_PROTECTED -> Cue.FAILED_PROTECTED;
            case FAILED_DESTROYED -> Cue.FAILED_DESTROYED;
            default -> Cue.REJECTED;
        };
    }

    public static Cue cueFor(TransmogApplicationService.Outcome outcome) {
        return outcome == TransmogApplicationService.Outcome.SUCCESS ? Cue.SUCCESS : Cue.REJECTED;
    }

    public static void play(ServerPlayer player, Cue cue) {
        soundsFor(cue).forEach(sound -> player.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), SoundSource.PLAYERS,
                player.getX(), player.getY(), player.getZ(), 1.0F, 1.0F, 0L)));
    }
}
