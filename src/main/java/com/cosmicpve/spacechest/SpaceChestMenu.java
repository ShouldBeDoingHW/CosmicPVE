package com.cosmicpve.spacechest;

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

public final class SpaceChestMenu extends AbstractContainerMenu {
    private final SimpleContainer display = new SimpleContainer(SpaceChestSessionService.SLOT_COUNT);
    private final Player owner;
    private final SpaceChestTier tier;

    public SpaceChestMenu(int id, Inventory inventory, SpaceChestTier tier) {
        super(ModMenus.SPACE_CHEST.get(), id);
        this.owner = inventory.player;
        this.tier = tier;
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) {
            int index = column + row * 9;
            addSlot(new DisplaySlot(display, index, 8 + column * 18, 18 + row * 18));
        }
        addStandardInventorySlots(inventory, 8, 85);
        refresh();
    }

    public static SpaceChestMenu client(int id, Inventory inventory, RegistryFriendlyByteBuf data) {
        return new SpaceChestMenu(id, inventory, data.readEnum(SpaceChestTier.class));
    }

    public SpaceChestTier tier() { return tier; }

    public void refresh() {
        if (!(owner instanceof ServerPlayer player)) return;
        var service = SpaceChestSessionService.INSTANCE;
        SpaceChestSession session = service.session(player);
        if (session == null) return;
        SpaceChestRuntime runtime = service.runtime(player);
        for (int slot = 0; slot < SpaceChestSessionService.SLOT_COUNT; slot++) {
            ItemStack shown;
            if (session.phase() == SpaceChestPhase.SELECTING) {
                shown = pane(session.selectedSlots().contains(slot) ? Items.WHITE_STAINED_GLASS_PANE : tierPane(tier),
                        session.selectedSlots().contains(slot) ? "Selected" : "???");
            } else {
                int currentSlot = slot;
                var reward = session.committedRewards().stream().filter(entry -> entry.slot() == currentSlot).findFirst();
                if (reward.isPresent()) {
                    var selectedReward = reward.orElseThrow();
                    shown = runtime != null && runtime.revealFinished && selectedReward.delivered()
                            ? preview(selectedReward.items())
                            : pane(Items.WHITE_STAINED_GLASS_PANE, runtime != null && runtime.revealFinished
                                    ? "Click to reveal reward" : "Selected");
                } else if (runtime == null || runtime.revealFinished) {
                    shown = ItemStack.EMPTY;
                } else {
                    int missedOrdinal = missedOrdinal(session.selectedSlots(), slot);
                    int visible = SpaceChestSessionTransitions.visibleMissedRewards(runtime.revealTicks);
                    shown = missedOrdinal >= 0 && missedOrdinal < visible
                            ? preview(runtime.rewards.get(slot)) : pane(Items.GRAY_STAINED_GLASS_PANE, "Revealing...");
                }
            }
            display.setItem(slot, shown);
        }
    }

    @Override public void broadcastChanges() {
        if (owner instanceof ServerPlayer player) SpaceChestSessionService.INSTANCE.tick(player, this);
        refresh();
        super.broadcastChanges();
    }

    @Override public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < SpaceChestSessionService.SLOT_COUNT
                && button == 0 && clickType == ClickType.PICKUP) {
            if (player instanceof ServerPlayer serverPlayer) {
                var session = SpaceChestSessionService.INSTANCE.session(serverPlayer);
                if (session != null && session.phase() == SpaceChestPhase.SELECTING)
                    SpaceChestSessionService.INSTANCE.select(serverPlayer, slotId, this);
                else SpaceChestSessionService.INSTANCE.claim(serverPlayer, slotId, this);
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) { return true; }

    @Override public void removed(Player player) {
        super.removed(player);
        if (player instanceof ServerPlayer serverPlayer) SpaceChestSessionService.INSTANCE.closed(serverPlayer);
    }

    private static int missedOrdinal(List<Integer> selected, int target) {
        if (selected.contains(target)) return -1;
        int ordinal = 0;
        for (int slot = 0; slot < target; slot++) if (!selected.contains(slot)) ordinal++;
        return ordinal;
    }

    private static ItemStack pane(net.minecraft.world.item.Item item, String label) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(label));
        return stack;
    }

    private static net.minecraft.world.item.Item tierPane(SpaceChestTier tier) {
        return switch (tier) {
            case ULTIMATE -> Items.YELLOW_STAINED_GLASS_PANE;
            case LEGENDARY -> Items.ORANGE_STAINED_GLASS_PANE;
            case MASTERY -> Items.RED_STAINED_GLASS_PANE;
        };
    }

    static ItemStack preview(List<ItemStack> bundle) {
        if (bundle.isEmpty()) return pane(Items.BARRIER, "Unavailable reward");
        ItemStack stack = bundle.getFirst().copy();
        boolean homogeneous = bundle.stream().allMatch(item -> ItemStack.isSameItemSameComponents(stack, item));
        if (homogeneous) {
            stack.setCount(Math.min(stack.getMaxStackSize(), bundle.stream().mapToInt(ItemStack::getCount).sum()));
        }
        if (bundle.size() > 1) stack.set(DataComponents.LORE,
                new ItemLore(List.of(Component.literal("Bundle contains " + bundle.size() + " item stacks"))));
        return stack;
    }

    private static final class DisplaySlot extends Slot {
        DisplaySlot(Container container, int slot, int x, int y) { super(container, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
    }
}
