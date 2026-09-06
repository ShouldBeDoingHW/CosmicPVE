package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.CosmicPVE;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public final class EffectiveEnchantmentsResolver {
    public EffectiveEnchantments resolve(LivingEntity owner, ItemStack stack, List<VirtualEnchantmentGrant> grants) {
        return com.cosmicpve.adventure.AdventureRules.filter(owner,resolve(stack,com.cosmicpve.adventure.AdventureRules.restricted(owner) ? List.of() : grants));
    }
    public EffectiveEnchantments resolve(ItemStack stack, List<VirtualEnchantmentGrant> virtualGrants) {
        return resolve(stack, CosmicPVE.id("actual_item"), virtualGrants);
    }

    public EffectiveEnchantments resolve(
            ItemStack stack, Identifier actualSourceId, List<VirtualEnchantmentGrant> virtualGrants) {
        var actualGrants = new ArrayList<ActualEnchantmentGrant>();
        for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()) {
            entry.getKey().unwrapKey().ifPresent(key -> actualGrants.add(
                    new ActualEnchantmentGrant(key.identifier(), entry.getIntValue(), actualSourceId)));
        }
        return resolveSources(actualGrants, virtualGrants);
    }

    public EffectiveEnchantments resolve(LivingEntity entity, List<VirtualEnchantmentGrant> virtualGrants) {
        var actualGrants = new ArrayList<ActualEnchantmentGrant>();
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            Identifier sourceId = CosmicPVE.id("actual_" + slot.getSerializedName());
            for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(entity.getItemBySlot(slot)).entrySet()) {
                entry.getKey().unwrapKey().ifPresent(key -> actualGrants.add(
                        new ActualEnchantmentGrant(key.identifier(), entry.getIntValue(), sourceId)));
            }
        }
        return com.cosmicpve.adventure.AdventureRules.filter(entity,resolveSources(actualGrants, virtualGrants));
    }

    public EffectiveEnchantments resolveSources(
            List<ActualEnchantmentGrant> actualGrants, List<VirtualEnchantmentGrant> virtualGrants) {
        var sources = new HashMap<Identifier, List<EnchantmentProvenance>>();
        for (var grant : actualGrants) {
            sources.computeIfAbsent(grant.enchantmentId(), ignored -> new ArrayList<>())
                    .add(new EnchantmentProvenance(EnchantmentSourceKind.ACTUAL, grant.sourceId(), grant.level()));
        }
        for (var grant : virtualGrants) {
            sources.computeIfAbsent(grant.enchantmentId(), ignored -> new ArrayList<>())
                    .add(new EnchantmentProvenance(EnchantmentSourceKind.VIRTUAL, grant.sourceId(), grant.level()));
        }

        var resolved = new HashMap<Identifier, EffectiveEnchantment>();
        sources.forEach((id, provenance) -> {
            int effectiveLevel = provenance.stream().mapToInt(EnchantmentProvenance::level).max().orElseThrow();
            resolved.put(id, new EffectiveEnchantment(id, effectiveLevel, provenance));
        });
        return new EffectiveEnchantments(resolved);
    }
}
