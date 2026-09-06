package com.cosmicpve.equipment.heroic;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.data.component.ArmorSetIdentity;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import com.cosmicpve.data.component.CustomEnchantMetadata;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundEvents;
import com.cosmicpve.equipment.enchantment.ItemApplicationFeedback;
import org.junit.jupiter.api.Test;

class HeroicApplicationServiceTest {
    private final HeroicApplicationService service = new HeroicApplicationService();

    @Test void crystalIsNonStackableAndSuccessUsesStandardLevelUpCue() {
        assertEquals(1, new ItemStack(ModItems.HEROIC_CRYSTAL.get()).getMaxStackSize());
        assertEquals(List.of(SoundEvents.PLAYER_LEVELUP),
                ItemApplicationFeedback.soundsFor(ItemApplicationFeedback.Cue.SUCCESS));
    }

    @Test void crystalUsesCanonicalPurpleNameAndYellowBodyLore() {
        var crystal = new ItemStack(ModItems.HEROIC_CRYSTAL.get());
        assertEquals(HeroicCrystalItem.NAME_COLOR, crystal.getHoverName().getStyle().getColor().getValue());
        assertTrue(crystal.getHoverName().getStyle().isBold());
        assertTrue(crystal.hasFoil());
        assertEquals(1, HeroicCrystalItem.lore().size());
        assertEquals("Gives +250 durability to pickaxes, shovels, and armor as a one-time bonus!",
                HeroicCrystalItem.lore().getFirst().getString());
    }

    @Test void appliesOnceAddsExactly250AndPreservesWearAndMetadata() {
        var crystal = new ItemStack(ModItems.HEROIC_CRYSTAL.get());
        var pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        pickaxe.setDamageValue(37);
        pickaxe.set(DataComponents.CUSTOM_NAME, Component.literal("Kept"));
        var metadata = new CustomEnchantMetadata(1, 5, 2, true, true);
        pickaxe.set(ModDataComponents.CUSTOM_ENCHANT_META.get(), metadata);
        int before = pickaxe.getMaxDamage();
        assertEquals(HeroicApplicationService.Outcome.SUCCESS, service.apply(crystal, pickaxe, pickaxe));
        assertEquals(before + 250, pickaxe.getMaxDamage());
        assertEquals(37, pickaxe.getDamageValue());
        assertEquals("Kept", pickaxe.getHoverName().getString());
        assertEquals(metadata, pickaxe.get(ModDataComponents.CUSTOM_ENCHANT_META.get()));
        assertTrue(pickaxe.has(ModDataComponents.HEROIC.get()));
        assertTrue(crystal.isEmpty());

        var second = new ItemStack(ModItems.HEROIC_CRYSTAL.get());
        assertEquals(HeroicApplicationService.Outcome.ALREADY_HEROIC, service.apply(second, pickaxe, pickaxe));
        assertEquals(before + 250, pickaxe.getMaxDamage());
        assertEquals(1, second.getCount());
    }

    @Test void supportsArmorBothOrdersAndRejectsInvalidOrStaleTargets() {
        var identity = new ArmorSetIdentity(1, net.minecraft.resources.Identifier.parse("cosmicpve:phantom"),
                Component.literal("Phantom"), 0x00AAFF, List.of(Component.literal("Bonus")));
        var armor = new ItemStack(Items.IRON_CHESTPLATE);
        armor.set(ModDataComponents.ARMOR_SET_ID.get(), identity);
        var crystal = new ItemStack(ModItems.HEROIC_CRYSTAL.get());
        assertEquals(HeroicApplicationService.Outcome.SUCCESS, service.apply(crystal, armor, armor));
        assertEquals(identity, armor.get(ModDataComponents.ARMOR_SET_ID.get()));
        assertTrue(armor.has(ModDataComponents.HEROIC.get()));

        var reverse = new ItemStack(Items.IRON_CHESTPLATE);
        var reverseCrystal = new ItemStack(ModItems.HEROIC_CRYSTAL.get());
        assertEquals(HeroicApplicationService.Outcome.SUCCESS,
                service.apply(reverseCrystal, reverse, reverse));
        reverse.set(ModDataComponents.ARMOR_SET_ID.get(), identity);
        assertTrue(reverse.has(ModDataComponents.HEROIC.get()));
        assertEquals(identity, reverse.get(ModDataComponents.ARMOR_SET_ID.get()));

        var invalid = new ItemStack(Items.DIAMOND_AXE);
        var other = new ItemStack(ModItems.HEROIC_CRYSTAL.get());
        assertEquals(HeroicApplicationService.Outcome.INVALID_TARGET, service.apply(other, invalid, invalid));
        assertEquals(HeroicApplicationService.Outcome.STALE_TARGET,
                service.apply(other, new ItemStack(Items.IRON_SHOVEL), new ItemStack(Items.IRON_SHOVEL)));
        assertEquals(1, other.getCount());
    }
}
