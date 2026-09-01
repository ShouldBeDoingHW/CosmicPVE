package com.cosmicpve.client;

import com.cosmicpve.economy.flashsale.FlashSalePreviewMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class FlashSalePreviewScreen extends AbstractContainerScreen<FlashSalePreviewMenu> {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    public FlashSalePreviewScreen(FlashSalePreviewMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 132;
        inventoryLabelY = 38;
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2, y = (height - imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, 0, 0, imageWidth, 35, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y + 35, 0, 126, imageWidth, 96, 256, 256);
    }
}
