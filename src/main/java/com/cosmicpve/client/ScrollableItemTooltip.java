package com.cosmicpve.client;

import com.cosmicpve.equipment.mask.MaskLore;
import com.cosmicpve.registry.ModDataComponents;
import com.mojang.datafixers.util.Either;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Client-only row scrolling for item tooltips taller than an inventory/container screen. */
public final class ScrollableItemTooltip {
    static final int EDGE_PADDING = 8;
    static final int ROW_PIXELS = 10;
    private final State state = new State();
    private long renderedAtNanos;
    private boolean rerendering;

    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(MaskLore.AttachedIdentityTooltip.class,
                tooltip -> new AttachedIdentityClientTooltip(tooltip.text().getVisualOrderText()));
    }

    public void onGather(RenderTooltipEvent.GatherComponents event) {
        var masks = event.getItemStack().get(ModDataComponents.MASK_LOADOUT.get());
        if (masks == null || !masks.valid() || masks.presentations().size() != masks.maskIds().size()) return;
        replaceIdentityElement(event.getTooltipElements(), MaskLore.attachedIdentity(masks.presentations()));
    }

    public void onRender(RenderTooltipEvent.Pre event) {
        if (rerendering) return;
        Screen current = Minecraft.getInstance().screen;
        if (!(current instanceof AbstractContainerScreen<?>) || event.getItemStack().isEmpty()) {
            reset();
            return;
        }
        int currentFingerprint = ItemStack.hashItemAndComponents(event.getItemStack());
        var hovered = ((AbstractContainerScreen<?>) current).getSlotUnderMouse();
        currentFingerprint = 31 * currentFingerprint + (hovered == null ? -1 : hovered.index);
        int width = event.getComponents().stream().mapToInt(component -> component.getWidth(event.getFont())).max().orElse(0);
        int height = event.getComponents().stream().mapToInt(component -> component.getHeight(event.getFont())).sum() + 2;
        var originalPosition = event.getTooltipPositioner().positionTooltip(event.getScreenWidth(), event.getScreenHeight(),
                event.getX(), event.getY(), width, height);
        state.update(current, currentFingerprint, overflow(originalPosition.y(), height, event.getScreenHeight()));
        int offset = state.offsetPixels();
        renderedAtNanos = System.nanoTime();
        if (offset > 0) {
            var vanillaPositioner = event.getTooltipPositioner();
            var shifted = (net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner)
                    (screenWidth, screenHeight, x, y, tooltipWidth, heightValue) -> {
                        var position = vanillaPositioner.positionTooltip(screenWidth, screenHeight, x, y, tooltipWidth, heightValue);
                        return new org.joml.Vector2i(position.x(), position.y() - offset);
                    };
            event.setCanceled(true);
            rerendering = true;
            try {
                event.getGraphics().renderTooltip(event.getFont(), event.getComponents(), event.getX(), event.getY(),
                        shifted, event.getItemStack().get(DataComponents.TOOLTIP_STYLE), event.getItemStack());
            } finally { rerendering = false; }
        }
    }

    public void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (System.nanoTime() - renderedAtNanos <= 250_000_000L
                && state.scroll(event.getScreen(), event.getScrollDeltaY())) event.setCanceled(true);
    }

    public void onScreenClosing(ScreenEvent.Closing event) {
        if (state.matchesScreen(event.getScreen())) reset();
    }

    void reset() {
        state.reset();
        renderedAtNanos = 0;
    }
    static int overflow(int tooltipY, int tooltipHeight, int screenHeight) {
        return Math.max(0, tooltipY + tooltipHeight - Math.max(1, screenHeight - EDGE_PADDING / 2));
    }
    static boolean replaceIdentityElement(List<Either<FormattedText, TooltipComponent>> elements,
                                          net.minecraft.network.chat.Component identity) {
        for (int i = 0; i < elements.size(); i++) {
            var text = elements.get(i).left();
            if (text.isPresent() && text.orElseThrow().equals(identity)) {
                elements.set(i, Either.right(new MaskLore.AttachedIdentityTooltip(identity)));
                return true;
            }
        }
        return false;
    }

    static final class State {
        private Object screen;
        private int identity;
        private int offsetPixels;
        private int overflow;
        void update(Object screen, int identity, int overflow) {
            if (this.screen != screen || this.identity != identity) offsetPixels = 0;
            this.screen = screen; this.identity = identity; this.overflow = Math.max(0, overflow);
            offsetPixels = Math.min(offsetPixels, this.overflow);
        }
        boolean scroll(Object screen, double delta) {
            if (this.screen != screen || overflow <= 0 || delta == 0) return false;
            offsetPixels = Math.max(0, Math.min(overflow,
                    offsetPixels + (delta < 0 ? ROW_PIXELS : -ROW_PIXELS)));
            return true;
        }
        int offsetPixels() { return offsetPixels; }
        boolean matchesScreen(Object screen) { return this.screen == screen; }
        void reset() { screen = null; identity = 0; offsetPixels = 0; overflow = 0; }
    }

    private record AttachedIdentityClientTooltip(FormattedCharSequence text) implements ClientTooltipComponent {
        @Override public int getHeight(Font font) { return 10; }
        @Override public int getWidth(Font font) { return font.width(text); }
        @Override public void renderText(GuiGraphics graphics, Font font, int x, int y) {
            graphics.drawString(font, text, x, y, -1, true);
        }
    }
}
