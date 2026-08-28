package com.cosmicpve.tinkerer;

import com.cosmicpve.equipment.enchantment.CosmicDustService;
import com.cosmicpve.registry.ModMenus;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

public final class TinkererMenu extends AbstractContainerMenu {
    public static final int MENU_SLOTS = 27;
    private final SimpleContainer input = new SimpleContainer(MENU_SLOTS);
    private final Player owner;

    public TinkererMenu(int id, Inventory inventory) {
        super(ModMenus.TINKERER.get(), id);
        owner = inventory.player;
        addSlot(new ConfirmSlot(input, 0, 8, 18));
        for (int slot = 1; slot < MENU_SLOTS; slot++) {
            int row = slot / 9, column = slot % 9;
            addSlot(new BookSlot(input, slot, 8 + column * 18, 18 + row * 18));
        }
        addStandardInventorySlots(inventory, 8, 85);
        input.setItem(0, confirmationPane());
    }

    public static TinkererMenu client(int id, Inventory inventory, RegistryFriendlyByteBuf ignored) {
        return new TinkererMenu(id, inventory);
    }

    @Override public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == 0 && button == 0 && clickType == ClickType.PICKUP) {
            if (player instanceof ServerPlayer serverPlayer) confirm(serverPlayer);
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    public boolean confirm(ServerPlayer player) {
        var result = new TinkererSalvageService().confirm(input, 1, MENU_SLOTS);
        if (!result.succeeded()) return false;
        result.dust().forEach((tier, count) -> {
            int remaining = count;
            while (remaining > 0) {
                int portion = Math.min(64, remaining);
                player.getInventory().placeItemBackInInventory(CosmicDustService.dust(tier, portion));
                remaining -= portion;
            }
        });
        player.level().playSound(null, player.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 1.0F, 1.0F);
        broadcastChanges();
        return true;
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack original = source.copy();
        if (index >= MENU_SLOTS) {
            if (!BookSlot.accepts(source) || !moveItemStackTo(source, 1, MENU_SLOTS, false)) return ItemStack.EMPTY;
        } else if (index > 0) {
            if (!moveItemStackTo(source, MENU_SLOTS, slots.size(), true)) return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;
        if (source.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }

    @Override public boolean stillValid(Player player) { return true; }

    @Override public void removed(Player player) {
        input.setItem(0, ItemStack.EMPTY);
        clearContainer(player, input);
        super.removed(player);
    }

    private static ItemStack confirmationPane() {
        ItemStack stack = new ItemStack(Items.RED_STAINED_GLASS_PANE);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("menu.cosmicpve.tinkerer.confirm"));
        stack.set(DataComponents.LORE, new ItemLore(java.util.List.of(
                Component.translatable("menu.cosmicpve.tinkerer.confirm_lore"))));
        return stack;
    }

    private static final class ConfirmSlot extends Slot {
        ConfirmSlot(Container container, int slot, int x, int y) { super(container, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
    }

    private static final class BookSlot extends Slot {
        BookSlot(Container container, int slot, int x, int y) { super(container, slot, x, y); }
        static boolean accepts(ItemStack stack) { return CosmicDustService.bookTier(stack).isPresent(); }
        @Override public boolean mayPlace(ItemStack stack) { return accepts(stack); }
    }
}
