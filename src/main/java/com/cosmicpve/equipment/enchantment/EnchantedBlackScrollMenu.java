package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.registry.ModMenus;
import com.cosmicpve.reward.RewardDeliveryService;
import java.util.List;
import net.minecraft.ChatFormatting;
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

public final class EnchantedBlackScrollMenu extends AbstractContainerMenu {
    public static final Component TITLE = Component.literal("SELECT AN ENCHANTMENT TO BLACKSCROLL");
    private final SimpleContainer display;
    private final Player owner;
    private final int rows;
    private final EnchantedBlackScrollExtractionService service = new EnchantedBlackScrollExtractionService();
    private List<VisibleCosmicEnchantmentOrdering.Entry> candidates = List.of();
    private ItemStack target = ItemStack.EMPTY;
    private ItemStack scroll = ItemStack.EMPTY;
    private boolean settled;

    public EnchantedBlackScrollMenu(int id, Inventory inventory, int rows, ItemStack target, ItemStack scroll) {
        super(ModMenus.ENCHANTED_BLACK_SCROLL.get(), id);
        this.owner = inventory.player;
        this.rows = Math.max(1, Math.min(2, rows));
        this.display = new SimpleContainer(this.rows * 9);
        this.target = target;
        this.scroll = scroll;
        this.candidates = service.candidates(target);
        populate();
        for (int slot = 0; slot < display.getContainerSize(); slot++) {
            addSlot(new LockedSlot(display, slot, 8 + (slot % 9) * 18, 18 + (slot / 9) * 18));
        }
    }

    public static EnchantedBlackScrollMenu client(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        return new EnchantedBlackScrollMenu(id, inventory, buffer.readVarInt(), ItemStack.EMPTY, ItemStack.EMPTY);
    }
    public int rows() { return rows; }

    private void populate() {
        display.setItem(0, target.copy());
        for (int i = 0; i < candidates.size() && i + 1 < display.getContainerSize(); i++) {
            var entry = candidates.get(i);
            ItemStack preview = new ItemStack(Items.BOOK);
            preview.set(DataComponents.CUSTOM_NAME, Component.translatable("enchantment." + entry.id().getNamespace()
                    + "." + entry.id().getPath()).append(" ").append(Component.translatable("enchantment.level." + entry.level()))
                    .withColor(entry.spec().tier().tooltipColor()));
            int success = scroll.get(com.cosmicpve.registry.ModDataComponents.ENCHANTED_BLACK_SCROLL.get()) == null ? 0
                    : scroll.get(com.cosmicpve.registry.ModDataComponents.ENCHANTED_BLACK_SCROLL.get()).returnedSuccessRate();
            preview.set(DataComponents.LORE, new ItemLore(List.of(
                    Component.translatable("tooltip.cosmicpve.enchanted_black_scroll.preview_success", success)
                            .withColor(ItemApplicationColors.SUCCESS),
                    Component.translatable("tooltip.cosmicpve.enchanted_black_scroll.preview_destroy")
                            .withStyle(ChatFormatting.GRAY))));
            display.setItem(i + 1, preview);
        }
    }

    @Override public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (button == 0 && clickType == ClickType.PICKUP && slotId > 0 && slotId <= candidates.size()
                && player instanceof ServerPlayer serverPlayer) {
            choose(serverPlayer, slotId - 1);
        }
    }

    private void choose(ServerPlayer player, int index) {
        if (settled || index < 0 || index >= candidates.size()) return;
        ItemStack book = service.extract(target, scroll, candidates.get(index), player.getRandom().nextInt(100) + 1);
        if (book.isEmpty()) return;
        settled = true;
        display.clearContent();
        new RewardDeliveryService().deliver(player, List.of(target, book));
        target = ItemStack.EMPTY;
        scroll = ItemStack.EMPTY;
        ItemApplicationFeedback.play(player, ItemApplicationFeedback.Cue.SUCCESS);
        player.closeContainer();
    }

    @Override public void removed(Player player) {
        if (!settled && player instanceof ServerPlayer serverPlayer) {
            settled = true;
            new RewardDeliveryService().deliver(serverPlayer, List.of(target, scroll));
            target = ItemStack.EMPTY; scroll = ItemStack.EMPTY;
        }
        super.removed(player);
    }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) { return player == owner; }

    private static final class LockedSlot extends Slot {
        LockedSlot(Container container, int slot, int x, int y) { super(container, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
    }
}
