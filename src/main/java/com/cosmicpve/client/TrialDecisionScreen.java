package com.cosmicpve.client;

import com.cosmicpve.trial.TrialDecisionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class TrialDecisionScreen extends AbstractContainerScreen<TrialDecisionMenu> {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    static final int ROWS_HEIGHT = 3 * 18 + 17;
    static final int BOTTOM_FRAME_HEIGHT = 7;
    public TrialDecisionScreen(TrialDecisionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title); imageHeight = ROWS_HEIGHT + BOTTOM_FRAME_HEIGHT;
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick); renderTooltip(graphics, mouseX, mouseY);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, -12566464, false);
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2, y = (height - imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, 0, 0, imageWidth, ROWS_HEIGHT, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y + ROWS_HEIGHT,
                0, 126, imageWidth, BOTTOM_FRAME_HEIGHT, 256, 256);
    }
}
