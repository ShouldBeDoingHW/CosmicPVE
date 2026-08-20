package com.cosmicpve.equipment.skin;

import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class WeaponSkinFeedback {
    public static final float ATTACH_PITCH = 1.0F;
    public static final float REMOVE_PITCH = 0.7F;
    private WeaponSkinFeedback() {}

    public static void play(ServerPlayer player, boolean attaching) {
        player.connection.send(new ClientboundSoundPacket(
                SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS,
                player.getX(), player.getY(), player.getZ(), 1.0F,
                attaching ? ATTACH_PITCH : REMOVE_PITCH, 0L));
    }
}
