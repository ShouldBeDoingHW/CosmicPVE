package com.cosmicpve.economy.fame;

import com.cosmicpve.economy.FameService;
import com.cosmicpve.registry.ModMenus;
import java.util.ArrayList;
import java.util.List;
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

public final class FameShopMenu extends AbstractContainerMenu {
    public static final int SLOT_COUNT = 9;
    public static final Component TITLE = Component.literal("FAME SHOP")
            .withStyle(style -> style.withColor(FameService.COLOR).withBold(true));
    private static final FameShopService SHOP = new FameShopService();
    private final SimpleContainer display = new SimpleContainer(SLOT_COUNT);
    private final Player owner;
    public FameShopMenu(int id, Inventory inventory) {
        super(ModMenus.FAME_SHOP.get(), id); owner = inventory.player;
        for (int slot = 0; slot < SLOT_COUNT; slot++) addSlot(new DisplaySlot(display, slot, 8 + slot * 18, 18));
        refresh();
    }
    public static FameShopMenu client(int id, Inventory inventory, RegistryFriendlyByteBuf ignored) { return new FameShopMenu(id, inventory); }
    public void refresh() {
        if (!(owner instanceof ServerPlayer player)) return;
        FameShopCatalog catalog = SHOP.catalog(player);
        long balance = new FameService().balance(player);
        long days = Math.max(0L, catalog.nextRefreshDay() - FameShopService.currentDay(player));
        for (int slot = 0; slot < SLOT_COUNT; slot++) display.setItem(slot,
                catalog.offers().get(slot).purchased() ? purchased(catalog.offers().get(slot))
                        : preview(catalog.offers().get(slot), balance, days));
    }
    @Override public void clicked(int slot, int button, ClickType type, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || slot < 0 || slot >= SLOT_COUNT
                || button != 0 || type != ClickType.PICKUP) return;
        FameShopService.PurchaseResult result = SHOP.purchase(serverPlayer, slot);
        switch (result) {
            case SUCCESS -> {
                serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                        SoundSource.PLAYERS, 1.0F, 1.0F);
                serverPlayer.sendSystemMessage(Component.literal("PURCHASED!")
                        .withStyle(style -> style.withColor(FameService.COLOR).withBold(true)));
                serverPlayer.sendSystemMessage(Component.literal("Fame: ").withColor(0xAAAAAA)
                        .append(Component.literal(Long.toString(new FameService().balance(serverPlayer)))
                                .withStyle(style -> style.withColor(FameService.COLOR).withBold(true))));
            }
            case INSUFFICIENT -> {
                long missing = Math.max(0L, SHOP.catalog(serverPlayer).offers().get(slot).price()
                        - new FameService().balance(serverPlayer));
                serverPlayer.sendSystemMessage(Component.literal("You need " + missing + " Fame to purchase this.").withColor(0xFF5555));
            }
            case EXPIRED -> serverPlayer.sendSystemMessage(Component.literal("Your Fame Shop refreshed.").withColor(FameService.COLOR));
            case PURCHASED, INVALID -> {}
        }
        refresh();
    }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) { return owner == player; }

    static ItemStack preview(FameShopOffer offer, long balance, long refreshDays) {
        ItemStack shown = offer.payload().getFirst().copy();
        var lore = new ArrayList<>(shown.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines());
        lore.add(Component.literal("SOURCE: " + offer.sourceTier().displayName().toUpperCase())
                .withColor(offer.sourceTier().color()));
        lore.add(Component.literal("PRICE: " + offer.price() + " FAME")
                .withStyle(style -> style.withColor(FameService.COLOR).withBold(true)));
        lore.add(Component.literal("YOU HAVE: ").withColor(0xAAAAAA)
                .append(Component.literal(Long.toString(balance)).withColor(FameService.COLOR)));
        int quantity = offer.payload().stream().mapToInt(ItemStack::getCount).sum();
        if (quantity > 1 || offer.payload().size() > 1) lore.add(Component.literal("QUANTITY: " + quantity).withColor(0xAAAAAA));
        lore.add(Component.literal("Refresh in " + refreshDays + " Minecraft days").withColor(0x777777));
        lore.add(Component.literal("Click to purchase.").withStyle(style -> style.withColor(0xAAAAAA).withItalic(true)));
        shown.set(DataComponents.LORE, new ItemLore(List.copyOf(lore)));
        return shown;
    }
    private static ItemStack purchased(FameShopOffer offer) {
        ItemStack pane = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        pane.set(DataComponents.CUSTOM_NAME, Component.literal("PURCHASED")
                .withStyle(style -> style.withColor(0xFF5555).withBold(true)));
        pane.set(DataComponents.LORE, new ItemLore(List.of(Component.literal(offer.payload().getFirst().getHoverName().getString()).withColor(0xAAAAAA))));
        return pane;
    }
    private static final class DisplaySlot extends Slot {
        private DisplaySlot(Container container, int slot, int x, int y) { super(container, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
    }
}
