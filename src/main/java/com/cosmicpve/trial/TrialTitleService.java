package com.cosmicpve.trial;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;

public final class TrialTitleService {
    private static final TextColor ORANGE = TextColor.fromRgb(0xFFAA00);
    public void decision(ServerPlayer player, boolean joining, int seconds) {
        send(player, decisionTitle(), decisionSubtitle(joining, seconds));
    }
    public void roomCountdown(ServerPlayer player, String roomName, int seconds) {
        send(player, roomTitle(roomName), roomSubtitle(seconds));
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
    private static void send(ServerPlayer player, Component title, Component subtitle) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 25, 5));
        player.connection.send(new ClientboundSetTitleTextPacket(title));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
    }
}
