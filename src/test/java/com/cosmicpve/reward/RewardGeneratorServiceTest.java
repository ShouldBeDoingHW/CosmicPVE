package com.cosmicpve.reward;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.content.definition.reward.RewardDescriptor;
import com.cosmicpve.data.component.BanknoteData;
import com.cosmicpve.data.component.BlackScrollData;
import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import com.cosmicpve.data.component.EnchantmentOrbData;
import com.cosmicpve.data.component.MobSpawnerData;
import com.cosmicpve.data.component.UnexaminedBookData;
import com.cosmicpve.data.component.EnchantedBlackScrollData;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class RewardGeneratorServiceTest {
    private final RewardGeneratorService service = new RewardGeneratorService();

    @Test void staticItemsAndExactBanknoteDenominationsUseCanonicalFactories() {
        assertEquals(Items.GOLDEN_APPLE, generate(new RewardDescriptor.StaticItem(
                Identifier.parse("minecraft:golden_apple"))).getItem());
        var note = generate(new RewardDescriptor.Banknote(5_000_000));
        assertEquals(ModItems.BANKNOTE.get(), note.getItem());
        assertEquals(5_000_000, note.get(ModDataComponents.BANKNOTE.get()).valueCents());
    }

    @Test void randomBooksStayInRarityAndObeyNormalAndMasteryRateRules() {
        for (var tier : new CosmicEnchantmentTier[]{CosmicEnchantmentTier.ULTIMATE,
                CosmicEnchantmentTier.LEGENDARY, CosmicEnchantmentTier.MASTERY}) {
            for (int seed = 0; seed < 40; seed++) {
                var stack = service.generate(new RewardDescriptor.CosmicBook(tier),
                        new RewardGenerationContext(RegistryAccess.EMPTY, RandomSource.create(seed), null)).orElseThrow();
                CosmicEnchantmentBookData data = stack.get(ModDataComponents.COSMIC_ENCHANT_BOOK.get());
                var spec = CosmicEnchantmentSpecs.find(data.enchantmentId()).orElseThrow();
                assertEquals(tier, spec.tier());
                assertTrue(data.level() >= 1 && data.level() <= spec.maxLevel());
                assertTrue(tier.allowsRates(data.successRate(), data.destroyRate()));
            }
        }
    }

    @Test void unexaminedBookRewardStoresOnlyRarityAndDefersEveryActualBookRoll() {
        for (var tier : CosmicEnchantmentTier.values()) {
            var stack = generate(new RewardDescriptor.UnexaminedBook(tier));
            assertEquals(ModItems.UNEXAMINED_ENCHANTMENT_BOOK.get(), stack.getItem());
            UnexaminedBookData data = stack.get(ModDataComponents.UNEXAMINED_BOOK.get());
            assertNotNull(data);
            assertEquals(tier, data.tier());
            assertEquals(null, stack.get(ModDataComponents.COSMIC_ENCHANT_BOOK.get()));
        }
    }

    @Test void fixedScrollAndOrbSuccessRatesPreserveIndependentValidDestroyRolls() {
        var black = generate(new RewardDescriptor.BlackScroll(75));
        assertEquals(75, black.get(ModDataComponents.BLACK_SCROLL.get()).returnedSuccessRate());
        for (var descriptor : new RewardDescriptor[]{new RewardDescriptor.ArmorOrb(50),
                new RewardDescriptor.WeaponOrb(80)}) {
            var orb = generate(descriptor);
            EnchantmentOrbData data = orb.get(ModDataComponents.ENCHANTMENT_ORB.get());
            assertTrue(data.successRate() == 50 || data.successRate() == 80);
            assertTrue(data.destroyRate() >= 1 && data.destroyRate() <= 100);
        }
    }

    @Test void unspecifiedOrbsRollIndependentValidSuccessAndDestroyRates() {
        for (var descriptor : new RewardDescriptor[]{new RewardDescriptor.ArmorOrb(0),
                new RewardDescriptor.WeaponOrb(0)}) {
            EnchantmentOrbData data=generate(descriptor).get(ModDataComponents.ENCHANTMENT_ORB.get());
            assertTrue(data.successRate()>=1 && data.successRate()<=100);
            assertTrue(data.destroyRate()>=1 && data.destroyRate()<=100);
        }
    }

    @Test void spawnerRewardPersistsExactEntityIdentity() {
        var stack = generate(new RewardDescriptor.MobSpawner(Identifier.parse("minecraft:blaze")));
        assertEquals(ModItems.MOB_SPAWNER.get(), stack.getItem());
        MobSpawnerData data = stack.get(ModDataComponents.MOB_SPAWNER.get());
        assertNotNull(data);
        assertEquals(Identifier.parse("minecraft:blaze"), data.entityTypeId());
    }

    @Test void step8fFactoriesReuseTypedXpEnchantedScrollAndUniformVKitItems() {
        var xp = generate(new RewardDescriptor.XpBottle(15_000));
        assertEquals(ModItems.SALVAGED_XP_BOTTLE.get(), xp.getItem());
        assertEquals(15_000, xp.get(ModDataComponents.STORED_XP_BOTTLE.get()).storedXp());
        var scroll = generate(new RewardDescriptor.EnchantedBlackScroll(50));
        assertEquals(ModItems.ENCHANTED_BLACK_SCROLL.get(), scroll.getItem());
        EnchantedBlackScrollData data = scroll.get(ModDataComponents.ENCHANTED_BLACK_SCROLL.get());
        assertEquals(50, data.returnedSuccessRate());

        var possible = java.util.Set.of(ModItems.PHOENIX_VKIT_CRYSTAL.get(), ModItems.OGRE_VKIT_CRYSTAL.get(),
                ModItems.JUDGEMENT_VKIT_CRYSTAL.get(), ModItems.SLAYER_VKIT_CRYSTAL.get());
        var seen = new java.util.HashSet<net.minecraft.world.item.Item>();
        var random = RandomSource.create(55);
        for (int roll = 0; roll < 100; roll++) seen.add(service.generate(new RewardDescriptor.RandomVKitCrystal(),
                new RewardGenerationContext(RegistryAccess.EMPTY, random, null)).orElseThrow().getItem());
        assertEquals(possible, seen);
    }

    private net.minecraft.world.item.ItemStack generate(RewardDescriptor descriptor) {
        return service.generate(descriptor,
                new RewardGenerationContext(RegistryAccess.EMPTY, RandomSource.create(55), null)).orElseThrow();
    }
}
