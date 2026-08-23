package com.cosmicpve.equipment.repair;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.data.component.CustomEnchantMetadata;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class RepairScrollServiceTest {
    private final RepairScrollService service = new RepairScrollService();
    @Test void repairsCurrentMaximumInPlaceAndPreservesComponents() {
        var target = new ItemStack(Items.DIAMOND_PICKAXE);
        target.set(DataComponents.MAX_DAMAGE, target.getMaxDamage() + 250);
        target.setDamageValue(200);
        target.set(DataComponents.CUSTOM_NAME, Component.literal("Heroic Test"));
        var meta = new CustomEnchantMetadata(1, 5, 2, true, true);
        target.set(ModDataComponents.CUSTOM_ENCHANT_META.get(), meta);
        var scroll = new ItemStack(ModItems.REPAIR_SCROLL.get());
        int max = target.getMaxDamage();
        assertEquals(RepairScrollService.Outcome.SUCCESS, service.apply(scroll, target, target));
        assertEquals(0, target.getDamageValue());
        assertEquals(max, target.getMaxDamage());
        assertEquals(meta, target.get(ModDataComponents.CUSTOM_ENCHANT_META.get()));
        assertEquals("Heroic Test", target.getHoverName().getString());
        assertTrue(scroll.isEmpty());
    }
    @Test void fullNonDamageableAndStaleTargetsRejectWithoutConsumption() {
        var scroll = new ItemStack(ModItems.REPAIR_SCROLL.get());
        var full = new ItemStack(Items.IRON_SWORD);
        assertEquals(RepairScrollService.Outcome.ALREADY_REPAIRED, service.apply(scroll, full, full));
        var paper = new ItemStack(Items.PAPER);
        assertEquals(RepairScrollService.Outcome.NOT_DAMAGEABLE, service.apply(scroll, paper, paper));
        var damaged = new ItemStack(Items.IRON_SWORD); damaged.setDamageValue(1);
        assertEquals(RepairScrollService.Outcome.STALE_TARGET,
                service.apply(scroll, damaged, damaged.copy()));
        assertEquals(1, scroll.getCount());
    }
}
