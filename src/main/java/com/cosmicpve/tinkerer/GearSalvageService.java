package com.cosmicpve.tinkerer;

import com.cosmicpve.data.component.StoredXpBottleData;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.equipment.enchantment.CustomEnchantCapacityService;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import java.util.OptionalLong;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Pure gear-to-XP valuation and typed bottle construction for the Tinkerer. */
public final class GearSalvageService {
    public static final long VANILLA_XP_PER_LEVEL = 25;
    public static final long HEROIC_XP_PER_LEVEL = 2500;

    public OptionalLong storedXp(ItemStack stack) {
        if ((!CustomEnchantCapacityService.isArmor(stack) && !isSalvageWeapon(stack))
                || stack.has(ModDataComponents.MASK_LOADOUT.get()) || stack.has(ModDataComponents.WEAPON_SKIN.get())) {
            return OptionalLong.empty();
        }
        long total = 0;
        boolean enchanted = false;
        try {
            for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()) {
                int level = entry.getIntValue();
                if (level <= 0) continue;
                enchanted = true;
                var id = entry.getKey().unwrapKey().map(key -> key.identifier()).orElse(null);
                long perLevel = id == null ? VANILLA_XP_PER_LEVEL : CosmicEnchantmentSpecs.find(id)
                        .map(spec -> perLevel(spec.tier())).orElse(VANILLA_XP_PER_LEVEL);
                total = Math.addExact(total, Math.multiplyExact(perLevel, level));
            }
        } catch (ArithmeticException overflow) {
            return OptionalLong.empty();
        }
        return enchanted && total > 0 ? OptionalLong.of(total) : OptionalLong.empty();
    }

    private static boolean isSalvageWeapon(ItemStack stack) {
        return stack.is(net.minecraft.tags.ItemTags.SWORDS)
                || stack.getItem() instanceof net.minecraft.world.item.AxeItem
                || stack.getItem() instanceof net.minecraft.world.item.BowItem
                || stack.getItem() instanceof net.minecraft.world.item.CrossbowItem;
    }

    public ItemStack bottle(long storedXp) {
        if (storedXp < 1) throw new IllegalArgumentException("Stored XP must be positive");
        ItemStack bottle = new ItemStack(ModItems.SALVAGED_XP_BOTTLE.get());
        bottle.set(ModDataComponents.STORED_XP_BOTTLE.get(), new StoredXpBottleData(
                StoredXpBottleData.CURRENT_DATA_VERSION, storedXp));
        return bottle;
    }

    public static long perLevel(CosmicEnchantmentTier tier) {
        return switch (tier) {
            case SIMPLE -> 75;
            case UNIQUE -> 125;
            case ELITE -> 250;
            case ULTIMATE -> 475;
            case LEGENDARY -> 800;
            case MASTERY -> 2500;
        };
    }
}
