package com.cosmicpve.equipment.mask;

import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class MaskApplicationService {
    private final com.cosmicpve.content.CosmicContentRepository content;
    public MaskApplicationService() { this(CosmicContent.repository()); }
    public MaskApplicationService(com.cosmicpve.content.CosmicContentRepository content) { this.content = content; }
    public enum ApplyOutcome { SUCCESS, STALE, INVALID, NOT_HELMET, ALREADY_MASKED }
    public enum RemoveOutcome { SUCCESS, STALE, INVALID, OUTPUT_BLOCKED }
    public record RemovalResult(RemoveOutcome outcome, ItemStack returnedMask) {}

    public ApplyOutcome apply(ItemStack carried, ItemStack target, ItemStack currentCarried, ItemStack currentTarget) {
        if (carried != currentCarried || target != currentTarget) return ApplyOutcome.STALE;
        var loadout = carried.get(ModDataComponents.MASK_ITEM.get());
        if (!carried.is(ModItems.MASK.get()) || !valid(loadout)) return ApplyOutcome.INVALID;
        var equippable = target.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.slot() != EquipmentSlot.HEAD) return ApplyOutcome.NOT_HELMET;
        if (target.has(ModDataComponents.MASK_LOADOUT.get())) return ApplyOutcome.ALREADY_MASKED;
        target.set(ModDataComponents.MASK_LOADOUT.get(), loadout);
        carried.shrink(1);
        return ApplyOutcome.SUCCESS;
    }
    public RemovalResult remove(ItemStack target, ItemStack currentTarget, boolean cursorEmpty) {
        if (target != currentTarget) return new RemovalResult(RemoveOutcome.STALE, ItemStack.EMPTY);
        var loadout = target.get(ModDataComponents.MASK_LOADOUT.get());
        if (!valid(loadout)) return new RemovalResult(RemoveOutcome.INVALID, ItemStack.EMPTY);
        if (!cursorEmpty) return new RemovalResult(RemoveOutcome.OUTPUT_BLOCKED, ItemStack.EMPTY);
        ItemStack output = MaskItemFactory.create(loadout.maskIds(), content);
        target.remove(ModDataComponents.MASK_LOADOUT.get());
        return new RemovalResult(RemoveOutcome.SUCCESS, output);
    }
    private boolean valid(com.cosmicpve.data.component.MaskLoadout loadout) {
        return loadout != null && loadout.valid() && loadout.maskIds().stream()
                .allMatch(id -> content.findMaskDefinition(id).isPresent());
    }
}
