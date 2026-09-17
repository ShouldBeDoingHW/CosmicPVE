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
    public enum SocketOutcome { SUCCESS, FAILED, STALE, INVALID, NOT_CHESTPLATE, NOT_LEGGINGS, NOT_SUPPORTED_ARMOR, ALREADY_SOCKETED }
    public enum AttachOutcome { SUCCESS, STALE, INVALID, NOT_CHESTPLATE, NOT_LEGGINGS, UNSOCKETED, ALREADY_ATTACHED }
    public enum RemoveOutcome { SUCCESS, STALE, INVALID, OUTPUT_BLOCKED }
    public record RemovalResult(RemoveOutcome outcome, ItemStack returnedAmulet) {}

    public SocketOutcome socket(ItemStack carried, ItemStack target, ItemStack currentCarried,
            ItemStack currentTarget, IntSupplier roll) {
        if (carried != currentCarried || target != currentTarget) return SocketOutcome.STALE;
        AccessorySlot slot;
        int successRate;
        var data = carried.get(ModDataComponents.ACCESSORY_SOCKET.get());
        if (carried.is(ModItems.OMNI_SOCKET.get())) {
            var omniRate = carried.get(ModDataComponents.OMNI_SOCKET_SUCCESS.get());
            if (omniRate == null || omniRate < 1 || omniRate > 100) return SocketOutcome.INVALID;
            if (isChestplate(target)) slot = AccessorySlot.AMULET;
            else if (isLeggings(target)) slot = AccessorySlot.BELT;
            else return SocketOutcome.NOT_SUPPORTED_ARMOR;
            successRate = omniRate;
        } else {
            if (data == null || !data.valid()) return SocketOutcome.INVALID;
            slot = data.slot(); successRate = data.successRate();
            if (slot == AccessorySlot.AMULET && (!carried.is(ModItems.AMULET_SOCKET.get()) || !isChestplate(target)))
                return SocketOutcome.NOT_CHESTPLATE;
            if (slot == AccessorySlot.BELT && (!carried.is(ModItems.BELT_SOCKET.get()) || !isLeggings(target)))
                return SocketOutcome.NOT_LEGGINGS;
        }
        var loadout = loadout(target);
        if (loadout.socketed(slot)) return SocketOutcome.ALREADY_SOCKETED;
        boolean success = successRate == 100 || normalizeRoll(roll.getAsInt()) <= successRate;
        carried.shrink(1);
        if (!success) return SocketOutcome.FAILED;
        target.set(ModDataComponents.ACCESSORY_LOADOUT.get(), loadout.withSocket(slot));
        return SocketOutcome.SUCCESS;
    }

    public AttachOutcome attach(ItemStack carried, ItemStack target, ItemStack currentCarried, ItemStack currentTarget) {
        if (carried != currentCarried || target != currentTarget) return AttachOutcome.STALE;
        var item = carried.get(ModDataComponents.ACCESSORY_ITEM.get());
        if (item == null || !item.valid() || !known(item.slot(), item.accessoryId())) return AttachOutcome.INVALID;
        if (item.slot() == AccessorySlot.AMULET && !isChestplate(target)) return AttachOutcome.NOT_CHESTPLATE;
        if (item.slot() == AccessorySlot.BELT && !isLeggings(target)) return AttachOutcome.NOT_LEGGINGS;
        var loadout = loadout(target);
        if (!loadout.socketed(item.slot())) return AttachOutcome.UNSOCKETED;
        if (loadout.attached(item.slot()).isPresent()) return AttachOutcome.ALREADY_ATTACHED;
        target.set(ModDataComponents.ACCESSORY_LOADOUT.get(),
                loadout.withAttachment(item.slot(), item.accessoryId()));
        carried.shrink(1);
        return AttachOutcome.SUCCESS;
    }

    public RemovalResult remove(ItemStack target, ItemStack currentTarget, boolean cursorEmpty) {
        if (target != currentTarget) return new RemovalResult(RemoveOutcome.STALE, ItemStack.EMPTY);
        var loadout = target.get(ModDataComponents.ACCESSORY_LOADOUT.get());
        if (loadout == null || !loadout.valid()) return new RemovalResult(RemoveOutcome.INVALID, ItemStack.EMPTY);
        AccessorySlot slot = isChestplate(target) ? AccessorySlot.AMULET
                : isLeggings(target) ? AccessorySlot.BELT : null;
        if (slot == null) return new RemovalResult(RemoveOutcome.INVALID, ItemStack.EMPTY);
        var id = loadout.attached(slot).orElse(null);
        if (id == null || !known(slot, id)) return new RemovalResult(RemoveOutcome.INVALID, ItemStack.EMPTY);
        if (!cursorEmpty) return new RemovalResult(RemoveOutcome.OUTPUT_BLOCKED, ItemStack.EMPTY);
        target.set(ModDataComponents.ACCESSORY_LOADOUT.get(), loadout.withoutAttachment(slot));
        return new RemovalResult(RemoveOutcome.SUCCESS, slot == AccessorySlot.AMULET
                ? AmuletItemFactory.create(AmuletDefinition.find(id).orElseThrow())
                : BeltItemFactory.create(BeltDefinition.find(id).orElseThrow()));
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
    public static boolean isLeggings(ItemStack stack) {
        var equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.slot() != EquipmentSlot.LEGS) return false;
        if (stack.is(ItemTags.LEG_ARMOR)) return true;
        return stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY).modifiers().stream()
                .anyMatch(entry -> entry.attribute().equals(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR)
                        && entry.slot().test(EquipmentSlot.LEGS) && entry.modifier().amount() > 0);
    }
    private static boolean known(AccessorySlot slot, net.minecraft.resources.Identifier id) {
        return slot == AccessorySlot.AMULET ? AmuletDefinition.find(id).isPresent() : BeltDefinition.find(id).isPresent();
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
