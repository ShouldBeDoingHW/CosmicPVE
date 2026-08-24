package com.cosmicpve.client;

import com.cosmicpve.network.TrialCelebrationPayload;
import com.cosmicpve.network.TrialTimerPayload;
import com.cosmicpve.network.TrialOwnerPayload;
import com.cosmicpve.network.TrialPhasePayload;
import com.cosmicpve.network.TrialRoomPayload;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.FireworkExplosion;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

/** Client-only participant HUD and harmless cash-out firework particles. */
public final class TrialClientPresentation {
    static final int HUD_HEIGHT = 132;
    static final int MIN_HUD_WIDTH = 150;
    static final int HORIZONTAL_PADDING = 18;
    static final int LINE_STEP = 27;
    private static int timerSeconds = -1;
    private static String timerText = "";
    private static String ownerHeading = "";
    private static String phaseLabel = "";
    private static int phaseColor = 0xFFFFFF;
    private static String roomLine = "";
    private TrialClientPresentation() {}

    public static void registerPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(TrialTimerPayload.TYPE, (payload, context) -> {
            timerSeconds = payload.seconds(); timerText = formatSeconds(timerSeconds);
        });
        event.register(TrialOwnerPayload.TYPE, (payload, context) -> ownerHeading = payload.heading());
        event.register(TrialPhasePayload.TYPE, (payload, context) -> {
            phaseLabel = payload.label(); phaseColor = payload.color();
        });
        event.register(TrialRoomPayload.TYPE, (payload, context) -> roomLine = payload.line());
        event.register(TrialCelebrationPayload.TYPE, (payload, context) -> {
            var level = Minecraft.getInstance().level;
            if (level == null) return;
            var explosion = new FireworkExplosion(FireworkExplosion.Shape.LARGE_BALL,
                    IntList.of(payload.color()), IntList.of(0xFFFFFF), true, true);
            level.createFireworks(payload.x(), payload.y(), payload.z(), 0.0, 0.08, 0.0, List.of(explosion));
        });
    }

    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.SCOREBOARD_SIDEBAR,
                com.cosmicpve.CosmicPVE.id("trial_timer"), TrialClientPresentation::renderTimer);
    }

    private static void renderTimer(GuiGraphics graphics, net.minecraft.client.DeltaTracker ignored) {
        Minecraft minecraft = Minecraft.getInstance();
        if (timerSeconds < 0 || minecraft.options.hideGui) return;
        String value = timerText;
        String heading = ownerHeading.isBlank() ? "Trial" : ownerHeading;
        int maxWidth = Math.max(48, graphics.guiWidth() - 12);
        int contentWidth = Math.max(minecraft.font.width(heading), minecraft.font.width(value));
        if (!phaseLabel.isBlank()) contentWidth = Math.max(contentWidth, minecraft.font.width(phaseLabel));
        if (!roomLine.isBlank()) contentWidth = Math.max(contentWidth, minecraft.font.width(roomLine));
        int width = Math.min(maxWidth, Math.max(MIN_HUD_WIDTH, contentWidth + HORIZONTAL_PADDING * 2));
        int right = graphics.guiWidth() - 3;
        int left = right - width;
        int top = 34;
        int textWidth = Math.max(1, width - HORIZONTAL_PADDING * 2);
        heading = fit(minecraft.font, heading, textWidth);
        String phase = fit(minecraft.font, phaseLabel, textWidth);
        String room = fit(minecraft.font, roomLine, textWidth);
        graphics.fill(left, top, right, top + HUD_HEIGHT, 0x88000000);
        graphics.fill(left, top, right, top + 1, 0xAAFFAA00);
        graphics.drawString(minecraft.font, Component.literal(heading), left + HORIZONTAL_PADDING, top + 13, 0xFFFFAA00, false);
        if (!phase.isBlank()) graphics.drawString(minecraft.font, Component.literal(phase),
                left + HORIZONTAL_PADDING, top + 13 + LINE_STEP, 0xFF000000 | phaseColor, false);
        if (!room.isBlank()) graphics.drawString(minecraft.font, Component.literal(room),
                left + HORIZONTAL_PADDING, top + 13 + LINE_STEP * 2, 0xFFFFFFFF, false);
        graphics.drawString(minecraft.font, Component.literal(value), right - HORIZONTAL_PADDING - minecraft.font.width(value),
                top + 13 + LINE_STEP * 3,
                0xFFFFFFFF, false);
    }

    private static String fit(net.minecraft.client.gui.Font font, String value, int width) {
        if (value.isBlank() || font.width(value) <= width) return value;
        String ellipsis = "...";
        return font.plainSubstrByWidth(value, Math.max(1, width - font.width(ellipsis))) + ellipsis;
    }

    static String formatSeconds(int seconds) {
        int safe = Math.max(0, seconds);
        return safe / 60 + ":" + String.format(java.util.Locale.ROOT, "%02d", safe % 60);
    }
}
