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
import net.minecraft.ChatFormatting;

/** Client-only participant HUD and harmless cash-out firework particles. */
public final class TrialClientPresentation {
    static final int HUD_HEIGHT = 132;
    static final int HUD_WIDTH = 120;
    static final int HORIZONTAL_PADDING = 8;
    private static int timerSeconds = -1;
    private static String timerText = "";
    private static String ownerHeading = "";
    private static String phaseLabel = "";
    private static int phaseColor = 0xFFFFFF;
    private static int roomOrdinal;
    private static String roomName = "";
    private TrialClientPresentation() {}

    public static void registerPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(TrialTimerPayload.TYPE, (payload, context) -> {
            timerSeconds = payload.seconds(); timerText = formatSeconds(timerSeconds);
        });
        event.register(TrialOwnerPayload.TYPE, (payload, context) -> ownerHeading = payload.heading());
        event.register(TrialPhasePayload.TYPE, (payload, context) -> {
            phaseLabel = payload.label(); phaseColor = payload.color();
        });
        event.register(TrialRoomPayload.TYPE, (payload, context) -> {
            roomOrdinal = payload.ordinal(); roomName = payload.displayName();
        });
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
        PanelLayout layout = layout(graphics.guiWidth(), graphics.guiHeight());
        int textWidth = Math.max(1, layout.width() - HORIZONTAL_PADDING * 2);
        heading = fit(minecraft.font, heading, textWidth);
        String phase = fit(minecraft.font, phaseLabel, textWidth);
        String room = fit(minecraft.font, roomName, textWidth);
        int x = layout.left() + HORIZONTAL_PADDING;
        int top = layout.top();
        graphics.fill(layout.left(), top, layout.right(), top + layout.height(), 0x88000000);
        graphics.fill(layout.left(), top, layout.right(), top + 1, 0xAAFFAA00);
        graphics.drawString(minecraft.font, Component.literal(heading).withStyle(ChatFormatting.BOLD),
                x, top + 10, 0xFFFFAA00, false);
        graphics.drawString(minecraft.font, Component.literal(tierHeading(phaseLabel)).withStyle(ChatFormatting.BOLD),
                x, top + 30, 0xFFFFFFFF, false);
        if (!phase.isBlank()) graphics.drawString(minecraft.font, Component.literal(phase),
                x, top + 41, 0xFF000000 | phaseColor, false);
        graphics.drawString(minecraft.font, Component.literal(roomHeading(roomOrdinal)).withStyle(ChatFormatting.BOLD),
                x, top + 61, 0xFFFFFFFF, false);
        if (!room.isBlank()) graphics.drawString(minecraft.font, Component.literal(room),
                x, top + 72, 0xFFE0E0E0, false);
        graphics.drawString(minecraft.font, Component.literal("Time Left").withStyle(ChatFormatting.BOLD),
                x, top + 93, 0xFFFFFFFF, false);
        graphics.drawString(minecraft.font, Component.literal(value), x, top + 104, 0xFFE0E0E0, false);
    }

    private static String fit(net.minecraft.client.gui.Font font, String value, int width) {
        if (value.isBlank() || font.width(value) <= width) return value;
        String ellipsis = "...";
        return font.plainSubstrByWidth(value, Math.max(1, width - font.width(ellipsis))) + ellipsis;
    }

    static String formatSeconds(int seconds) {
        return com.cosmicpve.trial.TrialTimerDisplayService.formatSeconds(seconds);
    }

    static String tierHeading(String phase) {
        return "Tier (" + switch (phase) {
            case "Hardcore" -> 2;
            case "Impossible" -> 3;
            case "Demonic" -> 4;
            default -> 1;
        } + "/4)";
    }

    static String roomHeading(int ordinal) { return ordinal > 0 ? "Room (#" + ordinal + ")" : "Room"; }

    static PanelLayout layout(int guiWidth, int guiHeight) {
        int width = Math.max(1, Math.min(HUD_WIDTH, guiWidth));
        int height = Math.max(1, Math.min(HUD_HEIGHT, guiHeight));
        return new PanelLayout(guiWidth - width, Math.max(0, (guiHeight - height) / 2), width, height);
    }

    record PanelLayout(int left, int top, int width, int height) {
        int right() { return left + width; }
    }
}
