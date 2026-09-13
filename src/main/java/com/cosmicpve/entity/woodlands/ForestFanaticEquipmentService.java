package com.cosmicpve.entity.woodlands;

import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class ForestFanaticEquipmentService {
    public static final float DROP_CHANCE = 0.0F;

    public interface Rolls {
        int nextInt(int bound);
        static Rolls from(RandomSource random) { return random::nextInt; }
    }

    public ForestFanaticEquipmentPlan generate(Rolls rolls) {
        Optional<ForestFanaticEquipmentPlan.Helmet> helmet = oneThird(rolls)
                ? Optional.of(new ForestFanaticEquipmentPlan.Helmet(level(rolls, 4), level(rolls, 6)))
                : Optional.empty();
        Optional<ForestFanaticEquipmentPlan.Chestplate> chest = oneThird(rolls)
                ? Optional.of(new ForestFanaticEquipmentPlan.Chestplate(level(rolls, 4), level(rolls, 4)))
                : Optional.empty();
        Optional<ForestFanaticEquipmentPlan.Leggings> legs = oneThird(rolls)
                ? Optional.of(new ForestFanaticEquipmentPlan.Leggings(7, level(rolls, 4), 4))
                : Optional.empty();
        Optional<ForestFanaticEquipmentPlan.Boots> boots = oneThird(rolls)
                ? Optional.of(new ForestFanaticEquipmentPlan.Boots(4, level(rolls, 5), level(rolls, 4), level(rolls, 4)))
                : Optional.empty();
        var bow = new ForestFanaticEquipmentPlan.Bow(3,
                optionalLevel(rolls, 5), optionalLevel(rolls, 3), optionalLevel(rolls, 3), optionalLevel(rolls, 4));
        return new ForestFanaticEquipmentPlan(helmet, chest, legs, boots, bow);
    }

    public ForestFanaticEquipmentPlan equip(ForestFanaticEntity entity, RegistryAccess registries, RandomSource random) {
        var plan = generate(Rolls.from(random));
        apply(entity, registries, plan);
        return plan;
    }

    public void apply(ForestFanaticEntity entity, RegistryAccess registries, ForestFanaticEquipmentPlan plan) {
        var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        plan.helmet().ifPresent(roll -> {
            var stack = new ItemStack(Items.CHAINMAIL_HELMET);
            set(stack, enchantments.getOrThrow(Enchantments.PROTECTION), roll.protection());
            set(stack, enchantments.getOrThrow(ModEnchantments.VOODOO), roll.voodoo());
            equip(entity, EquipmentSlot.HEAD, stack);
        });
        plan.chestplate().ifPresent(roll -> {
            var stack = new ItemStack(Items.IRON_CHESTPLATE);
            set(stack, enchantments.getOrThrow(ModEnchantments.ARMORED), roll.armored());
            set(stack, enchantments.getOrThrow(Enchantments.PROTECTION), roll.protection());
            equip(entity, EquipmentSlot.CHEST, stack);
        });
        plan.leggings().ifPresent(roll -> {
            var stack = greenLeather(new ItemStack(Items.LEATHER_LEGGINGS));
            set(stack, enchantments.getOrThrow(ModEnchantments.PLAGUE_CARRIER), roll.plagueCarrier());
            set(stack, enchantments.getOrThrow(ModEnchantments.ARMORED), roll.armored());
            set(stack, enchantments.getOrThrow(Enchantments.PROTECTION), roll.protection());
            equip(entity, EquipmentSlot.LEGS, stack);
        });
        plan.boots().ifPresent(roll -> {
            var stack = greenLeather(new ItemStack(Items.LEATHER_BOOTS));
            set(stack, enchantments.getOrThrow(ModEnchantments.NIMBLE), roll.nimble());
            set(stack, enchantments.getOrThrow(ModEnchantments.ANGELIC), roll.angelic());
            set(stack, enchantments.getOrThrow(ModEnchantments.ARMORED), roll.armored());
            set(stack, enchantments.getOrThrow(Enchantments.PROTECTION), roll.protection());
            equip(entity, EquipmentSlot.FEET, stack);
        });

        var bow = new ItemStack(Items.BOW);
        set(bow, enchantments.getOrThrow(ModEnchantments.VENOM), plan.bow().venom());
        if (plan.bow().power() > 0) set(bow, enchantments.getOrThrow(Enchantments.POWER), plan.bow().power());
        if (plan.bow().virus() > 0) set(bow, enchantments.getOrThrow(ModEnchantments.VIRUS), plan.bow().virus());
        if (plan.bow().obliterate() > 0) set(bow, enchantments.getOrThrow(ModEnchantments.OBLITERATE), plan.bow().obliterate());
        if (plan.bow().snare() > 0) set(bow, enchantments.getOrThrow(ModEnchantments.SNARE), plan.bow().snare());
        equip(entity, EquipmentSlot.MAINHAND, bow);
        entity.setCanPickUpLoot(false);
    }

    public static int greenLeatherColor() {
        return greenLeather(new ItemStack(Items.LEATHER_BOOTS)).getOrDefault(
                DataComponents.DYED_COLOR, new DyedItemColor(0)).rgb();
    }

    private static ItemStack greenLeather(ItemStack stack) {
        return DyedItemColor.applyDyes(stack, List.of((DyeItem) Items.GREEN_DYE));
    }

    private static void equip(ForestFanaticEntity entity, EquipmentSlot slot, ItemStack stack) {
        entity.setItemSlot(slot, stack);
        entity.setDropChance(slot, DROP_CHANCE);
    }

    private static void set(ItemStack stack, net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment, int level) {
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(enchantment, level));
    }

    private static boolean oneThird(Rolls rolls) { return rolls.nextInt(3) == 0; }
    private static int level(Rolls rolls, int max) { return 1 + rolls.nextInt(max); }
    private static int optionalLevel(Rolls rolls, int max) { return oneThird(rolls) ? level(rolls, max) : 0; }
}
