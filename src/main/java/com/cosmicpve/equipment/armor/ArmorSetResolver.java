package com.cosmicpve.equipment.armor;

import com.cosmicpve.content.CosmicContentRepository;
import com.cosmicpve.content.definition.armor.ArmorSetDefinition;
import com.cosmicpve.registry.ModDataComponents;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** The single owner of full four-piece identity resolution for players and mobs. */
public final class ArmorSetResolver {
    private final CosmicContentRepository content;

    public ArmorSetResolver(CosmicContentRepository content) {
        this.content = content;
    }

    public Optional<ArmorSetDefinition> resolve(LivingEntity entity) {
        return resolveIdentity(List.of(
                entity.getItemBySlot(EquipmentSlot.HEAD), entity.getItemBySlot(EquipmentSlot.CHEST),
                entity.getItemBySlot(EquipmentSlot.LEGS), entity.getItemBySlot(EquipmentSlot.FEET)))
                .flatMap(content::findArmorSetDefinition);
    }

    public static Optional<Identifier> resolveIdentity(List<ItemStack> pieces) {
        if (pieces.size() != 4) return Optional.empty();
        var identities = new java.util.ArrayList<Optional<Identifier>>(4);
        for (ItemStack stack : pieces) {
            if (!isArmor(stack)) return Optional.empty();
            var identity = stack.get(ModDataComponents.ARMOR_SET_ID.get());
            identities.add(identity == null ? Optional.empty() : Optional.of(identity.setId()));
        }
        return resolveIdentityIds(identities);
    }

    /** Pure identity seam used by resolution tests; armor eligibility is checked before this in production. */
    public static Optional<Identifier> resolveIdentityIds(List<Optional<Identifier>> pieces) {
        if (pieces.size() != 4 || pieces.stream().anyMatch(Optional::isEmpty)) return Optional.empty();
        Identifier id = pieces.getFirst().orElseThrow();
        return pieces.stream().allMatch(piece -> piece.orElseThrow().equals(id)) ? Optional.of(id) : Optional.empty();
    }

    public static boolean isArmor(ItemStack stack) {
        var equippable = stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }
}
