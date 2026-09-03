package com.cosmicpve.enchanter;

import com.cosmicpve.economy.RawExperienceService;
import com.cosmicpve.equipment.enchantment.UnexaminedBooks;
import com.cosmicpve.registry.ModMenus;
import com.cosmicpve.reward.RewardDeliveryService;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
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

public final class EnchanterMenu extends AbstractContainerMenu {
    public static final int SLOT_COUNT = 9;
    private final SimpleContainer display = new SimpleContainer(SLOT_COUNT);
    private final Player owner;
    private final RawExperienceService experience = new RawExperienceService();
    private final RewardDeliveryService delivery = new RewardDeliveryService();

    public EnchanterMenu(int id, Inventory inventory) {
        super(ModMenus.ENCHANTER.get(), id);
        owner = inventory.player;
        for (int slot = 0; slot < SLOT_COUNT; slot++)
            addSlot(new DisplaySlot(display, slot, 8 + slot * 18, 18));
        addStandardInventorySlots(inventory, 8, 49);
        refresh();
    }

    public static EnchanterMenu client(int id, Inventory inventory, RegistryFriendlyByteBuf ignored) {
        return new EnchanterMenu(id, inventory);
    }

    public void refresh() {
        int current = owner instanceof ServerPlayer player ? experience.balance(player) : Math.max(0, owner.totalExperience);
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            EnchanterOffer offer = EnchanterOffer.at(slot);
            display.setItem(slot, offer == null ? background() : offer(offer, current));
        }
        broadcastChanges();
    }

    @Override public void clicked(int slot, int button, ClickType type, Player player) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            super.clicked(slot, button, type, player);
            return;
        }
        if (button != 0 || type != ClickType.PICKUP || !(player instanceof ServerPlayer serverPlayer)) return;
        EnchanterOffer offer = EnchanterOffer.at(slot);
        if (offer == null) return;
        int current = experience.balance(serverPlayer);
        var outcome = EnchanterPurchaseTransaction.execute(current, offer.cost(),
                () -> UnexaminedBooks.create(offer.tier()),
                cost -> experience.subtract(serverPlayer, cost),
                reward -> delivery.deliver(serverPlayer, List.of(reward)));
        if (outcome == EnchanterPurchaseTransaction.Outcome.SUCCESS) {
            serverPlayer.connection.send(new ClientboundSoundPacket(
                    net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.EXPERIENCE_ORB_PICKUP),
                    SoundSource.PLAYERS, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    1.0F, 1.0F, serverPlayer.getRandom().nextLong()));
            refresh();
        } else if (outcome == EnchanterPurchaseTransaction.Outcome.INSUFFICIENT_XP) {
            serverPlayer.sendSystemMessage(Component.literal("You need " + grouped(offer.cost())
                    + " XP to purchase this book.").withColor(0xFF5555));
        }
    }

    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) { return true; }

    static ItemStack offer(EnchanterOffer offer, int currentXp) {
        ItemStack stack = new ItemStack(offer.icon());
        String rarity = titleCase(offer.tier().serializedName());
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(rarity + " Enchantment Book")
                .withStyle(style -> style.withColor(offer.color()).withBold(true)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("UNEXAMINED BOOK").withStyle(style -> style.withColor(0x55FFFF).withBold(true)),
                Component.literal("Cost: ").withColor(0xAAAAAA).append(Component.literal(grouped(offer.cost()) + " XP")
                        .withStyle(style -> style.withColor(0x55FF55).withBold(true))),
                Component.literal("You Have: ").withColor(0xAAAAAA).append(Component.literal(grouped(currentXp) + " XP")
                        .withStyle(style -> style.withColor(0xFFFFFF).withBold(true))),
                Component.literal("Click to purchase one.").withStyle(style -> style.withColor(0xAAAAAA).withItalic(true)))));
        return stack;
    }

    private static ItemStack background() {
        ItemStack stack = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
        stack.set(DataComponents.CUSTOM_NAME, Component.empty());
        return stack;
    }
    private static String grouped(int value) { return NumberFormat.getIntegerInstance(Locale.US).format(value); }
    private static String titleCase(String value) { return Character.toUpperCase(value.charAt(0)) + value.substring(1); }

    private static final class DisplaySlot extends Slot {
        DisplaySlot(Container container, int slot, int x, int y) { super(container, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
    }
}
