package com.cosmicpve.adventure.ranger;

import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.data.component.ArmorSetIdentity;
import com.cosmicpve.data.component.HeroicEquipmentKind;
import com.cosmicpve.equipment.armor.ArmorSetIds;
import com.cosmicpve.equipment.heroic.HeroicApplicationService;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class CosmicRangerEquipment {
    private CosmicRangerEquipment() {}
    public static ItemStack armor(EquipmentSlot slot, RegistryAccess access) {
        ItemStack stack=new ItemStack(switch(slot){case HEAD->Items.IRON_HELMET;case CHEST->Items.IRON_CHESTPLATE;
            case LEGS->Items.IRON_LEGGINGS;case FEET->Items.IRON_BOOTS;default->throw new IllegalArgumentException("Armor slot required");});
        HeroicApplicationService.applyState(stack, HeroicEquipmentKind.ARMOR);
        stack.set(ModDataComponents.ARMOR_SET_ID.get(), ArmorSetIdentity.from(
                CosmicContent.repository().requireArmorSetDefinition(ArmorSetIds.RANGER)));
        var registry=access.lookupOrThrow(Registries.ENCHANTMENT);
        EnchantmentHelper.updateEnchantments(stack, mutable->{
            set(mutable,registry,Enchantments.PROTECTION,4);
            for(var e: cosmic(slot))set(mutable,registry,e.key(),e.level());
        });
        return stack;
    }
    public static ItemStack bow(RegistryAccess access) {
        var stack=new ItemStack(Items.BOW);var registry=access.lookupOrThrow(Registries.ENCHANTMENT);
        EnchantmentHelper.updateEnchantments(stack,m->{
            set(m,registry,Enchantments.POWER,5);set(m,registry,Enchantments.FLAME,1);
            set(m,registry,ModEnchantments.LIGHTNING,4);set(m,registry,ModEnchantments.OBLITERATE,3);
            set(m,registry,ModEnchantments.VIRUS,3);set(m,registry,ModEnchantments.PINPOINT,6);
            set(m,registry,ModEnchantments.SOLITUDE,3);set(m,registry,ModEnchantments.VENOM,3);
        });return stack;
    }
    private static List<E> cosmic(EquipmentSlot slot){return switch(slot){
        case HEAD->List.of(new E(ModEnchantments.ENDER_SHIFT,3),new E(ModEnchantments.SPIRIT_LINK,7),new E(ModEnchantments.VOODOO,6));
        case CHEST->List.of(new E(ModEnchantments.CURSE,5),new E(ModEnchantments.SPIRIT_LINK,7),new E(ModEnchantments.STORMCALLER,5),new E(ModEnchantments.UNDEAD_RUSE,10),new E(ModEnchantments.LEADERSHIP,10));
        case LEGS->List.of(new E(ModEnchantments.PLAGUE_CARRIER,7),new E(ModEnchantments.SELF_DESTRUCT,3),new E(ModEnchantments.CACTUS,2));
        case FEET->List.of(new E(ModEnchantments.NIMBLE,4),new E(ModEnchantments.STORMCALLER,5),new E(ModEnchantments.MOLTEN,4));
        default->List.of();};}
    private static void set(net.minecraft.world.item.enchantment.ItemEnchantments.Mutable m,Registry<Enchantment> r,ResourceKey<Enchantment> k,int l){m.set(r.getOrThrow(k),l);}
    private record E(ResourceKey<Enchantment> key,int level){}
}
