package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.proc.ProcChance;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SpiritLinkBehaviorTest {
    @Test void descriptorAggregateChanceHealAndBonusAreExact() {
        assertEquals("cosmicpve:spirit_link", CosmicEnchantmentSpecs.SPIRIT_LINK.id().toString());
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.SPIRIT_LINK.tier());
        assertEquals(7, CosmicEnchantmentSpecs.SPIRIT_LINK.maxLevel());
        assertEquals("helmet_or_chestplate", CosmicEnchantmentSpecs.SPIRIT_LINK.equipmentApplicability());
        assertTrue(CosmicEnchantmentSpecs.SPIRIT_LINK.tier().extractableByBlackScroll());
        assertEquals(4, SpiritLinkBehavior.aggregateLevels(4, 0));
        assertEquals(7, SpiritLinkBehavior.aggregateLevels(4, 3));
        assertEquals(14, SpiritLinkBehavior.aggregateLevels(7, 7));
        assertEquals(SpiritLinkBehavior.BASE_CHANCE, .05, 1e-12);
        assertEquals(.06, ProcChance.calculate(SpiritLinkBehavior.BASE_CHANCE, List.of(1.2)), 1e-12);
        assertEquals(1.0F, SpiritLinkBehavior.healing(2));
        assertEquals(3.5F, SpiritLinkBehavior.healing(7));
        assertEquals(7.0F, SpiritLinkBehavior.healing(14));
        assertEquals(.07, SpiritLinkBehavior.attackBonus(7), 1e-12);
        assertEquals(.14, SpiritLinkBehavior.attackBonus(14), 1e-12);
    }

    @Test void allySelectionIsUniformlyPartitionedAndRequiresARealCandidateSet() {
        assertEquals(0, SpiritLinkBehavior.selectedAllyIndex(0.0, 2));
        assertEquals(0, SpiritLinkBehavior.selectedAllyIndex(.499999, 2));
        assertEquals(1, SpiritLinkBehavior.selectedAllyIndex(.5, 2));
        assertEquals(1, SpiritLinkBehavior.selectedAllyIndex(.999999, 2));
        assertThrows(IllegalArgumentException.class, () -> SpiritLinkBehavior.selectedAllyIndex(0, 0));
    }

    @Test void storedChargeReplacesRatherThanStacksAndCanBeConsumedOnce() {
        var behavior = new SpiritLinkBehavior();
        UUID wearer = UUID.randomUUID();
        behavior.storeForTest(wearer, 7);
        assertTrue(behavior.hasCharge(wearer));
        assertEquals(.07, behavior.storedBonus(wearer), 1e-12);
        behavior.storeForTest(wearer, 14);
        assertEquals(.14, behavior.storedBonus(wearer), 1e-12);
        behavior.consumeForTest(wearer);
        assertFalse(behavior.hasCharge(wearer));
        behavior.consumeForTest(wearer);
        assertEquals(0.0, behavior.storedBonus(wearer), 1e-12);
    }

    @Test void chargeOnlyModifiesOrdinaryParentMeleeOrProjectileDamage() {
        assertEquals(.14, SpiritLinkBehavior.contribution(
                DamageChannel.ORDINARY, AttackCategory.MELEE, true, .14).getFirst().bonus(), 1e-12);
        assertEquals(.14, SpiritLinkBehavior.contribution(
                DamageChannel.ORDINARY, AttackCategory.PROJECTILE, true, .14).getFirst().bonus(), 1e-12);
        assertTrue(SpiritLinkBehavior.contribution(
                DamageChannel.TRUE, AttackCategory.MELEE, true, .14).isEmpty());
        assertTrue(SpiritLinkBehavior.contribution(
                DamageChannel.ORDINARY, AttackCategory.MELEE, false, .14).isEmpty());
        assertTrue(SpiritLinkBehavior.contribution(
                DamageChannel.ORDINARY, AttackCategory.ENVIRONMENTAL, true, .14).isEmpty());
    }
}
