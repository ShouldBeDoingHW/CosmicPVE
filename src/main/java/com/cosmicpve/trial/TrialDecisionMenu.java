package com.cosmicpve.trial;

import com.cosmicpve.registry.ModMenus;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

public final class TrialDecisionMenu extends AbstractContainerMenu {
    public enum SlotRole { DEAL, BLOCKED_CENTER, NO_DEAL, POT }
    public static final int SLOT_COUNT = 27;
    public static final int ENTRIES_PER_PAGE = 18;
    private final SimpleContainer display = new SimpleContainer(SLOT_COUNT);
    private final Player owner;
    private int pageIndex;
    public TrialDecisionMenu(int id, Inventory inventory) {
        super(ModMenus.TRIAL_DECISION.get(), id); owner = inventory.player;
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) {
            int slot = row * 9 + column;
            addSlot(new DisplaySlot(display, slot, 8 + column * 18, 18 + row * 18));
        }
        refresh();
    }
    public static TrialDecisionMenu client(int id, Inventory inventory, RegistryFriendlyByteBuf data) {
        data.readInt(); return new TrialDecisionMenu(id, inventory);
    }
    public void refresh() {
        TrialSession session = owner instanceof ServerPlayer player
                ? TrialRuntime.sessions().active(player.level().getServer()).orElse(null) : null;
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (slot < 4) display.setItem(slot, pane(Items.GREEN_STAINED_GLASS_PANE, "DEAL", 0x55FF55));
            else if (slot == 4) display.setItem(slot, pageControl(session));
            else if (slot < 9) display.setItem(slot, pane(Items.RED_STAINED_GLASS_PANE, "NO DEAL", 0xFF5555));
            else {
                int index = pageIndex * ENTRIES_PER_PAGE + slot - 9;
                display.setItem(slot, session != null && index < session.progress().pot().size()
                        ? preview(session.progress().pot().get(index).items())
                        : pane(Items.WHITE_STAINED_GLASS_PANE, "???", 0xFFFFFF));
            }
        }
    }
    @Override public void clicked(int slot, int button, ClickType type, Player player) {
        if (slot < 0 || slot >= 9 || type != ClickType.PICKUP || !(player instanceof ServerPlayer serverPlayer)) return;
        if (slot == 4 && (button == 0 || button == 1)) {
            TrialSession session = TrialRuntime.sessions().active(serverPlayer.level().getServer()).orElse(null);
            int pages = totalPages(session == null ? 0 : session.progress().pot().size());
            if (pages > 1) {
                pageIndex = Math.floorMod(pageIndex + (button == 0 ? 1 : -1), pages);
                refresh();
            }
        } else if (button == 0) {
            if (slot < 4) TrialRuntime.sessions().decide(serverPlayer, TrialDecision.DEAL);
            else if (slot > 4) TrialRuntime.sessions().decide(serverPlayer, TrialDecision.NO_DEAL);
        }
    }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) { return true; }
    private static ItemStack pane(net.minecraft.world.item.Item item, String label, int color) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(label).withStyle(style -> style.withColor(color)));
        return stack;
    }
    static ItemStack preview(List<ItemStack> bundle) {
        if (bundle.isEmpty()) return pane(Items.BARRIER, "Unavailable reward", 0xFF5555);
        ItemStack stack = bundle.getFirst().copy();
        if (bundle.size() > 1) stack.set(DataComponents.LORE,
                new net.minecraft.world.item.component.ItemLore(List.of(Component.literal("Bundle: " + bundle.size() + " stacks"))));
        return stack;
    }
    public static SlotRole role(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) throw new IllegalArgumentException("slot outside Trial menu");
        if (slot < 4) return SlotRole.DEAL;
        if (slot == 4) return SlotRole.BLOCKED_CENTER;
        if (slot < 9) return SlotRole.NO_DEAL;
        return SlotRole.POT;
    }
    public static int potIndex(int slot) { return role(slot) == SlotRole.POT ? slot - 9 : -1; }
    public int visiblePotIndex(int slot) { return pagePotIndex(pageIndex, slot); }
    public static int pagePotIndex(int page, int slot) {
        int local = potIndex(slot); return local < 0 ? -1 : Math.max(0, page) * ENTRIES_PER_PAGE + local;
    }
    public int pageIndex() { return pageIndex; }
    public static int totalPages(int entries) { return Math.max(1, (Math.max(0, entries) + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE); }

    private ItemStack pageControl(TrialSession session) {
        int pages = totalPages(session == null ? 0 : session.progress().pot().size());
        if (pages == 1) return pane(Items.WHITE_STAINED_GLASS_PANE, "???", 0xFFFFFF);
        if (pageIndex >= pages) pageIndex = pages - 1;
        ItemStack paper = new ItemStack(Items.PAPER);
        paper.set(DataComponents.CUSTOM_NAME, Component.literal("Page " + (pageIndex + 1) + "/" + pages)
                .withStyle(style -> style.withColor(0xFFFFFF)));
        paper.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Left Click: Next Page").withStyle(net.minecraft.ChatFormatting.GRAY),
                Component.literal("Right Click: Previous Page").withStyle(net.minecraft.ChatFormatting.GRAY))));
        return paper;
    }
    private static final class DisplaySlot extends Slot {
        DisplaySlot(Container container, int slot, int x, int y) { super(container, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
    }
}
