package com.cosmicpve.vkit;

import com.cosmicpve.registry.ModMenus;
import java.util.List;
import java.util.OptionalInt;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

/** Read-only, current-player V-Kit progression and possible-reward presentation. */
public final class VKitInfoMenu extends AbstractContainerMenu {
    public static final int SLOT_COUNT = 9;
    public static final int[] KIT_SLOTS = {1, 3, 5, 7};
    public static final Component TITLE = Component.literal("V-KITS")
            .withStyle(style -> style.withColor(0xFFAA00).withBold(true));
    public static final List<VKitDefinition> DISPLAY_ORDER = List.of(
            VKitDefinition.PHOENIX, VKitDefinition.OGRE, VKitDefinition.SLAYER, VKitDefinition.JUDGEMENT);
    private static final List<Item> ICONS = List.of(Items.BLAZE_POWDER, Items.SLIME_BALL, Items.ECHO_SHARD, Items.HEAVY_CORE);
    private final SimpleContainer display = new SimpleContainer(SLOT_COUNT);
    private final Player owner;

    private VKitInfoMenu(int id, Inventory inventory) {
        super(ModMenus.VKIT_INFO.get(), id);
        owner = inventory.player;
        for (int slot = 0; slot < SLOT_COUNT; slot++) addSlot(new DisplaySlot(display, slot, 8 + slot * 18, 18));
        for (int slot = 0; slot < SLOT_COUNT; slot++) display.setItem(slot, filler());
    }

    public VKitInfoMenu(int id, Inventory inventory, ServerPlayer player) {
        this(id, inventory);
        var progression = new VKitProgressionService();
        for (int index = 0; index < DISPLAY_ORDER.size(); index++) {
            VKitDefinition definition = DISPLAY_ORDER.get(index);
            display.setItem(KIT_SLOTS[index], icon(definition, ICONS.get(index), progression.level(player, definition)));
        }
    }

    public static VKitInfoMenu client(int id, Inventory inventory, RegistryFriendlyByteBuf ignored) {
        return new VKitInfoMenu(id, inventory);
    }

    public static ItemStack icon(VKitDefinition definition, Item base, int level) {
        if (level < 0 || level > VKitEquipmentGenerator.MAX_KIT_LEVEL)
            throw new IllegalArgumentException("V-Kit level must be 0-10");
        ItemStack icon = new ItemStack(base);
        icon.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        icon.set(DataComponents.CUSTOM_NAME, Component.literal(definition.displayName() + " V-Kit")
                .withStyle(style -> style.withColor(definition.color()).withBold(true).withUnderlined(true)
                        .withItalic(false)));
        var lore = new java.util.ArrayList<Component>();
        lore.add(Component.literal("LEVEL: ").withStyle(style -> style.withColor(0xFFAA00).withBold(true))
                .append(Component.literal(level == 0 ? "LOCKED" : VKitEquipmentGenerator.roman(level))
                        .withStyle(style -> style.withColor(level == 0 ? 0xFF5555 : 0xFFFFFF).withBold(true))));
        lore.add(Component.empty());
        lore.add(Component.literal("POSSIBLE REWARDS")
                .withStyle(style -> style.withColor(0x55FFFF).withBold(true)));
        OptionalInt shownLevel = level == 0 ? OptionalInt.empty() : OptionalInt.of(level);
        lore.add(VKitEquipmentGenerator.equipmentName(definition, definition.armor(), shownLevel));
        lore.add(VKitEquipmentGenerator.equipmentName(definition, definition.weapon(), shownLevel));
        lore.add(Component.literal("Each claim awards one of these two items.")
                .withStyle(style -> style.withColor(0xAAAAAA).withItalic(true)));
        icon.set(DataComponents.LORE, new ItemLore(List.copyOf(lore)));
        return icon;
    }

    public static SlotRole role(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) throw new IllegalArgumentException("V-Kit slot must be 0-8");
        for (int kitSlot : KIT_SLOTS) if (slot == kitSlot) return SlotRole.KIT;
        return SlotRole.FILLER;
    }

    ItemStack displayedItem(int slot) { return display.getItem(slot); }

    @Override public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // This menu exposes no mutable player or display slots; ignore every inventory gesture.
    }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) { return owner == player; }

    private static ItemStack filler() {
        ItemStack pane = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
        pane.set(DataComponents.CUSTOM_NAME, Component.literal(" "));
        return pane;
    }

    private static final class DisplaySlot extends Slot {
        private DisplaySlot(Container container, int slot, int x, int y) { super(container, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
    }
    public enum SlotRole { FILLER, KIT }
}
