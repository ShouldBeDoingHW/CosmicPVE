package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.reward.lootbox.CosmicEnchantmentTableRewards;
import com.cosmicpve.reward.lootbox.HeroicCosmicEnchantmentTableRewards;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Loaded-registry proof for all five definitions and both expanded Table matrices. */
public final class CleaveCurseGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, CosmicPVE.MOD_ID);
    static { FUNCTIONS.register("cleave_curse_registry_tables", ignored -> CleaveCurseGameTests::verify); }
    private CleaveCurseGameTests() {}

    public static void register(IEventBus bus) {
        FUNCTIONS.register(bus);
        bus.addListener(CleaveCurseGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                CosmicPVE.id("cleave_curse_environment"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("cleave_curse_registry_tables"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("cleave_curse_registry_tables")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
    }

    private static void verify(GameTestHelper helper) {
        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        assertLevel(helper, registry, ModEnchantments.CLEAVE, 8);
        assertLevel(helper, registry, ModEnchantments.SOLITUDE, 3);
        assertLevel(helper, registry, ModEnchantments.CURSE, 5);
        assertLevel(helper, registry, ModEnchantments.MIGHTY_CLEAVE, 8);
        assertLevel(helper, registry, ModEnchantments.FORBIDDEN_CURSE, 5);

        var ordinary = new CosmicEnchantmentTableRewards();
        helper.assertTrue(CosmicEnchantmentTableRewards.POOL.size() == 18,
                "ordinary Table must contain exactly the 18 starred enchantments");
        for (var key : CosmicEnchantmentTableRewards.POOL) {
            var successes = com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs.find(key.identifier())
                    .orElseThrow().tier() == com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier.MASTERY
                    ? CosmicEnchantmentTableRewards.MASTERY_SUCCESS : CosmicEnchantmentTableRewards.ORDINARY_SUCCESS;
            for (int success : successes) {
                CosmicEnchantmentBookData data = ordinary.create(registry, key, success, RandomSource.create(1))
                        .get(ModDataComponents.COSMIC_ENCHANT_BOOK.get());
                helper.assertTrue(data != null && data.level() == registry.getOrThrow(key).value().getMaxLevel()
                        && data.successRate() == success, "ordinary Table output must be max-level with canonical rate");
            }
        }

        var heroic = new HeroicCosmicEnchantmentTableRewards();
        helper.assertTrue(HeroicCosmicEnchantmentTableRewards.POOL.size() == 11
                && HeroicCosmicEnchantmentTableRewards.ENTRY_COUNT == 33, "Heroic Table must contain 11x3 outcomes");
        for (var id : HeroicCosmicEnchantmentTableRewards.POOL) for (int success : HeroicCosmicEnchantmentTableRewards.SUCCESS) {
            CosmicEnchantmentBookData data = heroic.create(registry, id, success, RandomSource.create(2))
                    .get(ModDataComponents.COSMIC_ENCHANT_BOOK.get());
            helper.assertTrue(data != null && data.successRate() == success,
                    "Heroic Table output must contain every canonical success combination");
        }
        helper.succeed();
    }

    private static void assertLevel(GameTestHelper helper, net.minecraft.core.Registry<Enchantment> registry,
            ResourceKey<Enchantment> key, int max) {
        helper.assertTrue(registry.getOrThrow(key).value().getMaxLevel() == max,
                key.identifier() + " must decode at max level " + max);
    }
}
