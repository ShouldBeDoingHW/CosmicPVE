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
    private final ArmorSetSuppressionService suppression;

    public ArmorSetResolver(CosmicContentRepository content) {
        this(content, new ArmorSetSuppressionService());
    }

    public ArmorSetResolver(CosmicContentRepository content, ArmorSetSuppressionService suppression) {
        this.content = content;
        this.suppression = suppression;
    }

    public Optional<ArmorSetDefinition> resolve(LivingEntity entity) {
        return suppression.isSuppressed(entity) ? Optional.empty() : resolvePotential(entity)
                .filter(d -> !com.cosmicpve.adventure.AdventureRules.restricted(entity) || d.id().equals(ArmorSetIds.DIMENSIONAL_TRAVELER));
    }

    /** Resolves equipment identity without applying temporary activation suppression. */
    public Optional<ArmorSetDefinition> resolvePotential(LivingEntity entity) {
        return resolveIdentity(List.of(
                entity.getItemBySlot(EquipmentSlot.HEAD), entity.getItemBySlot(EquipmentSlot.CHEST),
                entity.getItemBySlot(EquipmentSlot.LEGS), entity.getItemBySlot(EquipmentSlot.FEET)))
                .flatMap(content::findArmorSetDefinition);
    }

    public boolean isSuppressed(LivingEntity entity) { return suppression.isSuppressed(entity); }

    public static Optional<Identifier> resolveIdentity(List<ItemStack> pieces) {
        if (pieces.size() != 4) return Optional.empty();
        var identities = new java.util.ArrayList<PieceIdentity>(4);
        for (ItemStack stack : pieces) {
            if (!isArmor(stack)) return Optional.empty();
            var identity = stack.get(ModDataComponents.ARMOR_SET_ID.get());
            identities.add(new PieceIdentity(identity == null ? Optional.empty() : Optional.of(identity.setId()),
                    Boolean.TRUE.equals(stack.get(ModDataComponents.OMNI_ARMOR.get()))));
        }
        return resolvePieceIdentities(identities);
    }

    /** Pure identity seam used by resolution tests; armor eligibility is checked before this in production. */
    public static Optional<Identifier> resolveIdentityIds(List<Optional<Identifier>> pieces) {
        if (pieces.size() != 4 || pieces.stream().anyMatch(Optional::isEmpty)) return Optional.empty();
        Identifier id = pieces.getFirst().orElseThrow();
        return pieces.stream().allMatch(piece -> piece.orElseThrow().equals(id)) ? Optional.of(id) : Optional.empty();
    }

    public static Optional<Identifier> resolvePieceIdentities(List<PieceIdentity> pieces) {
        if (pieces.size() != 4) return Optional.empty();
        var anchors = pieces.stream().filter(piece -> !piece.omni()).toList();
        if (anchors.size() < 2 || anchors.stream().anyMatch(piece -> piece.setId().isEmpty())) return Optional.empty();
        Identifier id = anchors.getFirst().setId().orElseThrow();
        return anchors.stream().allMatch(piece -> piece.setId().orElseThrow().equals(id))
                ? Optional.of(id) : Optional.empty();
    }

    public record PieceIdentity(Optional<Identifier> setId, boolean omni) {}

    public static boolean isArmor(ItemStack stack) {
        var equippable = stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }
}
