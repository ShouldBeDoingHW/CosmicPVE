package com.cosmicpve.spacechest;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** Central presentation policy for server-authoritative Space Chest transitions. */
public final class SpaceChestSounds {
    private SpaceChestSounds() {}

    public static SoundEvent initialSelection() { return SoundEvents.ARMOR_EQUIP_LEATHER.value(); }
    public static SoundEvent missedRewardsCleared() { return SoundEvents.CHICKEN_EGG; }
    public static SoundEvent selectedRewardRevealed() { return SoundEvents.EXPERIENCE_ORB_PICKUP; }
    public static SoundEvent committedMenuClosed() { return SoundEvents.CHEST_CLOSE; }
    public static boolean playsCloseSound(SpaceChestPhase phase) { return phase == SpaceChestPhase.COMMITTED; }

    public static void playForPlayer(ServerPlayer player, SoundEvent sound) {
        player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
                SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0F, 1.0F,
                player.getRandom().nextLong()));
    }
}
