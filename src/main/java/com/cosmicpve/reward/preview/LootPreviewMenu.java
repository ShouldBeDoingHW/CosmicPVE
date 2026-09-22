package com.cosmicpve.reward.preview;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** All slots are inert, including inventory and outside clicks; only explicit navigation changes pages. */
public final class LootPreviewMenu extends ChestMenu {
    public static final int PAGED_CONTENT_SLOTS = 45;
    private final ServerPlayer owner;
    private final Component name;
    private final List<ItemStack> outcomes;
    private final int page;
    private LootPreviewMenu(int id, Inventory inventory, ServerPlayer owner, Component name,
                            List<ItemStack> outcomes, int page, SimpleContainer display) {
        super(MenuType.GENERIC_9x6, id, inventory, display, 6);
        this.owner = owner; this.name = name; this.outcomes = outcomes; this.page = page;
        int capacity = outcomes.size() <= 54 ? 54 : PAGED_CONTENT_SLOTS;
        int offset = page * capacity;
        for (int i = 0; i < capacity && offset + i < outcomes.size(); i++) display.setItem(i, outcomes.get(offset + i).copy());
        if (pages(outcomes.size()) > 1) {
            if (page > 0) display.setItem(45, arrow("Previous Page"));
            if (page + 1 < pages(outcomes.size())) display.setItem(53, arrow("Next Page"));
        }
    }
    private static ItemStack arrow(String label) {
        ItemStack arrow = new ItemStack(Items.ARROW);
        arrow.set(DataComponents.CUSTOM_NAME, Component.literal(label));
        return arrow;
    }
    public static int pages(int count) { return count <= 54 ? 1 : (count + PAGED_CONTENT_SLOTS - 1) / PAGED_CONTENT_SLOTS; }
    public static void open(ServerPlayer player, Component name, List<ItemStack> outcomes, int page) {
        int pages = pages(outcomes.size());
        if (outcomes.isEmpty() || page < 0 || page >= pages) return;
        var title = name.copy().append(Component.literal(" Preview" + (pages > 1 ? " " + (page + 1) + "/" + pages : ""))
                .withStyle(style -> style.withColor(0xFFFFFF).withBold(false).withItalic(false).withUnderlined(false)));
        var snapshot = outcomes.stream().map(ItemStack::copy).toList();
        player.openMenu(new SimpleMenuProvider((id, inventory, ignored) ->
                new LootPreviewMenu(id, inventory, player, name, snapshot, page, new SimpleContainer(54)), title));
    }
    public static boolean openHeld(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof LootPreviewProvider provider)) return false;
        var outcomes = provider.previewOutcomes(player, held);
        if (outcomes.isEmpty()) return false;
        open(player, provider.previewTitle(held), outcomes, 0);
        return true;
    }
    @Override public void clicked(int slot, int button, ClickType type, Player player) {
        if (player != owner || button != 0 || type != ClickType.PICKUP || pages(outcomes.size()) <= 1) return;
        if (slot == 45 && page > 0) open(owner, name, outcomes, page - 1);
        if (slot == 53 && page + 1 < pages(outcomes.size())) open(owner, name, outcomes, page + 1);
    }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) { return player == owner && owner.isAlive(); }
}
