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
    private boolean tooltipRenderedThisFrame;

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
        tooltipRenderedThisFrame = true;
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
        ScrollRange range = scrollRange(originalPosition.y(), height, event.getScreenHeight());
        state.update(current, currentFingerprint, range.minimumOffset(), range.maximumOffset());
        int offset = state.offsetPixels();
        renderedAtNanos = System.nanoTime();
        if (offset != 0) {
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

    public void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        tooltipRenderedThisFrame = false;
    }

    public void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (!tooltipRenderedThisFrame && state.matchesScreen(event.getScreen())) reset();
    }

    void reset() {
        state.reset();
        renderedAtNanos = 0;
        tooltipRenderedThisFrame = false;
    }
    static int overflow(int tooltipY, int tooltipHeight, int screenHeight) {
        return scrollRange(tooltipY, tooltipHeight, screenHeight).distance();
    }
    static ScrollRange scrollRange(int tooltipY, int tooltipHeight, int screenHeight) {
        int verticalPadding = EDGE_PADDING / 2;
        int usableHeight = Math.max(1, screenHeight - 2 * verticalPadding);
        if (tooltipHeight <= usableHeight) return new ScrollRange(0, 0);
        int topOffset = tooltipY - verticalPadding;
        int bottomOffset = tooltipY + tooltipHeight - (screenHeight - verticalPadding);
        return new ScrollRange(Math.min(topOffset, bottomOffset), Math.max(topOffset, bottomOffset));
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
        private int minimumOffset;
        private int maximumOffset;
        void update(Object screen, int identity, int overflow) {
            update(screen, identity, 0, Math.max(0, overflow));
        }
        void update(Object screen, int identity, int minimumOffset, int maximumOffset) {
            boolean changed = this.screen != screen || this.identity != identity;
            this.screen = screen; this.identity = identity;
            this.minimumOffset = Math.min(minimumOffset, maximumOffset);
            this.maximumOffset = Math.max(minimumOffset, maximumOffset);
            if (changed) offsetPixels = this.minimumOffset;
            else offsetPixels = Math.max(this.minimumOffset, Math.min(offsetPixels, this.maximumOffset));
        }
        boolean scroll(Object screen, double delta) {
            if (this.screen != screen || minimumOffset == maximumOffset || delta == 0) return false;
            offsetPixels = Math.max(minimumOffset, Math.min(maximumOffset,
                    offsetPixels + (delta < 0 ? ROW_PIXELS : -ROW_PIXELS)));
            return true;
        }
        int offsetPixels() { return offsetPixels; }
        boolean matchesScreen(Object screen) { return this.screen == screen; }
        void reset() { screen = null; identity = 0; offsetPixels = 0; minimumOffset = 0; maximumOffset = 0; }
    }

    record ScrollRange(int minimumOffset, int maximumOffset) {
        int distance() { return Math.max(0, maximumOffset - minimumOffset); }
    }

    private record AttachedIdentityClientTooltip(FormattedCharSequence text) implements ClientTooltipComponent {
        @Override public int getHeight(Font font) { return 10; }
        @Override public int getWidth(Font font) { return font.width(text); }
        @Override public void renderText(GuiGraphics graphics, Font font, int x, int y) {
            graphics.drawString(font, text, x, y, -1, true);
        }
    }
}
