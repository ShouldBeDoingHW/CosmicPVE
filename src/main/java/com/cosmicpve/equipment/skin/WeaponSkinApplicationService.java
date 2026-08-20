package com.cosmicpve.equipment.skin;

import com.cosmicpve.data.component.WeaponSkinIdentity;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

/** Atomic, server-facing mutation boundary for skin attachment and removal. */
public final class WeaponSkinApplicationService {
    public enum ApplyOutcome { SUCCESS, REJECTED_STALE, REJECTED_INVALID_SKIN, REJECTED_TARGET, REJECTED_ALREADY_SKINNED }
    public enum RemoveOutcome { SUCCESS, REJECTED_STALE, REJECTED_NOT_SKINNED, REJECTED_OUTPUT }
    public record RemovalResult(RemoveOutcome outcome, ItemStack returnedSkin) {
        public RemovalResult { returnedSkin = returnedSkin.copy(); }
        public static RemovalResult rejected(RemoveOutcome outcome) { return new RemovalResult(outcome, ItemStack.EMPTY); }
    }

    public ApplyOutcome apply(ItemStack carried, ItemStack target, ItemStack currentCarried, ItemStack currentTarget) {
        if (carried != currentCarried || target != currentTarget) return ApplyOutcome.REJECTED_STALE;
        var itemData = carried.get(ModDataComponents.WEAPON_SKIN_ITEM.get());
        if (!carried.is(ModItems.WEAPON_SKIN.get()) || itemData == null)
            return ApplyOutcome.REJECTED_INVALID_SKIN;
        var definition = WeaponSkinDefinitions.find(itemData.skinId()).orElse(null);
        if (definition == null) return ApplyOutcome.REJECTED_INVALID_SKIN;
        if (!definition.accepts(target)) return ApplyOutcome.REJECTED_TARGET;
        if (target.has(ModDataComponents.WEAPON_SKIN.get())) return ApplyOutcome.REJECTED_ALREADY_SKINNED;

        var previousModel = Optional.ofNullable(target.get(DataComponents.ITEM_MODEL));
        target.set(ModDataComponents.WEAPON_SKIN.get(), new WeaponSkinIdentity(
                WeaponSkinIdentity.CURRENT_DATA_VERSION, definition.id(), previousModel));
        target.set(DataComponents.ITEM_MODEL, definition.itemModel());
        carried.shrink(1);
        return ApplyOutcome.SUCCESS;
    }

    public RemovalResult remove(ItemStack target, ItemStack currentTarget, boolean cursorEmpty) {
        if (target != currentTarget) return RemovalResult.rejected(RemoveOutcome.REJECTED_STALE);
        var identity = target.get(ModDataComponents.WEAPON_SKIN.get());
        if (identity == null || WeaponSkinDefinitions.find(identity.skinId()).isEmpty())
            return RemovalResult.rejected(RemoveOutcome.REJECTED_NOT_SKINNED);
        if (!cursorEmpty) return RemovalResult.rejected(RemoveOutcome.REJECTED_OUTPUT);

        var output = WeaponSkinItemFactory.create(identity.skinId());
        identity.previousItemModel().ifPresentOrElse(
                model -> target.set(DataComponents.ITEM_MODEL, model),
                () -> target.remove(DataComponents.ITEM_MODEL));
        target.remove(ModDataComponents.WEAPON_SKIN.get());
        return new RemovalResult(RemoveOutcome.SUCCESS, output);
    }
}
