package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.registry.ModEnchantments;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

public final class MiningEnchantmentService {
    private final EffectiveEnchantmentsResolver enchantments = new EffectiveEnchantmentsResolver();

    public void apply(BlockDropsEvent event) {
        ItemStack tool = event.getTool();
        var effective = event.getBreaker() instanceof net.minecraft.world.entity.LivingEntity owner
                ? enchantments.resolve(owner, tool, List.of()) : enchantments.resolve(tool, List.of());
        int autoSmelt = effective.level(ModEnchantments.AUTO_SMELT.identifier());
        int experience = effective.level(ModEnchantments.EXPERIENCE.identifier());
        int telekinesis = effective.level(ModEnchantments.TELEKINESIS.identifier());
        if (autoSmelt > 0 && event.getLevel() instanceof ServerLevel level) {
            transformDrops(event.getDrops(), stack -> smeltingResult(level, stack));
        }
        if (telekinesis > 0 && event.getBreaker() instanceof ServerPlayer player) {
            routeFinalDrops(event.getDrops(), player);
        }
        if (experience > 0) event.setDroppedExperience(scaleBlockExperience(event.getDroppedExperience(), experience));
    }

    public float applyObsidianDestroyer(float currentSpeed, BlockState state, ItemStack tool) {
        int level = enchantments.resolve(tool, List.of()).level(ModEnchantments.OBSIDIAN_DESTROYER.identifier());
        return applyObsidianDestroyer(currentSpeed, state, level);
    }

    public float applyObsidianDestroyer(
            net.minecraft.world.entity.LivingEntity owner, float currentSpeed, BlockState state, ItemStack tool) {
        int level = enchantments.resolve(owner, tool, List.of()).level(ModEnchantments.OBSIDIAN_DESTROYER.identifier());
        return applyObsidianDestroyer(currentSpeed, state, level);
    }

    public static float applyObsidianDestroyer(float currentSpeed, BlockState state, int level) {
        return currentSpeed + (float) obsidianDestroyerBonus(state, level);
    }

    public static double obsidianDestroyerBonus(BlockState state, int level) {
        return state.is(Blocks.OBSIDIAN) ? 2.0 * Math.max(0, Math.min(4, level)) : 0.0;
    }

    static ItemStack smeltingResult(ServerLevel level, ItemStack inputStack) {
        ItemStack one = inputStack.copyWithCount(1);
        return level.getServer().getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(one), level)
                .map(holder -> holder.value().assemble(new SingleRecipeInput(one), level.registryAccess()))
                .orElse(ItemStack.EMPTY);
    }

    public static void transformDrops(List<ItemEntity> drops, Function<ItemStack, ItemStack> recipeResolver) {
        var replacement = new ArrayList<ItemEntity>();
        for (ItemEntity entity : List.copyOf(drops)) {
            ItemStack original = entity.getItem();
            ItemStack result = recipeResolver.apply(original);
            if (result.isEmpty()) continue;
            long total = (long) original.getCount() * result.getCount();
            int firstCount = (int) Math.min(total, result.getMaxStackSize());
            entity.setItem(result.copyWithCount(firstCount));
            total -= firstCount;
            while (total > 0) {
                int count = (int) Math.min(total, result.getMaxStackSize());
                var split = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(),
                        result.copyWithCount(count));
                split.setDeltaMovement(entity.getDeltaMovement());
                replacement.add(split);
                total -= count;
            }
        }
        drops.addAll(replacement);
    }

    public static List<ItemStack> transformStacks(List<ItemStack> drops,
            Function<ItemStack, ItemStack> recipeResolver) {
        var transformed = new ArrayList<ItemStack>();
        for (ItemStack original : drops) {
            ItemStack result = recipeResolver.apply(original);
            if (result.isEmpty()) {
                transformed.add(original.copy());
                continue;
            }
            long total = (long) original.getCount() * result.getCount();
            while (total > 0) {
                int count = (int) Math.min(total, result.getMaxStackSize());
                transformed.add(result.copyWithCount(count));
                total -= count;
            }
        }
        return List.copyOf(transformed);
    }

    /** Routes only the final entities belonging to this exact accepted BlockDropsEvent. */
    static void routeFinalDrops(List<ItemEntity> drops, ServerPlayer player) {
        for (ItemEntity entity : List.copyOf(drops)) {
            ItemStack overflow = overflowAfterInsertion(entity.getItem(), player.getInventory()::add);
            if (overflow.isEmpty()) drops.remove(entity);
            else entity.setItem(overflow);
        }
    }

    static ItemStack overflowAfterInsertion(ItemStack finalDrop, Consumer<ItemStack> insertion) {
        ItemStack remainder = finalDrop.copy();
        insertion.accept(remainder);
        return remainder;
    }

    public static int scaleBlockExperience(int vanillaExperience, int level) {
        if (vanillaExperience <= 0 || level <= 0) return vanillaExperience;
        return Mth.floor(vanillaExperience * (1.0D + 0.5D * level));
    }
}
