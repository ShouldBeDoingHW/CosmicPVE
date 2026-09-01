package com.cosmicpve.economy.flashsale;

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

/** Read-only authoritative preview of the reward represented by the active Flash Sale. */
public final class FlashSalePreviewMenu extends AbstractContainerMenu {
    public static final int DISPLAY_SLOTS = 9;
    public static final int REWARD_SLOT = 4;
    public static final Component TITLE = Component.literal("FLASH SALE PREVIEW");
    private final SimpleContainer display = new SimpleContainer(DISPLAY_SLOTS);
    private final Player owner;
    private final String expectedEntryId;
    private final long expectedStartTick;

    private FlashSalePreviewMenu(int id, Inventory inventory, String expectedEntryId, long expectedStartTick) {
        super(ModMenus.FLASH_SALE_PREVIEW.get(), id);
        this.owner = inventory.player;
        this.expectedEntryId = expectedEntryId;
        this.expectedStartTick = expectedStartTick;
        for (int column = 0; column < DISPLAY_SLOTS; column++)
            addSlot(new DisplaySlot(display, column, 8 + column * 18, 18));
        addStandardInventorySlots(inventory, 8, 49);
        for (int slot = 0; slot < DISPLAY_SLOTS; slot++) display.setItem(slot, filler());
    }

    public FlashSalePreviewMenu(int id, Inventory inventory, ItemStack reward, int offeredQuantity,
            String expectedEntryId, long expectedStartTick) {
        this(id, inventory, expectedEntryId, expectedStartTick);
        display.setItem(REWARD_SLOT, displayCopy(reward, offeredQuantity));
    }

    public static FlashSalePreviewMenu client(int id, Inventory inventory, RegistryFriendlyByteBuf ignored) {
        return new FlashSalePreviewMenu(id, inventory, "", Long.MIN_VALUE);
    }

    static ItemStack displayCopy(ItemStack reward, int offeredQuantity) {
        ItemStack shown = reward.copy();
        int representable = Math.min(offeredQuantity, shown.getMaxStackSize());
        if (shown.getCount() <= 1 && representable > 1) shown.setCount(representable);
        if (offeredQuantity > shown.getMaxStackSize()) {
            var existing = shown.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines();
            var lines = new java.util.ArrayList<>(existing);
            lines.add(Component.literal("Offered Quantity: " + offeredQuantity)
                    .withStyle(style -> style.withColor(0xFFAA00).withBold(true)));
            shown.set(DataComponents.LORE, new ItemLore(List.copyOf(lines)));
            shown.setCount(1);
        }
        return shown;
    }

    ItemStack displayedReward() { return display.getItem(REWARD_SLOT); }

    @Override public void broadcastChanges() {
        if (owner instanceof ServerPlayer player
                && !FlashSaleRuntime.service().matches(player.level().getServer(), expectedEntryId, expectedStartTick))
            player.closeContainer();
        super.broadcastChanges();
    }
    @Override public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < DISPLAY_SLOTS) return;
        super.clicked(slotId, button, clickType, player);
    }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) {
        return owner == player && (!(player instanceof ServerPlayer serverPlayer)
                || FlashSaleRuntime.service().matches(serverPlayer.level().getServer(), expectedEntryId, expectedStartTick));
    }

    private static ItemStack filler() {
        ItemStack pane = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        pane.set(DataComponents.CUSTOM_NAME, Component.literal(" "));
        return pane;
    }
    private static final class DisplaySlot extends Slot {
        private DisplaySlot(Container container, int slot, int x, int y) { super(container, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
    }
}
