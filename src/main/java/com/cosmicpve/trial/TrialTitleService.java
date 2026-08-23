package com.cosmicpve.trial;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;

public final class TrialTitleService {
    private static final TextColor ORANGE = TextColor.fromRgb(0xFFAA00);
    private final java.util.Map<java.util.UUID, String> lastPresentation = new java.util.HashMap<>();
    public void decision(ServerPlayer player, boolean joining, int seconds) {
        if (!newPresentation(player, "decision:" + joining + ":" + seconds)) return;
        send(player, decisionTitle(), decisionSubtitle(joining, seconds));
        playForPlayer(player, countdownSound());
    }
    public void roomCountdown(ServerPlayer player, String roomName, int seconds) {
        if (!newPresentation(player, "room:" + roomName + ":" + seconds)) return;
        send(player, roomTitle(roomName), roomSubtitle(seconds));
        playForPlayer(player, countdownSound());
    }
    public void roomStarted(ServerPlayer player) {
        if (!newPresentation(player, "room_started")) return;
        playForPlayer(player, roomStartSound());
    }
    private boolean newPresentation(ServerPlayer player, String token) {
        return acceptPresentation(player.getUUID(), token);
    }
    boolean acceptPresentation(java.util.UUID playerId, String token) {
        return !token.equals(lastPresentation.put(playerId, token));
    }
    static Component decisionTitle() { return Component.literal("Decision Box").withStyle(style -> style.withColor(ORANGE)); }
    static Component decisionSubtitle(boolean joining, int seconds) {
        return Component.literal(joining ? seconds + " seconds for players to join!" : seconds + " seconds to choose!");
    }
    static Component roomTitle(String roomName) { return Component.literal(roomName).withStyle(style -> style.withColor(ORANGE)); }
    static Component roomSubtitle(int seconds) {
        return Component.literal("Starting in... ")
                .append(Component.literal(seconds + "s").withStyle(style -> style.withColor(0x55FF55)));
    }
    static net.minecraft.sounds.SoundEvent countdownSound() { return SoundEvents.NOTE_BLOCK_BASEDRUM.value(); }
    static net.minecraft.sounds.SoundEvent roomStartSound() { return SoundEvents.ENDER_DRAGON_GROWL; }
    public void debugCountdownSound(ServerPlayer player) { playForPlayer(player, countdownSound()); }
    public void debugRoomStartSound(ServerPlayer player) { playForPlayer(player, roomStartSound()); }
    static void playForPlayer(ServerPlayer player, SoundEvent sound) {
        player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
                SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0F, 1.0F,
                player.getRandom().nextLong()));
    }
    private static void send(ServerPlayer player, Component title, Component subtitle) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 25, 5));
        player.connection.send(new ClientboundSetTitleTextPacket(title));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
    }
}
