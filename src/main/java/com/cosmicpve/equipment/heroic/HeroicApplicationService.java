package com.cosmicpve.equipment.heroic;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.component.HeroicEquipmentKind;
import com.cosmicpve.data.component.HeroicIdentity;
import com.cosmicpve.equipment.armor.ArmorSetResolver;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

public final class HeroicApplicationService {
    public enum Outcome { SUCCESS, INVALID_CRYSTAL, INVALID_TARGET, ALREADY_HEROIC, STALE_TARGET }

    public static Optional<HeroicEquipmentKind> kind(ItemStack stack) {
        if (ArmorSetResolver.isArmor(stack)) return Optional.of(HeroicEquipmentKind.ARMOR);
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (stack.is(ItemTags.PICKAXES) || path.endsWith("_pickaxe"))
            return Optional.of(HeroicEquipmentKind.PICKAXE);
        if (stack.is(ItemTags.SHOVELS) || stack.getItem() instanceof ShovelItem)
            return Optional.of(HeroicEquipmentKind.SHOVEL);
        return Optional.empty();
    }

    public Outcome apply(ItemStack crystal, ItemStack target, ItemStack authoritativeTarget) {
        if (!crystal.is(ModItems.HEROIC_CRYSTAL.get())) return Outcome.INVALID_CRYSTAL;
        if (target != authoritativeTarget) return Outcome.STALE_TARGET;
        var kind = kind(target);
        if (kind.isEmpty() || !target.isDamageableItem()) return Outcome.INVALID_TARGET;
        if (target.has(ModDataComponents.HEROIC.get())) return Outcome.ALREADY_HEROIC;

        applyState(target, kind.orElseThrow());
        crystal.shrink(1);
        return Outcome.SUCCESS;
    }

    /** Applies the canonical typed Heroic state for authoritative reward construction. */
    public static void applyState(ItemStack target, HeroicEquipmentKind kind) {
        if (target.has(ModDataComponents.HEROIC.get())) return;
        target.set(ModDataComponents.HEROIC.get(), HeroicIdentity.of(kind));
        target.set(DataComponents.MAX_DAMAGE, target.getMaxDamage() + HeroicIdentity.DURABILITY_BONUS);
        switch (kind) {
            case PICKAXE -> target.set(DataComponents.ITEM_MODEL, CosmicPVE.id("heroic_golden_pickaxe"));
            case SHOVEL -> target.set(DataComponents.ITEM_MODEL, CosmicPVE.id("heroic_golden_shovel"));
            case ARMOR -> applyArmorPresentation(target);
        }
    }

    private static void applyArmorPresentation(ItemStack target) {
        var equippable = target.get(DataComponents.EQUIPPABLE);
        if (equippable == null) return;
        String suffix = switch (equippable.slot()) {
            case HEAD -> "helmet";
            case CHEST -> "chestplate";
            case LEGS -> "leggings";
            case FEET -> "boots";
            default -> throw new IllegalArgumentException("Unsupported Heroic armor slot: " + equippable.slot());
        };
        target.set(DataComponents.ITEM_MODEL, CosmicPVE.id("heroic_leather_" + suffix));
        target.set(DataComponents.EQUIPPABLE, new Equippable(
                equippable.slot(), equippable.equipSound(), Optional.of(EquipmentAssets.LEATHER),
                equippable.cameraOverlay(), equippable.allowedEntities(), equippable.dispensable(),
                equippable.swappable(), equippable.damageOnHurt(), equippable.equipOnInteract(),
                equippable.canBeSheared(), equippable.shearingSound()));
    }
}
