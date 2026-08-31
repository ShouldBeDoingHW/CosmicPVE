package com.cosmicpve.reward.lootbox;

import com.cosmicpve.data.component.AdminAbuseRewardIdentity;
import com.cosmicpve.data.component.CustomEnchantMetadata;
import com.cosmicpve.data.component.HeroicEquipmentKind;
import com.cosmicpve.equipment.heroic.HeroicApplicationService;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class AdminAbuseRewardFactory {
    public ItemStack create(AdminAbuseRewards.Outcome outcome, RegistryAccess access) {
        ItemStack stack = switch (outcome) {
            case GHOSTLY_VEIL -> new ItemStack(Items.IRON_HELMET);
            case COVERT_CLOAK -> new ItemStack(Items.IRON_CHESTPLATE);
            case NANKADA -> new ItemStack(Items.NETHERITE_SWORD);
            case ASHOKA -> new ItemStack(Items.NETHERITE_AXE);
        };
        stack.set(ModDataComponents.ADMIN_ABUSE_REWARD.get(), new AdminAbuseRewardIdentity(
                AdminAbuseRewardIdentity.CURRENT_DATA_VERSION, outcome.id()));
        stack.set(DataComponents.CUSTOM_NAME, name(outcome));
        int orbs = switch (outcome) {
            case GHOSTLY_VEIL, NANKADA -> 2;
            case COVERT_CLOAK, ASHOKA -> 1;
        };
        stack.set(ModDataComponents.CUSTOM_ENCHANT_META.get(), new CustomEnchantMetadata(
                CustomEnchantMetadata.CURRENT_DATA_VERSION, CustomEnchantMetadata.DEFAULT_SLOT_LIMIT,
                orbs, true, true));
        if (outcome == AdminAbuseRewards.Outcome.GHOSTLY_VEIL || outcome == AdminAbuseRewards.Outcome.COVERT_CLOAK) {
            stack.set(ModDataComponents.OMNI_ARMOR.get(), true);
            HeroicApplicationService.applyState(stack, HeroicEquipmentKind.ARMOR);
        }
        applyEnchantments(stack, outcome, access.lookupOrThrow(Registries.ENCHANTMENT));
        return stack;
    }

    private static void applyEnchantments(ItemStack stack, AdminAbuseRewards.Outcome outcome,
            Registry<Enchantment> registry) {
        EnchantmentHelper.updateEnchantments(stack, mutable -> {
            switch (outcome) {
                case GHOSTLY_VEIL -> {
                    vanillaArmor(mutable, registry);
                    set(mutable, registry, ModEnchantments.MORTAL_COIL, 2); set(mutable, registry, ModEnchantments.ARMORED, 4);
                    set(mutable, registry, ModEnchantments.MOLTEN, 4); set(mutable, registry, ModEnchantments.IMPLANTS, 3);
                    set(mutable, registry, ModEnchantments.VOODOO, 6); set(mutable, registry, ModEnchantments.ENDER_SHIFT, 3);
                    set(mutable, registry, ModEnchantments.GLOWING, 1);
                }
                case COVERT_CLOAK -> {
                    vanillaArmor(mutable, registry);
                    set(mutable, registry, ModEnchantments.DEATH_PACT, 5); set(mutable, registry, ModEnchantments.PERMAFROST, 6);
                    set(mutable, registry, ModEnchantments.ARMORED, 4); set(mutable, registry, ModEnchantments.AEGIS, 6);
                    set(mutable, registry, ModEnchantments.ANGELIC, 5); set(mutable, registry, ModEnchantments.UNDEAD_RUSE, 10);
                }
                case NANKADA -> {
                    set(mutable, registry, Enchantments.SHARPNESS, 5); set(mutable, registry, Enchantments.UNBREAKING, 3);
                    set(mutable, registry, Enchantments.MENDING, 1); set(mutable, registry, Enchantments.FIRE_ASPECT, 2);
                    set(mutable, registry, ModEnchantments.SOUL_SIPHON, 4); set(mutable, registry, ModEnchantments.BLACKOUT, 4);
                    set(mutable, registry, ModEnchantments.RAGE, 6); set(mutable, registry, ModEnchantments.DOUBLESTRIKE, 3);
                    set(mutable, registry, ModEnchantments.EXECUTE, 5); set(mutable, registry, ModEnchantments.GREATSWORD, 4);
                    set(mutable, registry, ModEnchantments.POISON, 3);
                }
                case ASHOKA -> {
                    set(mutable, registry, Enchantments.SHARPNESS, 5); set(mutable, registry, Enchantments.UNBREAKING, 3);
                    set(mutable, registry, Enchantments.MENDING, 1);
                    set(mutable, registry, ModEnchantments.HERO_KILLER, 3); set(mutable, registry, ModEnchantments.SOUL_TETHER, 3);
                    set(mutable, registry, ModEnchantments.SOUL_SIPHON, 4); set(mutable, registry, ModEnchantments.RAGE, 6);
                    set(mutable, registry, ModEnchantments.DEVOUR, 4); set(mutable, registry, ModEnchantments.PUMMEL, 3);
                }
            }
        });
    }
    private static void vanillaArmor(net.minecraft.world.item.enchantment.ItemEnchantments.Mutable mutable,
            Registry<Enchantment> registry) {
        set(mutable, registry, Enchantments.PROTECTION, 4); set(mutable, registry, Enchantments.UNBREAKING, 3);
        set(mutable, registry, Enchantments.MENDING, 1);
    }
    private static void set(net.minecraft.world.item.enchantment.ItemEnchantments.Mutable mutable,
            Registry<Enchantment> registry, ResourceKey<Enchantment> key, int level) {
        mutable.set(registry.getOrThrow(key), level);
    }
    public static Component name(AdminAbuseRewards.Outcome outcome) {
        return switch (outcome) {
            case GHOSTLY_VEIL -> styled("Ghostly Veil", 0x345FA8);
            case COVERT_CLOAK -> styled("Covert Cloak", 0x345FA8);
            case NANKADA -> outer("N", 0xB8B8B8, "ankad", 0xFFE578, "a", 0xB8B8B8);
            case ASHOKA -> outer("A", 0xFFFFFF, "shok", 0x397AB8, "a", 0xFFFFFF);
        };
    }
    private static MutableComponent styled(String text, int color) {
        return Component.literal(text).withStyle(style -> style.withColor(color).withBold(true).withItalic(true).withStrikethrough(true));
    }
    private static MutableComponent outer(String first, int firstColor, String middle, int middleColor, String last, int lastColor) {
        return styled(first, firstColor).append(styled(middle, middleColor)).append(styled(last, lastColor));
    }
}
