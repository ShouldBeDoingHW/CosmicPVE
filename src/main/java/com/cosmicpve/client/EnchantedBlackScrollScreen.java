package com.cosmicpve.client;

import com.cosmicpve.equipment.enchantment.EnchantedBlackScrollMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class EnchantedBlackScrollScreen extends AbstractContainerScreen<EnchantedBlackScrollMenu> {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    public EnchantedBlackScrollScreen(EnchantedBlackScrollMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = menu.rows() * 18 + 17;
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2, y = (height - imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
    }
}
