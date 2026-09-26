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
    private static final java.util.Set<Integer> DECISION_ANNOUNCEMENTS = java.util.Set.of(30,25,20,15,10,5,4,3,2,1);
    private final java.util.Map<java.util.UUID, String> lastPresentation = new java.util.HashMap<>();
    public void decision(ServerPlayer player, boolean joining, int seconds) {
        if (!shouldAnnounceDecision(seconds)) return;
        if (!newPresentation(player, "decision:" + joining + ":" + seconds)) return;
        send(player, decisionTitle(), decisionSubtitle(joining, seconds), 10, 10, 5);
        playForPlayer(player, countdownSound());
    }
    public void roomCountdown(ServerPlayer player, String roomName, int seconds) {
        if (!newPresentation(player, "room:" + roomName + ":" + seconds)) return;
        send(player, roomTitle(roomName), roomSubtitle(seconds), 0, 25, 5);
        playForPlayer(player, countdownSound());
    }
    public void roomStarted(ServerPlayer player) {
        if (!newPresentation(player, "room_started")) return;
        playForPlayer(player, roomStartSound());
    }
    public static void cinderWolfDown(ServerPlayer player) {
        send(player, cinderWolfDownTitle(), Component.empty(), 10, 80, 20);
    }
    static Component cinderWolfDownTitle() {
        return Component.literal("Cinderwolf down! Find the exit portal!")
                .withStyle(style -> style.withColor(ORANGE).withBold(true));
    }
    public void warzoneWarning(ServerPlayer player,
            com.cosmicpve.trial.room.WarzoneGiantsService.FloorColor first,
            com.cosmicpve.trial.room.WarzoneGiantsService.FloorColor second, int seconds) {
        if (!newPresentation(player, "warzone:" + first.serialized + ":" + second.serialized + ":" + seconds)) return;
        Component title = first.label().copy().append(Component.literal(" + ").withStyle(style -> style.withBold(true)))
                .append(second.label());
        Component subtitle = Component.literal(Integer.toString(seconds)).withStyle(style -> style.withBold(true).withColor(0xFFFFFF));
        send(player, title, subtitle, 0, 25, 0);
        playForPlayer(player, countdownSound());
    }
    private boolean newPresentation(ServerPlayer player, String token) {
        return acceptPresentation(player.getUUID(), token);
    }
    boolean acceptPresentation(java.util.UUID playerId, String token) {
        return !token.equals(lastPresentation.put(playerId, token));
    }
    static Component decisionTitle() { return Component.literal("Decision Box").withStyle(style -> style.withColor(ORANGE)); }
    static Component decisionSubtitle(boolean joining, int seconds) {
        String unit = seconds == 1 ? " second" : " seconds";
        return Component.literal(joining ? seconds + unit + " for players to join!" : seconds + unit + " to choose!");
    }
    static boolean shouldAnnounceDecision(int seconds) { return DECISION_ANNOUNCEMENTS.contains(seconds); }
    static Component roomTitle(String roomName) { return Component.literal(roomName).withStyle(style -> style.withColor(ORANGE)); }
    static Component roomSubtitle(int seconds) {
        return Component.literal("Starting in... ")
                .append(Component.literal(seconds + "s").withStyle(style -> style.withColor(0x55FF55)));
    }
    static net.minecraft.sounds.SoundEvent countdownSound() { return SoundEvents.NOTE_BLOCK_BASEDRUM.value(); }
    static net.minecraft.sounds.SoundEvent roomStartSound() { return SoundEvents.ENDER_DRAGON_GROWL; }
    public void debugCountdownSound(ServerPlayer player) { playForPlayer(player, countdownSound()); }
    public void debugRoomStartSound(ServerPlayer player) { playForPlayer(player, roomStartSound()); }
    public static void playForPlayer(ServerPlayer player, SoundEvent sound) {
        playForPlayer(player, sound, 1.0F, 1.0F);
    }
    public static void playForPlayer(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
                SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), volume, pitch,
                player.getRandom().nextLong()));
    }
    private static void send(ServerPlayer player, Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
        player.connection.send(new ClientboundSetTitleTextPacket(title));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
    }
}
