package com.cosmicpve.client;

import com.cosmicpve.network.TrialCelebrationPayload;
import com.cosmicpve.network.TrialTimerPayload;
import com.cosmicpve.network.TrialOwnerPayload;
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
    private static int timerSeconds = -1;
    private static String timerText = "";
    private static String ownerHeading = "";
    private TrialClientPresentation() {}

    public static void registerPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(TrialTimerPayload.TYPE, (payload, context) -> {
            timerSeconds = payload.seconds(); timerText = formatSeconds(timerSeconds);
        });
        event.register(TrialOwnerPayload.TYPE, (payload, context) -> ownerHeading = payload.heading());
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
        int width = Math.max(minecraft.font.width(heading), minecraft.font.width(value)) + 12;
        int right = graphics.guiWidth() - 3;
        int left = right - width;
        int top = 34;
        graphics.fill(left, top, right, top + 24, 0x88000000);
        graphics.fill(left, top, right, top + 1, 0xAAFFAA00);
        graphics.drawString(minecraft.font, Component.literal(heading), left + 6, top + 4, 0xFFFFAA00, false);
        graphics.drawString(minecraft.font, Component.literal(value), right - 6 - minecraft.font.width(value), top + 14,
                0xFFFFFFFF, false);
    }

    static String formatSeconds(int seconds) {
        int safe = Math.max(0, seconds);
        return safe / 60 + ":" + String.format(java.util.Locale.ROOT, "%02d", safe % 60);
    }
}
