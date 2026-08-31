package com.cosmicpve.reward.lootbox;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.registry.ModDataComponents;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Loaded-registry construction coverage for all Step 8E generated rewards. */
public final class Step8EGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, CosmicPVE.MOD_ID);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONSTRUCTION =
            FUNCTIONS.register("step8e_reward_construction", ignored -> Step8EGameTests::construction);
    private Step8EGameTests() {}
    public static void register(IEventBus bus) { FUNCTIONS.register(bus); bus.addListener(Step8EGameTests::registerTests); }
    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                CosmicPVE.id("step8e_environment"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("step8e_reward_construction"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("step8e_reward_construction")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
    }
    private static void construction(GameTestHelper helper) {
        var access = helper.getLevel().registryAccess();
        var signatureFactory = new SignatureWeaponFactory();
        for (var definition : SignatureWeaponDefinition.ALL) {
            var stack = signatureFactory.create(definition, access);
            var identity = stack.get(ModDataComponents.SIGNATURE_WEAPON.get());
            helper.assertTrue(identity != null && identity.signatureId().equals(definition.id())
                    && identity.matchingArmorSetId().equals(definition.matchingSetId()) && identity.kind() == definition.kind(),
                    "Signature weapon identity must match its definition");
            helper.assertTrue(stack.is(definition.baseItem()), "Signature weapon base item must be exact");
            var applied = EnchantmentHelper.getEnchantmentsForCrafting(stack);
            helper.assertTrue(applied.getLevel(access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING)) == 3,
                    "Every signature weapon needs Unbreaking III");
            if (definition.kind() == com.cosmicpve.data.component.SignatureWeaponIdentity.Kind.MELEE)
                helper.assertTrue(applied.getLevel(access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS)) == 5,
                        "Melee signature needs Sharpness V");
            if (definition == SignatureWeaponDefinition.RANGERS_BOW)
                helper.assertTrue(applied.getLevel(access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.POWER)) == 5,
                        "Ranger's Bow needs Power V");
            if (definition == SignatureWeaponDefinition.TRAVELERS_SPACE_BLASTER) {
                helper.assertTrue(applied.getLevel(access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.QUICK_CHARGE)) == 3,
                        "Space Blaster needs Quick Charge III");
                helper.assertTrue(applied.getLevel(access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PIERCING)) == 4,
                        "Space Blaster needs Piercing IV");
            }
        }
        var adminFactory = new AdminAbuseRewardFactory();
        int[] expectedCosmic = {7, 6, 7, 6};
        int index = 0;
        for (var outcome : AdminAbuseRewards.ALL) {
            var stack = adminFactory.create(outcome, access);
            var identity = stack.get(ModDataComponents.ADMIN_ABUSE_REWARD.get());
            var metadata = stack.get(ModDataComponents.CUSTOM_ENCHANT_META.get());
            long cosmic = EnchantmentHelper.getEnchantmentsForCrafting(stack).keySet().stream().filter(holder ->
                    holder.unwrapKey().map(key -> key.identifier().getNamespace().equals(CosmicPVE.MOD_ID)).orElse(false)).count();
            helper.assertTrue(identity != null && identity.rewardId().equals(outcome.id()), "Admin reward identity must be exact");
            helper.assertTrue(metadata != null && metadata.whiteScrollProtected() && metadata.transmogSorted(),
                    "Admin rewards must be White Scrolled and Transmogged");
            helper.assertTrue(cosmic == expectedCosmic[index++], "Admin reward Cosmic enchantment count must be exact");
            helper.assertTrue(new com.cosmicpve.equipment.enchantment.CustomEnchantCapacityService().capacity(stack)
                    == expectedCosmic[index - 1], "Admin reward capacity must match exact enchantment count");
            if (outcome == AdminAbuseRewards.Outcome.GHOSTLY_VEIL || outcome == AdminAbuseRewards.Outcome.COVERT_CLOAK)
                helper.assertTrue(Boolean.TRUE.equals(stack.get(ModDataComponents.OMNI_ARMOR.get()))
                        && stack.has(ModDataComponents.HEROIC.get()), "Admin armor must be typed Omni and Heroic");
            if (outcome == AdminAbuseRewards.Outcome.ASHOKA)
                helper.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(
                        access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(
                                com.cosmicpve.registry.ModEnchantments.INSANITY), stack) == 0,
                        "Ashoka must not contain Insanity");
        }
        var table = new CosmicEnchantmentTableRewards();
        var enchantments = access.lookupOrThrow(Registries.ENCHANTMENT);
        for (int sample = 0; sample < 100; sample++) {
            var book = table.create(enchantments, helper.getLevel().getRandom());
            var data = book.get(ModDataComponents.COSMIC_ENCHANT_BOOK.get());
            helper.assertTrue(data != null && CosmicEnchantmentTableRewards.POOL.stream()
                    .anyMatch(key -> key.identifier().equals(data.enchantmentId())), "Table reward must use exact pool");
            helper.assertTrue(data.level() == enchantments.get(data.enchantmentId()).orElseThrow().value().getMaxLevel(),
                    "Table book must use registered maximum level");
            var spec = com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs.find(data.enchantmentId()).orElseThrow();
            helper.assertTrue(com.cosmicpve.equipment.enchantment.CosmicBookRateRules.allows(book, spec, data),
                    "Every generated Table book must be applicable under its scoped rate policy");
        }
        helper.succeed();
    }
}
