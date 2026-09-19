package com.cosmicpve.entity.spacepirate;

import com.cosmicpve.registry.ModEnchantments;
import java.util.ArrayList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class SpacePirateEquipmentService {
    public static final float GENERATED_EQUIPMENT_DROP_CHANCE = 0.0F;
    public static final int VARIANT_1_INSANITY_LEVEL = 8;
    public static final int VARIANT_1_PUMMEL_LEVEL = 3;
    public interface Rolls {
        boolean nextBoolean();
        int nextInt(int bound);
        static Rolls from(RandomSource random) {
            return new Rolls() {
                @Override public boolean nextBoolean() { return random.nextBoolean(); }
                @Override public int nextInt(int bound) { return random.nextInt(bound); }
            };
        }
    }

    public SpacePirateEquipmentPlan generate(SpacePirateVariant variant, Rolls rolls) {
        var armor = new ArrayList<SpacePirateEquipmentPlan.ArmorRoll>(4);
        for (int index = 0; index < 4; index++) {
            var material = rolls.nextBoolean() ? SpacePirateEquipmentPlan.ArmorMaterial.DIAMOND
                    : SpacePirateEquipmentPlan.ArmorMaterial.IRON;
            armor.add(new SpacePirateEquipmentPlan.ArmorRoll(material, 1 + rolls.nextInt(4)));
        }
        return switch (variant) {
            case VARIANT_1 -> {
                boolean pummel = rolls.nextBoolean();
                int silence = rolls.nextBoolean() ? 1 + rolls.nextInt(4) : 0;
                yield new SpacePirateEquipmentPlan(variant, armor, 0, 0, pummel, silence);
            }
            case VARIANT_2 -> {
                int poison = 1 + rolls.nextInt(3);
                boolean execute = rolls.nextInt(4) == 0;
                yield new SpacePirateEquipmentPlan(variant, armor, poison,
                        execute ? 1 + rolls.nextInt(5) : 0, false);
            }
        };
    }

    public SpacePirateEquipmentPlan equip(SpacePirateEntity entity, RegistryAccess registries, RandomSource random) {
        var plan = generate(entity.variant(), Rolls.from(random));
        apply(entity, registries, plan);
        return plan;
    }

    public void apply(SpacePirateEntity entity, RegistryAccess registries, SpacePirateEquipmentPlan plan) {
        var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        var protection = enchantments.getOrThrow(Enchantments.PROTECTION);
        EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int index = 0; index < armorSlots.length; index++) {
            EquipmentSlot slot = armorSlots[index];
            var roll = plan.armor().get(index);
            ItemStack stack = new ItemStack(armorItem(slot, roll.material()));
            EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(protection, roll.protectionLevel()));
            entity.setItemSlot(slot, stack);
            entity.setDropChance(slot, GENERATED_EQUIPMENT_DROP_CHANCE);
        }

        ItemStack weapon;
        if (plan.variant() == SpacePirateVariant.VARIANT_1) {
            weapon = new ItemStack(Items.DIAMOND_AXE);
            var insanity = enchantments.getOrThrow(ModEnchantments.INSANITY);
            EnchantmentHelper.updateEnchantments(weapon, mutable -> mutable.set(insanity, VARIANT_1_INSANITY_LEVEL));
            if (plan.pummel()) {
                var pummel = enchantments.getOrThrow(ModEnchantments.PUMMEL);
                EnchantmentHelper.updateEnchantments(weapon, mutable -> mutable.set(pummel, VARIANT_1_PUMMEL_LEVEL));
            }
            if (plan.silenceLevel() > 0) {
                var silence = enchantments.getOrThrow(ModEnchantments.SILENCE);
                EnchantmentHelper.updateEnchantments(weapon, mutable -> mutable.set(silence, plan.silenceLevel()));
            }
        } else {
            weapon = new ItemStack(Items.IRON_SWORD);
            var poison = enchantments.getOrThrow(ModEnchantments.POISON);
            EnchantmentHelper.updateEnchantments(weapon, mutable -> mutable.set(poison, plan.poisonLevel()));
            if (plan.executeLevel() > 0) {
                var execute = enchantments.getOrThrow(ModEnchantments.EXECUTE);
                EnchantmentHelper.updateEnchantments(weapon, mutable -> mutable.set(execute, plan.executeLevel()));
            }
        }
        entity.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        entity.setDropChance(EquipmentSlot.MAINHAND, GENERATED_EQUIPMENT_DROP_CHANCE);
    }

    private static Item armorItem(EquipmentSlot slot, SpacePirateEquipmentPlan.ArmorMaterial material) {
        return switch (material) {
            case IRON -> switch (slot) {
                case HEAD -> Items.IRON_HELMET; case CHEST -> Items.IRON_CHESTPLATE;
                case LEGS -> Items.IRON_LEGGINGS; case FEET -> Items.IRON_BOOTS;
                default -> throw new IllegalArgumentException("Not an armor slot: " + slot);
            };
            case DIAMOND -> switch (slot) {
                case HEAD -> Items.DIAMOND_HELMET; case CHEST -> Items.DIAMOND_CHESTPLATE;
                case LEGS -> Items.DIAMOND_LEGGINGS; case FEET -> Items.DIAMOND_BOOTS;
                default -> throw new IllegalArgumentException("Not an armor slot: " + slot);
            };
        };
    }
}
