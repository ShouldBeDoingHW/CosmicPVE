package com.cosmicpve.reward.lootbox;

import com.cosmicpve.data.component.AdminAbuseRewardIdentity;
import com.cosmicpve.data.component.CustomEnchantMetadata;
import com.cosmicpve.data.component.HeroicEquipmentKind;
import com.cosmicpve.data.component.ArmorSetIdentity;
import com.cosmicpve.data.component.SignatureWeaponIdentity;
import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.equipment.armor.ArmorSetIds;
import com.cosmicpve.equipment.heroic.HeroicApplicationService;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import java.util.function.IntUnaryOperator;
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
            case ASHOKA, IZANAGI -> new ItemStack(Items.NETHERITE_AXE);
            case IDATEN -> new ItemStack(Items.IRON_LEGGINGS);
            case FREEMAN_WALKERS, ETERNAL_STRIDERS -> new ItemStack(Items.IRON_BOOTS);
        };
        stack.set(ModDataComponents.ADMIN_ABUSE_REWARD.get(), new AdminAbuseRewardIdentity(
                AdminAbuseRewardIdentity.CURRENT_DATA_VERSION, outcome.id()));
        stack.set(DataComponents.CUSTOM_NAME, name(outcome));
        int orbs = switch (outcome) {
            case GHOSTLY_VEIL, COVERT_CLOAK, IDATEN, FREEMAN_WALKERS, ETERNAL_STRIDERS -> 3;
            case NANKADA, ASHOKA, IZANAGI -> 5;
        };
        stack.set(ModDataComponents.CUSTOM_ENCHANT_META.get(), new CustomEnchantMetadata(
                CustomEnchantMetadata.CURRENT_DATA_VERSION, CustomEnchantMetadata.DEFAULT_SLOT_LIMIT,
                orbs, true, true));
        // Predefined rewards intentionally retain White Scroll protection alongside typed Holy state.
        stack.set(ModDataComponents.HOLY.get(), true);
        switch (outcome) {
            case GHOSTLY_VEIL, COVERT_CLOAK -> stack.set(ModDataComponents.OMNI_ARMOR.get(), true);
            case IDATEN -> setArmorIdentity(stack, ArmorSetIds.DIMENSIONAL_TRAVELER);
            case FREEMAN_WALKERS -> setArmorIdentity(stack, ArmorSetIds.ENGINEER);
            case ETERNAL_STRIDERS -> setArmorIdentity(stack, ArmorSetIds.DRAGONSLAYER);
            case NANKADA -> setSignatureIdentity(stack, outcome, ArmorSetIds.YJIKI);
            case ASHOKA -> setSignatureIdentity(stack, outcome, ArmorSetIds.PHANTOM);
            case IZANAGI -> setSignatureIdentity(stack, outcome, ArmorSetIds.YETI);
        }
        if (switch (outcome) {
            case GHOSTLY_VEIL, COVERT_CLOAK, IDATEN, FREEMAN_WALKERS, ETERNAL_STRIDERS -> true;
            default -> false;
        }) HeroicApplicationService.applyState(stack, HeroicEquipmentKind.ARMOR);
        applyEnchantments(stack, outcome, access.lookupOrThrow(Registries.ENCHANTMENT));
        return stack;
    }

    private static void setArmorIdentity(ItemStack stack, net.minecraft.resources.Identifier setId) {
        var definition = CosmicContent.repository().findArmorSetDefinition(setId).orElseThrow(() ->
                new IllegalStateException("Missing Admin Abuse armor set: " + setId));
        stack.set(ModDataComponents.ARMOR_SET_ID.get(), ArmorSetIdentity.from(definition));
    }

    private static void setSignatureIdentity(ItemStack stack, AdminAbuseRewards.Outcome outcome,
            net.minecraft.resources.Identifier setId) {
        if (CosmicContent.repository().findArmorSetDefinition(setId).isEmpty())
            throw new IllegalStateException("Missing Admin Abuse weapon set: " + setId);
        stack.set(ModDataComponents.SIGNATURE_WEAPON.get(), new SignatureWeaponIdentity(
                SignatureWeaponIdentity.CURRENT_DATA_VERSION, outcome.id(), setId,
                SignatureWeaponIdentity.Kind.MELEE));
    }

    private static void applyEnchantments(ItemStack stack, AdminAbuseRewards.Outcome outcome,
            Registry<Enchantment> registry) {
        EnchantmentHelper.updateEnchantments(stack, mutable -> {
            switch (outcome) {
                case GHOSTLY_VEIL -> {
                    vanillaArmor(mutable, registry);
                    set(mutable, registry, ModEnchantments.MORTAL_COIL, 2); set(mutable, registry, ModEnchantments.PALADIN_ARMORED, 4);
                    set(mutable, registry, ModEnchantments.MOLTEN, 4); set(mutable, registry, ModEnchantments.ALIEN_IMPLANTS, 3);
                    set(mutable, registry, ModEnchantments.VOODOO, 6); set(mutable, registry, ModEnchantments.ENDER_SHIFT, 3);
                    set(mutable, registry, ModEnchantments.GLOWING, 1);
                }
                case COVERT_CLOAK -> {
                    vanillaArmor(mutable, registry);
                    set(mutable, registry, ModEnchantments.DEATH_PACT, 5); set(mutable, registry, ModEnchantments.PERMAFROST, 6);
                    set(mutable, registry, ModEnchantments.PALADIN_ARMORED, 4); set(mutable, registry, ModEnchantments.GODLY_OVERLOAD, 3);
                    set(mutable, registry, ModEnchantments.AEGIS, 6); set(mutable, registry, ModEnchantments.ANGELIC, 5);
                    set(mutable, registry, ModEnchantments.FORBIDDEN_CURSE, 5); set(mutable, registry, ModEnchantments.STORMCALLER, 5);
                }
                case NANKADA -> {
                    set(mutable, registry, Enchantments.SHARPNESS, 5); set(mutable, registry, Enchantments.UNBREAKING, 3);
                    set(mutable, registry, Enchantments.MENDING, 1); set(mutable, registry, Enchantments.FIRE_ASPECT, 2);
                    set(mutable, registry, ModEnchantments.SOUL_SIPHON, 4); set(mutable, registry, ModEnchantments.BLACKOUT, 4);
                    set(mutable, registry, ModEnchantments.RAGE, 6); set(mutable, registry, ModEnchantments.DOUBLESTRIKE, 3);
                    set(mutable, registry, ModEnchantments.PERMANENT_EXECUTE, 5); set(mutable, registry, ModEnchantments.GREATSWORD, 4);
                    set(mutable, registry, ModEnchantments.POISON, 3); set(mutable, registry, ModEnchantments.SILENCE, 4);
                    set(mutable, registry, ModEnchantments.TITAN_TRAP, 3); set(mutable, registry, ModEnchantments.THUNDERING_BLOW, 3);
                }
                case ASHOKA -> {
                    set(mutable, registry, Enchantments.SHARPNESS, 5); set(mutable, registry, Enchantments.UNBREAKING, 3);
                    set(mutable, registry, Enchantments.MENDING, 1);
                    set(mutable, registry, ModEnchantments.HERO_KILLER, 3); set(mutable, registry, ModEnchantments.SOUL_TETHER, 3);
                    set(mutable, registry, ModEnchantments.SOUL_SIPHON, 4); set(mutable, registry, ModEnchantments.RAGE, 6);
                    set(mutable, registry, ModEnchantments.DEEP_BLEED, 6); set(mutable, registry, ModEnchantments.DEVOUR, 4);
                    set(mutable, registry, ModEnchantments.SILENCE, 4); set(mutable, registry, ModEnchantments.PYRE, 3);
                    set(mutable, registry, ModEnchantments.PUMMEL, 3); set(mutable, registry, ModEnchantments.INSANITY, 8);
                }
                case IDATEN -> {
                    vanillaArmor(mutable, registry);
                    set(mutable, registry, ModEnchantments.PALADIN_ARMORED, 4); set(mutable, registry, ModEnchantments.MIGHTY_CACTUS, 2);
                    set(mutable, registry, ModEnchantments.EPIDEMIC_CARRIER, 7); set(mutable, registry, ModEnchantments.SELF_DESTRUCT, 3);
                    set(mutable, registry, ModEnchantments.ANGELIC, 5); set(mutable, registry, ModEnchantments.OBSIDIANSHIELD, 2);
                    set(mutable, registry, ModEnchantments.TANK, 4); set(mutable, registry, ModEnchantments.LUCK, 10);
                }
                case FREEMAN_WALKERS -> {
                    vanillaArmor(mutable, registry);
                    set(mutable, registry, ModEnchantments.PALADIN_ARMORED, 4); set(mutable, registry, ModEnchantments.TANK, 4);
                    set(mutable, registry, ModEnchantments.GEARS, 3); set(mutable, registry, ModEnchantments.PHOENIX, 3);
                    set(mutable, registry, ModEnchantments.STORMCALLER, 5); set(mutable, registry, ModEnchantments.NIMBLE, 4);
                    set(mutable, registry, ModEnchantments.LUCK, 10); set(mutable, registry, ModEnchantments.DODGE, 5);
                }
                case IZANAGI -> {
                    set(mutable, registry, Enchantments.SHARPNESS, 5); set(mutable, registry, Enchantments.UNBREAKING, 3);
                    set(mutable, registry, Enchantments.MENDING, 1);
                    set(mutable, registry, ModEnchantments.DEEP_BLEED, 6); set(mutable, registry, ModEnchantments.MIGHTY_CLEAVE, 8);
                    set(mutable, registry, ModEnchantments.INSANITY, 8); set(mutable, registry, ModEnchantments.HEX, 5);
                    set(mutable, registry, ModEnchantments.DEVOUR, 4); set(mutable, registry, ModEnchantments.BOSS_SLAYER, 3);
                    set(mutable, registry, ModEnchantments.BLESSED, 4); set(mutable, registry, ModEnchantments.BERSERK, 5);
                    set(mutable, registry, ModEnchantments.ANTI_GANK, 4); set(mutable, registry, ModEnchantments.OBLITERATE, 3);
                }
                case ETERNAL_STRIDERS -> {
                    vanillaArmor(mutable, registry);
                    set(mutable, registry, ModEnchantments.LUCK, 10); set(mutable, registry, ModEnchantments.DODGE, 5);
                    set(mutable, registry, ModEnchantments.PALADIN_ARMORED, 4); set(mutable, registry, ModEnchantments.ENDER_WALKER, 5);
                    set(mutable, registry, ModEnchantments.GEARS, 3); set(mutable, registry, ModEnchantments.PHOENIX, 3);
                    set(mutable, registry, ModEnchantments.NIMBLE, 4); set(mutable, registry, ModEnchantments.ANGELIC, 5);
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
            case GHOSTLY_VEIL -> styled("Ghostly Veil", 0x061630);
            case COVERT_CLOAK -> styled("Covert Cloak", 0x061630);
            case NANKADA -> outer("N", 0x555555, "ankad", 0xFFE578, "a", 0x555555);
            case ASHOKA -> outer("A", 0xFFFFFF, "shok", 0x103963, "a", 0xFFFFFF);
            case IDATEN -> decorative("✦~=- Idaten -=~✦", true, value -> value == '=' ? 0x555555 : 0xAA0000);
            case FREEMAN_WALKERS -> decorative("~ -= Freeman Walkers =- ~", true,
                    value -> value == '~' ? 0xFFFFFF : value == '-' || value == '=' ? 0xAA00AA : 0xFF55FF);
            case IZANAGI -> decorative("-=/=- Izanagi -=\\=-", true, value -> switch (value) {
                case '-' -> 0x555555;
                case '=' -> 0xAAAAAA;
                case '/', '\\' -> 0xFFFFFF;
                case 'I', 'a', 'g' -> 0xFFAA00;
                default -> 0xFFFF55;
            });
            case ETERNAL_STRIDERS -> decorative("~=- Eternal Striders -=~", true,
                    value -> value == '-' ? 0xFFFFFF : 0x55FFFF);
        };
    }
    private static MutableComponent styled(String text, int color) {
        return Component.literal(text).withStyle(style -> style.withColor(color).withBold(true).withItalic(true).withStrikethrough(true));
    }
    private static MutableComponent outer(String first, int firstColor, String middle, int middleColor, String last, int lastColor) {
        return styled(first, firstColor).append(styled(middle, middleColor)).append(styled(last, lastColor));
    }
    private static MutableComponent decorative(String text, boolean italic, IntUnaryOperator colorForCharacter) {
        var result = Component.empty();
        for (int i = 0; i < text.length(); i++) {
            char value = text.charAt(i);
            int color = colorForCharacter.applyAsInt(value);
            result.append(Component.literal(Character.toString(value))
                    .withStyle(style -> style.withColor(color).withItalic(italic)));
        }
        return result;
    }
}
