package com.cosmicpve.equipment.armor;

import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Server-thread-only, single-owner escrow; removing a transaction claims its crystal exactly once. */
public final class ArmorCrystalConfirmationService {
    public static final ArmorCrystalConfirmationService INSTANCE = new ArmorCrystalConfirmationService();
    public static final int TIMEOUT_TICKS = 600;
    private final Map<UUID, Pending> pending = new HashMap<>();
    private record Pending(Container container, int slot, ItemStack identity, ItemStack fingerprint,
                           ItemStack crystal, long deadline) {}

    public boolean begin(ServerPlayer player, ItemStack source, Slot slot) {
        cancel(player, false);
        ItemStack target = slot.getItem();
        var data = source.get(ModDataComponents.ARMOR_SET_CRYSTAL.get());
        if (!source.is(ModItems.ARMOR_SET_CRYSTAL.get()) || data == null
                || !ArmorSetResolver.isArmor(target) || !target.has(ModDataComponents.ARMOR_SET_ID.get())
                || CosmicContent.repository().findArmorSetDefinition(data.identity().setId()).isEmpty()) return false;
        ItemStack escrow = source.copyWithCount(1);
        source.shrink(1);
        pending.put(player.getUUID(), new Pending(slot.container, slot.getContainerSlot(), target,
                target.copy(), escrow, player.level().getServer().getTickCount() + TIMEOUT_TICKS));
        player.sendSystemMessage(Component.literal(target.getHoverName().getString()
                + " already has an armor set identity! Type \"confirm\" if you wish to apply the crystal!"));
        return true;
    }

    private boolean valid(ServerPlayer player, Pending transaction) {
        ItemStack current = transaction.container().getItem(transaction.slot());
        return player.isAlive() && transaction.container().stillValid(player)
                && current == transaction.identity()
                && ItemStack.matches(current, transaction.fingerprint());
    }

    public boolean chat(ServerPlayer player, String message) {
        Pending transaction = pending.remove(player.getUUID());
        if (transaction == null) return false;
        boolean confirm = isConfirmation(message);
        if (!confirm || player.level().getServer().getTickCount() >= transaction.deadline() || !valid(player, transaction)) {
            giveBack(player, transaction.crystal());
            player.sendSystemMessage(Component.literal("Armor crystal application canceled."));
            return confirm;
        }
        var service = new ArmorCrystalApplicationService(CosmicContent.repository(), () -> player.getRandom().nextInt(100) + 1);
        var outcome = service.applyConfirmedReplacement(transaction.crystal(), transaction.identity(),
                transaction.container().getItem(transaction.slot()));
        transaction.container().setChanged();
        player.containerMenu.broadcastChanges();
        if (!transaction.crystal().isEmpty()) giveBack(player, transaction.crystal());
        ArmorCrystalFeedback.play(player, outcome);
        player.sendSystemMessage(Component.literal(switch (outcome) {
            case SUCCESS -> "Armor set identity replaced.";
            case FAILED_PROTECTED -> "Crystal failed; White Scroll protected your armor.";
            case FAILED_DESTROYED -> "Crystal failed and destroyed your armor.";
            default -> "Armor crystal application canceled.";
        }));
        return true;
    }

    public static boolean isConfirmation(String message) { return message.trim().equalsIgnoreCase("confirm"); }

    public void tick(ServerPlayer player) {
        Pending transaction = pending.get(player.getUUID());
        if (transaction == null) return;
        boolean expired = player.level().getServer().getTickCount() >= transaction.deadline();
        if (expired || !valid(player, transaction)) cancel(player, expired);
    }

    public void cancel(ServerPlayer player, boolean expired) {
        Pending transaction = pending.remove(player.getUUID());
        if (transaction == null) return;
        giveBack(player, transaction.crystal());
        player.sendSystemMessage(Component.literal(expired ? "Armor crystal confirmation expired." : "Armor crystal application canceled."));
    }

    private static void giveBack(ServerPlayer player, ItemStack crystal) {
        // Inventory.add deliberately deletes overflow for creative players; escrow must never do that.
        player.getInventory().placeItemBackInInventory(crystal);
        player.containerMenu.broadcastChanges();
    }
}
