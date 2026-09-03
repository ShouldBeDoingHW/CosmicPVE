package com.cosmicpve.reward.animation;

import com.cosmicpve.registry.ModMenus;
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

public final class SingleRewardAnimationMenu extends AbstractContainerMenu {
    public static final int SLOT_COUNT = 9;
    private final SimpleContainer display = new SimpleContainer(SLOT_COUNT);
    private final Player owner;
    public SingleRewardAnimationMenu(int id, Inventory inventory) {
        super(ModMenus.SINGLE_REWARD_ANIMATION.get(), id);
        owner = inventory.player;
        for (int column = 0; column < SLOT_COUNT; column++) addSlot(new DisplaySlot(display, column, 8 + column * 18, 18));
        addStandardInventorySlots(inventory, 8, 49);
        refresh(0, java.util.List.of(), false);
    }
    public static SingleRewardAnimationMenu client(int id, Inventory inventory, RegistryFriendlyByteBuf ignored) {
        return new SingleRewardAnimationMenu(id, inventory);
    }
    void refresh(int elapsed, java.util.List<ItemStack> previews, boolean revealed) {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack shown;
            if (previews.size() == 1 && slot == 4) shown = previews.getFirst().copy();
            else if (previews.size() == 3 && slot >= 3 && slot <= 5) shown = previews.get(slot - 3).copy();
            else if (slot == 0 || slot == 8) shown = countdown(revealed ? 0 : LootAnimationTimeline.countdown(elapsed));
            else shown = pane(Items.BLACK_STAINED_GLASS_PANE, " ");
            display.setItem(slot, shown);
        }
    }
    @Override public void broadcastChanges() {
        if (owner instanceof ServerPlayer player) SingleRewardAnimationService.INSTANCE.tick(player, this);
        super.broadcastChanges();
    }
    @Override public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < SLOT_COUNT) return;
        super.clicked(slotId, button, clickType, player);
    }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) { return owner == player; }
    @Override public void removed(Player player) {
        super.removed(player);
        if (player instanceof ServerPlayer serverPlayer) SingleRewardAnimationService.INSTANCE.closed(serverPlayer);
    }
    private static ItemStack countdown(int value) {
        ItemStack stack = pane(value == 0 ? Items.GREEN_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE,
                value == 0 ? "Reward revealed!" : Integer.toString(value));
        stack.setCount(Math.max(1, value));
        return stack;
    }
    private static ItemStack pane(net.minecraft.world.item.Item item, String label) {
        ItemStack stack = new ItemStack(item); stack.set(DataComponents.CUSTOM_NAME, Component.literal(label)); return stack;
    }
    private static final class DisplaySlot extends Slot {
        DisplaySlot(Container container, int slot, int x, int y) { super(container, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
    }
}
