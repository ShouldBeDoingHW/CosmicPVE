package com.cosmicpve.vkit;

import com.cosmicpve.data.component.VKitEquipmentData;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpec;
import com.cosmicpve.equipment.enchantment.CustomEnchantCapacityService;
import com.cosmicpve.registry.ModDataComponents;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.IntUnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

/** Produces one final, already-enchanted V-Kit equipment roll. */
public final class VKitEquipmentGenerator {
    public static final int MAX_KIT_LEVEL = 10;
    private static final int[] POINT_BUDGETS = {0, 5, 6, 7, 9, 10, 12, 13, 14, 16, 18};
    private final CustomEnchantCapacityService capacity = new CustomEnchantCapacityService();

    public ItemStack roll(VKitDefinition definition, int level, Registry<Enchantment> enchantments, RandomSource random) {
        VKitEquipmentType type = chooseType(random::nextBoolean);
        return generate(definition, type, level, enchantments, random);
    }

    public VKitEquipmentType chooseType(RandomSource random) {
        return chooseType(random::nextBoolean);
    }

    public VKitEquipmentType chooseType(BooleanSupplier branch) {
        return branch.getAsBoolean() ? VKitEquipmentType.ARMOR : VKitEquipmentType.WEAPON;
    }

    public ItemStack generate(VKitDefinition definition, VKitEquipmentType type, int level,
            Registry<Enchantment> enchantments, RandomSource random) {
        validateLevel(level);
        VKitDefinition.EquipmentReward reward = type == VKitEquipmentType.ARMOR
                ? definition.armor() : definition.weapon();
        ItemStack result = new ItemStack(reward.item());
        result.set(ModDataComponents.VKIT_EQUIPMENT.get(), new VKitEquipmentData(
                VKitEquipmentData.CURRENT_DATA_VERSION, definition.id(), level, type));
        result.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                equipmentName(definition, reward, java.util.OptionalInt.of(level)));

        Map<CosmicEnchantmentSpec, Integer> cosmic = allocate(reward.pool(level), pointBudget(level), random);
        if (cosmic.size() > capacity.capacity(result)) {
            throw new IllegalStateException("V-Kit roll exceeds the item's Cosmic enchantment capacity");
        }
        EnchantmentHelper.updateEnchantments(result, mutable -> {
            applyVanilla(result, type, level, enchantments, mutable);
            cosmic.forEach((spec, enchantLevel) -> {
                var holder = enchantments.get(spec.id()).orElseThrow(() ->
                        new IllegalStateException("Missing registered V-Kit enchantment " + spec.id()));
                if (!holder.value().canEnchant(result)) {
                    throw new IllegalStateException(spec.id() + " cannot enchant " + reward.item());
                }
                mutable.set(holder, enchantLevel);
            });
        });
        return result;
    }

    public Map<CosmicEnchantmentSpec, Integer> allocate(
            List<CosmicEnchantmentSpec> pool, int points, RandomSource random) {
        return allocate(pool, points, random::nextInt);
    }

    public Map<CosmicEnchantmentSpec, Integer> allocate(
            List<CosmicEnchantmentSpec> pool, int points, IntUnaryOperator indexSelector) {
        int available = pool.stream().mapToInt(CosmicEnchantmentSpec::maxLevel).sum();
        if (points < 1 || available < points) {
            throw new IllegalArgumentException("V-Kit pool cannot spend exactly " + points + " points");
        }
        var remaining = new ArrayList<>(pool);
        var result = new LinkedHashMap<CosmicEnchantmentSpec, Integer>();
        int unspent = points;
        while (unspent > 0) {
            int index = indexSelector.applyAsInt(remaining.size());
            if (index < 0 || index >= remaining.size()) throw new IllegalArgumentException("Invalid V-Kit pool index " + index);
            CosmicEnchantmentSpec selected = remaining.remove(index);
            int level = Math.min(selected.maxLevel(), unspent);
            result.put(selected, level);
            unspent -= level;
        }
        return Map.copyOf(result);
    }

    public static int pointBudget(int level) {
        validateLevel(level);
        return POINT_BUDGETS[level];
    }

    private static void applyVanilla(ItemStack stack, VKitEquipmentType type, int level, Registry<Enchantment> enchantments,
            net.minecraft.world.item.enchantment.ItemEnchantments.Mutable mutable) {
        if (stack.is(Items.BOW)) {
            mutable.set(enchantments.getOrThrow(Enchantments.POWER), 5);
        } else if (stack.getItem() instanceof CrossbowItem) {
            mutable.set(enchantments.getOrThrow(Enchantments.PIERCING), 4);
        } else if (type == VKitEquipmentType.ARMOR) {
            mutable.set(enchantments.getOrThrow(Enchantments.PROTECTION), 4);
        } else {
            mutable.set(enchantments.getOrThrow(Enchantments.SHARPNESS), 5);
        }
        if (level >= 3) mutable.set(enchantments.getOrThrow(Enchantments.UNBREAKING), 3);
        if (level >= 8) {
            mutable.set(enchantments.getOrThrow(
                    stack.getItem() instanceof BowItem ? Enchantments.INFINITY : Enchantments.MENDING), 1);
        }
    }

    private static void validateLevel(int level) {
        if (level < 1 || level > MAX_KIT_LEVEL) throw new IllegalArgumentException("V-Kit level must be 1-10");
    }

    public static String roman(int level) {
        return switch (level) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; case 5 -> "V";
            case 6 -> "VI"; case 7 -> "VII"; case 8 -> "VIII"; case 9 -> "IX"; case 10 -> "X";
            default -> throw new IllegalArgumentException("V-Kit level must be 1-10");
        };
    }

    /** Canonical V-Kit equipment-name component, shared by generated gear and informational previews. */
    public static Component equipmentName(VKitDefinition definition, VKitDefinition.EquipmentReward reward,
            java.util.OptionalInt level) {
        String suffix = level.isPresent() ? " (" + roman(level.getAsInt()) + ")" : "";
        return Component.literal(reward.displayName() + suffix)
                .withStyle(style -> style.withColor(definition.color()).withBold(true).withItalic(true));
    }
}
