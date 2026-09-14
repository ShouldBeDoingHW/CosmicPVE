package com.cosmicpve.equipment.accessory;

import com.cosmicpve.data.component.AccessoryLoadout;
import com.cosmicpve.data.component.AccessorySlot;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import java.util.function.IntSupplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Atomic, identity-checked socket/attach/remove operations. */
public final class AccessoryApplicationService {
    public enum SocketOutcome { SUCCESS, FAILED, STALE, INVALID, NOT_CHESTPLATE, ALREADY_SOCKETED }
    public enum AttachOutcome { SUCCESS, STALE, INVALID, NOT_CHESTPLATE, UNSOCKETED, ALREADY_ATTACHED }
    public enum RemoveOutcome { SUCCESS, STALE, INVALID, OUTPUT_BLOCKED }
    public record RemovalResult(RemoveOutcome outcome, ItemStack returnedAmulet) {}

    public SocketOutcome socket(ItemStack carried, ItemStack target, ItemStack currentCarried,
            ItemStack currentTarget, IntSupplier roll) {
        if (carried != currentCarried || target != currentTarget) return SocketOutcome.STALE;
        var data = carried.get(ModDataComponents.ACCESSORY_SOCKET.get());
        if (!carried.is(ModItems.AMULET_SOCKET.get()) || data == null || !data.valid()
                || data.slot() != AccessorySlot.AMULET) return SocketOutcome.INVALID;
        if (!isChestplate(target)) return SocketOutcome.NOT_CHESTPLATE;
        var loadout = loadout(target);
        if (loadout.socketed(AccessorySlot.AMULET)) return SocketOutcome.ALREADY_SOCKETED;
        boolean success = data.successRate() == 100 || normalizeRoll(roll.getAsInt()) <= data.successRate();
        carried.shrink(1);
        if (!success) return SocketOutcome.FAILED;
        target.set(ModDataComponents.ACCESSORY_LOADOUT.get(), loadout.withSocket(AccessorySlot.AMULET));
        return SocketOutcome.SUCCESS;
    }

    public AttachOutcome attach(ItemStack carried, ItemStack target, ItemStack currentCarried, ItemStack currentTarget) {
        if (carried != currentCarried || target != currentTarget) return AttachOutcome.STALE;
        var item = carried.get(ModDataComponents.ACCESSORY_ITEM.get());
        if (item == null || !item.valid() || item.slot() != AccessorySlot.AMULET
                || AmuletDefinition.find(item.accessoryId()).isEmpty()) return AttachOutcome.INVALID;
        if (!isChestplate(target)) return AttachOutcome.NOT_CHESTPLATE;
        var loadout = loadout(target);
        if (!loadout.socketed(AccessorySlot.AMULET)) return AttachOutcome.UNSOCKETED;
        if (loadout.attached(AccessorySlot.AMULET).isPresent()) return AttachOutcome.ALREADY_ATTACHED;
        target.set(ModDataComponents.ACCESSORY_LOADOUT.get(),
                loadout.withAttachment(AccessorySlot.AMULET, item.accessoryId()));
        carried.shrink(1);
        return AttachOutcome.SUCCESS;
    }

    public RemovalResult remove(ItemStack target, ItemStack currentTarget, boolean cursorEmpty) {
        if (target != currentTarget) return new RemovalResult(RemoveOutcome.STALE, ItemStack.EMPTY);
        var loadout = target.get(ModDataComponents.ACCESSORY_LOADOUT.get());
        if (loadout == null || !loadout.valid()) return new RemovalResult(RemoveOutcome.INVALID, ItemStack.EMPTY);
        var definition = loadout.attached(AccessorySlot.AMULET).flatMap(AmuletDefinition::find);
        if (definition.isEmpty()) return new RemovalResult(RemoveOutcome.INVALID, ItemStack.EMPTY);
        if (!cursorEmpty) return new RemovalResult(RemoveOutcome.OUTPUT_BLOCKED, ItemStack.EMPTY);
        target.set(ModDataComponents.ACCESSORY_LOADOUT.get(), loadout.withoutAttachment(AccessorySlot.AMULET));
        return new RemovalResult(RemoveOutcome.SUCCESS, AmuletItemFactory.create(definition.orElseThrow()));
    }

    public static boolean isChestplate(ItemStack stack) {
        var equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.slot() != EquipmentSlot.CHEST) return false;
        if (stack.is(ItemTags.CHEST_ARMOR)) return true;
        // The attribute fallback recognizes data-driven/custom armor when tags are unavailable during early bootstrap,
        // while still excluding Elytra and arbitrary chest-slot items.
        return stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY).modifiers().stream()
                .anyMatch(entry -> entry.attribute().equals(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR)
                        && entry.slot().test(EquipmentSlot.CHEST) && entry.modifier().amount() > 0);
    }
    private static AccessoryLoadout loadout(ItemStack target) {
        var existing = target.get(ModDataComponents.ACCESSORY_LOADOUT.get());
        return existing != null && existing.valid() ? existing : AccessoryLoadout.empty();
    }
    private static int normalizeRoll(int roll) {
        if (roll < 1 || roll > 100) throw new IllegalArgumentException("Socket roll must be in [1,100]");
        return roll;
    }
}
