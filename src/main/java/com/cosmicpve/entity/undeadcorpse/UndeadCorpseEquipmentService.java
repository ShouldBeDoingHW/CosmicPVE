package com.cosmicpve.entity.undeadcorpse;

import com.cosmicpve.registry.ModEnchantments;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class UndeadCorpseEquipmentService {
    public static final float ENCHANT_CHANCE = 0.30F;
    public static final int BASE_ENCHANT_LEVEL = 3;
    private UndeadCorpseEquipmentService() {}

    @FunctionalInterface public interface FloatRoll { float next(); }
    public record AxeRolls(boolean sharpness, boolean bleed, boolean rage) {}
    public static AxeRolls rollEnchantments(FloatRoll rolls) {
        return new AxeRolls(rolls.next() < ENCHANT_CHANCE, rolls.next() < ENCHANT_CHANCE,
                rolls.next() < ENCHANT_CHANCE);
    }

    public static ItemStack rollAxe(RegistryAccess registries, RandomSource random) {
        ItemStack axe = new ItemStack(Items.IRON_AXE);
        var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        AxeRolls rolls = rollEnchantments(random::nextFloat);
        EnchantmentHelper.updateEnchantments(axe, mutable -> {
            if (rolls.sharpness()) mutable.set(enchantments.getOrThrow(Enchantments.SHARPNESS), BASE_ENCHANT_LEVEL);
            if (rolls.bleed()) mutable.set(enchantments.getOrThrow(ModEnchantments.BLEED), BASE_ENCHANT_LEVEL);
            if (rolls.rage()) mutable.set(enchantments.getOrThrow(ModEnchantments.RAGE), BASE_ENCHANT_LEVEL);
        });
        return axe;
    }

    public static void equipBase(UndeadCorpseEntity corpse, RegistryAccess registries, RandomSource random) {
        corpse.setItemSlot(EquipmentSlot.MAINHAND, rollAxe(registries, random));
        for (EquipmentSlot slot : EquipmentSlot.values()) if (slot.isArmor()) corpse.setDropChance(slot, 0.0F);
        corpse.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }
}
