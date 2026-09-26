package com.cosmicpve.trial;

import com.cosmicpve.combat.enchantment.DeathCoffinBehavior;
import com.cosmicpve.combat.enchantment.PyreBehavior;
import com.cosmicpve.content.definition.reward.RewardDescriptor;
import com.cosmicpve.content.definition.reward.RewardEntry;
import com.cosmicpve.content.definition.reward.RewardTable;
import com.cosmicpve.reward.RewardTableService;
import com.cosmicpve.equipment.accessory.BeltDefinition;
import com.cosmicpve.equipment.accessory.CinderwolfProcFactor;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.trial.room.CinderWolfService;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CinderWolfMilestoneTest {
    @Test void deathCoffinAndPyreAreOrdinaryAcquisitionSpecs() {
        assertEquals(CosmicEnchantmentTier.LEGENDARY, CosmicEnchantmentSpecs.DEATH_COFFIN.tier());
        assertEquals(3, CosmicEnchantmentSpecs.DEATH_COFFIN.maxLevel());
        assertEquals("all_weapons", CosmicEnchantmentSpecs.DEATH_COFFIN.equipmentApplicability());
        assertTrue(CosmicEnchantmentSpecs.DEATH_COFFIN.randomPoolEligible());
        assertEquals(CosmicEnchantmentTier.SIMPLE, CosmicEnchantmentSpecs.PYRE.tier());
        assertEquals(3, CosmicEnchantmentSpecs.PYRE.maxLevel());
        assertEquals("axe", CosmicEnchantmentSpecs.PYRE.equipmentApplicability());
        assertTrue(CosmicEnchantmentSpecs.PYRE.randomPoolEligible());
        assertTrue(DeathCoffinBehavior.belowThreshold(32.9, 100));
        assertFalse(DeathCoffinBehavior.belowThreshold(33.0, 100));
        assertFalse(DeathCoffinBehavior.belowThreshold(0, 100));
        assertArrayEquals(new int[]{1, 2, 3}, new int[]{DeathCoffinBehavior.radius(1),
                DeathCoffinBehavior.radius(2), DeathCoffinBehavior.radius(3)});
        assertEquals(.05, PyreBehavior.chance(1), 1e-9);
        assertEquals(.10, PyreBehavior.chance(2), 1e-9);
        assertEquals(.15, PyreBehavior.chance(3), 1e-9);
        assertEquals(60, PyreBehavior.FIRE_TICKS);
    }

    @Test void cinderwolfBeltUsesAdditiveLuckFactorOnlyForThreeFireEnchants() {
        assertEquals("Belt: Cinderwolf", BeltDefinition.CINDERWOLF.displayName());
        assertEquals(0x852C14, BeltDefinition.CINDERWOLF.color());
        assertEquals(1.15, CinderwolfProcFactor.boostedLuckFactor(1.0), 1e-9);
        assertEquals(1.35, CinderwolfProcFactor.boostedLuckFactor(1.20), 1e-9);
        assertEquals(.1725, PyreBehavior.chance(3) * CinderwolfProcFactor.boostedLuckFactor(1.0), 1e-9);
        assertEquals(.135, PyreBehavior.chance(2) * CinderwolfProcFactor.boostedLuckFactor(1.20), 1e-9);
        assertTrue(CinderwolfProcFactor.ELIGIBLE.contains(ModEnchantments.PYRE.identifier()));
        assertTrue(CinderwolfProcFactor.ELIGIBLE.contains(ModEnchantments.MOLTEN.identifier()));
        assertTrue(CinderwolfProcFactor.ELIGIBLE.contains(ModEnchantments.DIVINE_IMMOLATION.identifier()));
        assertFalse(CinderwolfProcFactor.ELIGIBLE.contains(ModEnchantments.HEALING.identifier()));
        assertFalse(CinderwolfProcFactor.ELIGIBLE.contains(ModEnchantments.DEATH_COFFIN.identifier()));
    }

    @Test void roomConstantsRewardRowAndAuthoredSourceAreExact() throws Exception {
        assertTrue(TrialSessionService.HARDCORE_NATIVE_ROOMS.contains(TrialSessionService.CINDER_WOLF));
        assertTrue(TrialSessionService.roomPool(TrialPhase.IMPOSSIBLE).contains(TrialSessionService.CINDER_WOLF));
        assertTrue(TrialSessionService.roomPool(TrialPhase.DEMONIC).contains(TrialSessionService.CINDER_WOLF));
        assertEquals(5, TrialSessionService.CINDERWOLF_BELT_REWARD.weight());
        assertEquals(1, TrialSessionService.CINDERWOLF_BELT_REWARD.minimumQuantity());
        assertEquals(1, TrialSessionService.CINDERWOLF_BELT_REWARD.maximumQuantity());
        assertEquals("cosmicpve:cinderwolf_belt", ((RewardDescriptor.StaticItem)
                TrialSessionService.CINDERWOLF_BELT_REWARD.reward()).itemId().toString());
        assertEquals(200, CinderWolfService.bossHealth(1));
        assertEquals(350, CinderWolfService.bossHealth(2));
        assertEquals(500, CinderWolfService.bossHealth(3));
        assertEquals(650, CinderWolfService.bossHealth(4));
        assertEquals(3, CinderWolfService.pupsPerWave(1));
        assertEquals(6, CinderWolfService.pupsPerWave(4));
        assertEquals(160, CinderWolfService.FIREBALL_INTERVAL);
        assertEquals(240, CinderWolfService.PUP_INTERVAL);
        assertEquals(100, CinderWolfService.FIRE_TICKS);
        assertEquals(2.5, CinderWolfService.EXPLOSION_RADIUS);
        assertEquals(2.0, CinderWolfService.FIREBALL_TRUE_DAMAGE);
        assertEquals(java.util.List.of(net.minecraft.core.Direction.NORTH, net.minecraft.core.Direction.EAST,
                net.minecraft.core.Direction.SOUTH, net.minecraft.core.Direction.WEST),
                CinderWolfService.FIREBALL_DIRECTIONS);
        assertEquals(2.0, CinderWolfService.fireballPacket().amount());
        assertTrue(CinderWolfService.fireballPacket().bypassesArmor()
                && CinderWolfService.fireballPacket().bypassesAbsorption()
                && CinderWolfService.fireballPacket().bypassesCustomReduction());
        Path project = Path.of(System.getProperty("cosmicpve.projectDir", "."));
        assertTrue(Files.isRegularFile(project.resolve("trial rooms/cinderwolf.nbt")));
        assertTrue(Files.isRegularFile(project.resolve("src/main/resources/assets/cosmicpve/textures/entity/cinder_wolf.png")));
    }

    @Test void conditionalRewardRowIsEphemeralAndOccupiesOneWeightedSlot() {
        var ordinary = new RewardEntry(7, 1, 1,
                new RewardDescriptor.StaticItem(com.cosmicpve.CosmicPVE.id("white_scroll")));
        var base = new RewardTable(com.cosmicpve.CosmicPVE.id("trial/test"), java.util.List.of(ordinary), 7);
        var augmented = RewardTableService.withExtra(base, TrialSessionService.CINDERWOLF_BELT_REWARD);
        assertEquals(1, base.entries().size());
        assertEquals(7, base.totalWeight());
        assertEquals(2, augmented.entries().size());
        assertEquals(12, augmented.totalWeight());
        assertSame(ordinary, augmented.entries().getFirst());
        assertSame(TrialSessionService.CINDERWOLF_BELT_REWARD, augmented.entries().getLast());
    }
}
