package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.equipment.enchantment.ActualEnchantmentGrant;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import com.cosmicpve.equipment.enchantment.EnchantmentSourceKind;
import com.cosmicpve.equipment.enchantment.HeroicEnchantments;
import com.cosmicpve.equipment.enchantment.VirtualEnchantmentGrant;
import com.cosmicpve.equipment.skin.WeaponSkinApplicationService;
import com.cosmicpve.equipment.skin.WeaponSkinDefinitions;
import com.cosmicpve.equipment.skin.WeaponSkinItemFactory;
import com.cosmicpve.equipment.skin.WeaponSkinResolver;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.junit.jupiter.api.Test;

class BossTankTitanMilestoneTest {
    @Test void bossSlayerThresholdIsInclusiveAndDamageIsLevelScaled() {
        assertEquals(CosmicPVE.id("boss_slayer"), ModEnchantments.BOSS_SLAYER.identifier());
        assertFalse(BossSlayerBehavior.qualifies(10, 29.99));
        assertTrue(BossSlayerBehavior.qualifies(10, 30));
        assertTrue(BossSlayerBehavior.qualifies(10, 30.01));
        assertEquals(.03, BossSlayerBehavior.bonus(1), 1e-12);
        assertEquals(.06, BossSlayerBehavior.bonus(2), 1e-12);
        assertEquals(.09, BossSlayerBehavior.bonus(3), 1e-12);
    }

    @Test void tankAggregatesFourArmorPiecesButStopsAtEight() {
        assertEquals(0, TankBehavior.aggregate());
        assertEquals(5, TankBehavior.aggregate(2, 3));
        assertEquals(8, TankBehavior.aggregate(4, 4));
        assertEquals(8, TankBehavior.aggregate(4, 4, 4, 4));
        assertEquals(.95, TankBehavior.incomingMultiplier(5), 1e-12);
        assertEquals(.92, TankBehavior.incomingMultiplier(16), 1e-12);
    }

    @Test void titanTrapReplacesOrdinaryAndVirtualFireworkNeverWritesAnEnchantment() {
        assertEquals(CosmicPVE.id("trap"), ModEnchantments.TRAP.identifier());
        assertEquals(ModEnchantments.TITAN_TRAP.identifier(),
                HeroicEnchantments.heroicFor(ModEnchantments.TRAP.identifier()).orElseThrow());
        assertEquals(.04, TitanTrapBehavior.bonus(1), 1e-12);
        assertEquals(.08, TitanTrapBehavior.bonus(2), 1e-12);
        assertEquals(.12, TitanTrapBehavior.bonus(3), 1e-12);

        var definition = WeaponSkinDefinitions.find(WeaponSkinDefinitions.FIREWORK_ROCKET).orElseThrow();
        assertEquals(0xD43700, definition.nameColor());
        assertTrue(definition.accepts(new ItemStack(Items.IRON_AXE)));
        assertFalse(definition.accepts(new ItemStack(Items.IRON_SWORD)));
        var skin = WeaponSkinItemFactory.create(WeaponSkinDefinitions.FIREWORK_ROCKET);
        var axe = new ItemStack(Items.DIAMOND_AXE);
        var applications = new WeaponSkinApplicationService();
        assertEquals(WeaponSkinApplicationService.ApplyOutcome.SUCCESS,
                applications.apply(skin, axe, skin, axe));
        assertFalse(EnchantmentHelper.hasAnyEnchantments(axe));
        List<VirtualEnchantmentGrant> grants = new WeaponSkinResolver().virtualEnchantments(axe);
        assertEquals(List.of(new VirtualEnchantmentGrant(ModEnchantments.TITAN_TRAP.identifier(), 3,
                WeaponSkinDefinitions.FIREWORK_ROCKET)), grants);
        var effective = new EffectiveEnchantmentsResolver().resolveSources(List.of(
                new ActualEnchantmentGrant(ModEnchantments.TRAP.identifier(), 3, CosmicPVE.id("actual_item")),
                new ActualEnchantmentGrant(ModEnchantments.TITAN_TRAP.identifier(), 1, CosmicPVE.id("actual_item"))), grants);
        assertEquals(0, effective.level(ModEnchantments.TRAP.identifier()));
        assertEquals(3, effective.level(ModEnchantments.TITAN_TRAP.identifier()));
        assertTrue(effective.get(ModEnchantments.TITAN_TRAP.identifier()).orElseThrow().provenance().stream()
                .anyMatch(source -> source.kind() == EnchantmentSourceKind.VIRTUAL && source.level() == 3));
        applications.remove(axe, axe, true);
        assertTrue(new WeaponSkinResolver().virtualEnchantments(axe).isEmpty());
    }
}
