package com.cosmicpve.adventure;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

/** Read-only 27-slot informational surface shared by all public aliases. */
public final class AdventureInfoMenu extends ChestMenu {
    public static final Component TITLE = Component.literal("Adventures").withColor(0x43B03C);
    public static final List<Integer> ENTRY_SLOTS = List.of(11, 13, 15);
    private final SimpleContainer display;
    public AdventureInfoMenu(int id, Inventory inventory) { this(id, inventory, new SimpleContainer(27)); }
    private AdventureInfoMenu(int id, Inventory inventory, SimpleContainer display) {
        super(com.cosmicpve.registry.ModMenus.ADVENTURES.get(), id, inventory, display, 3);
        this.display = display;
        for (int i=0;i<AdventureDefinition.ALL.size();i++) display.setItem(ENTRY_SLOTS.get(i), icon(AdventureDefinition.ALL.get(i)));
    }
    public static AdventureInfoMenu client(int id, Inventory inventory, RegistryFriendlyByteBuf ignored) {
        return new AdventureInfoMenu(id, inventory);
    }
    public static ItemStack icon(AdventureDefinition definition) {
        var stack = new ItemStack(definition.icon());
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(definition.name())
                .withStyle(s -> s.withColor(definition.color()).withBold(true).withItalic(false)));
        stack.set(DataComponents.LORE, new ItemLore(definition.lore().stream()
                .<Component>map(line -> Component.literal(line).withStyle(s -> s.withColor(0xAAAAAA).withItalic(false))).toList()));
        return stack;
    }
    public ItemStack displayedItem(int slot) { return display.getItem(slot); }
    @Override public void clicked(int slot, int button, ClickType type, Player player) {}
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) { return true; }
}
