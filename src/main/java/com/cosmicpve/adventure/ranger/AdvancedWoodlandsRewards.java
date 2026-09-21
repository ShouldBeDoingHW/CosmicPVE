package com.cosmicpve.adventure.ranger;

import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.content.definition.reward.*;
import com.cosmicpve.data.component.*;
import com.cosmicpve.economy.Banknotes;
import com.cosmicpve.equipment.armor.ArmorSetIds;
import com.cosmicpve.equipment.enchantment.*;
import com.cosmicpve.equipment.heroic.HeroicApplicationService;
import com.cosmicpve.registry.*;
import java.util.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class AdvancedWoodlandsRewards {
    public static final net.minecraft.resources.Identifier TABLE=com.cosmicpve.CosmicPVE.id("adventure/advanced_dense_woodlands");
    public static final long MIN_CENTS=100_000_000L,MAX_CENTS=400_000_000L,STEP_CENTS=1_000_000L;
    private AdvancedWoodlandsRewards(){}
    public static ItemStack pinpointBook(RandomSource random){
        var stack=new ItemStack(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        stack.set(ModDataComponents.COSMIC_ENCHANT_BOOK.get(),new CosmicEnchantmentBookData(
                CosmicEnchantmentBookData.CURRENT_DATA_VERSION,ModEnchantments.PINPOINT.identifier(),6,
                random.nextIntBetweenInclusive(1,100),random.nextIntBetweenInclusive(1,100)));return stack;
    }
    public static ItemStack banknote(RandomSource random){return Banknotes.create(banknoteCents(
            random.nextIntBetweenInclusive(0,(int)((MAX_CENTS-MIN_CENTS)/STEP_CENTS))));}
    public static long banknoteCents(int stepIndex){
        int maximumStep=(int)((MAX_CENTS-MIN_CENTS)/STEP_CENTS);
        if(stepIndex<0||stepIndex>maximumStep)throw new IllegalArgumentException("Banknote step outside canonical range");
        return MIN_CENTS+(long)stepIndex*STEP_CENTS;
    }
    public static ItemStack portal(int skip,int madness){var stack=new ItemStack(ModItems.TRIAL_PORTAL.get());
        com.cosmicpve.trial.portal.TrialPortalItem.applyModifiers(stack,new TrialPortalModifiers(
                TrialPortalModifiers.DATA_VERSION,0,skip,0,0,madness));return stack;}
    public static ItemStack randomRangerArmor(RegistryAccess access,RandomSource random){
        ItemStack stack=new ItemStack(List.of(Items.IRON_HELMET,Items.IRON_CHESTPLATE,Items.IRON_LEGGINGS,Items.IRON_BOOTS).get(random.nextInt(4)));
        HeroicApplicationService.applyState(stack,HeroicEquipmentKind.ARMOR);
        stack.set(ModDataComponents.ARMOR_SET_ID.get(),ArmorSetIdentity.from(CosmicContent.repository().requireArmorSetDefinition(ArmorSetIds.RANGER)));
        var registry=access.lookupOrThrow(Registries.ENCHANTMENT);
        var candidates=new ArrayList<>(CosmicEnchantmentSpecs.ALL.stream().filter(CosmicEnchantmentSpec::randomPoolEligible)
                .filter(s->s.tier()==CosmicEnchantmentTier.SIMPLE||s.tier()==CosmicEnchantmentTier.UNIQUE||s.tier()==CosmicEnchantmentTier.ELITE)
                .filter(s->registry.get(s.id()).filter(h->h.value().canEnchant(stack)).isPresent()).toList());
        for(int i=candidates.size()-1;i>0;i--){int j=random.nextInt(i+1);var t=candidates.get(i);candidates.set(i,candidates.get(j));candidates.set(j,t);}
        int count=random.nextIntBetweenInclusive(3,Math.min(5,candidates.size()));
        EnchantmentHelper.updateEnchantments(stack,m->{m.set(registry.getOrThrow(Enchantments.PROTECTION),4);m.set(registry.getOrThrow(Enchantments.UNBREAKING),3);m.set(registry.getOrThrow(Enchantments.MENDING),1);
            for(int i=0;i<count;i++){var s=candidates.get(i);m.set(registry.get(s.id()).orElseThrow(),Math.max(1,s.maxLevel()-random.nextInt(2)));}});return stack;
    }
}
