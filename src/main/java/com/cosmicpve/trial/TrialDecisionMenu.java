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

public final class TrialDecisionMenu extends AbstractContainerMenu {
    public enum SlotRole { DEAL, BLOCKED_CENTER, NO_DEAL, POT }
    public static final int SLOT_COUNT = 27;
    private final SimpleContainer display = new SimpleContainer(SLOT_COUNT);
    private final Player owner;
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
            else if (slot == 4) display.setItem(slot, pane(Items.WHITE_STAINED_GLASS_PANE, "???", 0xFFFFFF));
            else if (slot < 9) display.setItem(slot, pane(Items.RED_STAINED_GLASS_PANE, "NO DEAL", 0xFF5555));
            else {
                int index = slot - 9;
                display.setItem(slot, session != null && index < session.progress().pot().size()
                        ? preview(session.progress().pot().get(index).items())
                        : pane(Items.WHITE_STAINED_GLASS_PANE, "???", 0xFFFFFF));
            }
        }
    }
    @Override public void clicked(int slot, int button, ClickType type, Player player) {
        if (slot >= 0 && slot < 9 && button == 0 && type == ClickType.PICKUP
                && player instanceof ServerPlayer serverPlayer) {
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
    private static final class DisplaySlot extends Slot {
        DisplaySlot(Container container, int slot, int x, int y) { super(container, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
    }
}
