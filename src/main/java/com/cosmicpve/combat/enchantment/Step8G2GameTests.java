package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.cooldown.CooldownService;
import com.cosmicpve.combat.proc.ProcEngine;
import com.cosmicpve.combat.proc.ProcEvent;
import com.cosmicpve.combat.proc.ProcHook;
import com.cosmicpve.combat.proc.ProcTraceService;
import com.cosmicpve.combat.ownership.OwnedAllyResolver;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantments;
import com.cosmicpve.entity.undeadcorpse.UndeadCorpseEntity;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.registry.ModEntities;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Loaded-registry proof for Spirit Link ownership/proc behavior and the canonical corpse attribute. */
public final class Step8G2GameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, CosmicPVE.MOD_ID);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPIRIT_LINK =
            FUNCTIONS.register("spirit_link_owned_ally", ignored -> Step8G2GameTests::spiritLinkOwnedAlly);

    private Step8G2GameTests() {}

    public static void register(IEventBus modBus) {
        FUNCTIONS.register(modBus);
        modBus.addListener(Step8G2GameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                CosmicPVE.id("step_8g2_environment"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("spirit_link_owned_ally"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("spirit_link_owned_ally")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
    }

    private static void spiritLinkOwnedAlly(GameTestHelper helper) {
        var level = helper.getLevel();
        var wearer = helper.makeMockPlayer(GameType.SURVIVAL);
        var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var spiritLink = enchantments.getOrThrow(ModEnchantments.SPIRIT_LINK);
        wearer.setItemSlot(EquipmentSlot.HEAD, enchanted(Items.IRON_HELMET, spiritLink, 7));
        wearer.setItemSlot(EquipmentSlot.CHEST, enchanted(Items.IRON_CHESTPLATE, spiritLink, 7));
        var attacker = EntityType.ZOMBIE.create(level, EntitySpawnReason.TRIGGERED);
        helper.assertTrue(attacker != null, "test attacker must be constructible");

        var behavior = new SpiritLinkBehavior();
        var noAlly = event(wearer, attacker, List.of(1.2), () -> {
            throw new AssertionError("Spirit Link must not consume RNG without an eligible owned ally");
        });
        helper.assertTrue(behavior.resolve(noAlly).isEmpty() && !behavior.hasCharge(wearer.getUUID()),
                "Spirit Link must not produce a candidate or charge without an owned ally");

        var wolf = helper.spawn(EntityType.WOLF, new BlockPos(1, 2, 1));
        wolf.tame(wearer);
        wolf.setHealth(1.0F);
        // The helper places the ally inside the loaded test structure; wait one tick for the loaded-entity iterable.
        helper.runAfterDelay(1, () -> {
            int aggregate = SpiritLinkBehavior.equippedLevel(wearer);
            var allies = OwnedAllyResolver.livingAllies(wearer);
            var rolls = new ArrayDeque<Double>(List.of(.055, .0));
            var procEvent = event(wearer, attacker, List.of(1.2), rolls::removeFirst);
            var candidates = behavior.resolve(procEvent);
            helper.assertTrue(candidates.size() == 1 && candidates.getFirst().baseProbability() == .05,
                    "two Spirit Link pieces must produce one five-percent candidate (aggregate=" + aggregate
                            + ", loaded allies=" + allies.size() + ", owner="
                            + OwnedAllyResolver.ownerId(wolf).orElse(null) + ")");
            var result = new ProcEngine(new CooldownService(), new ProcTraceService()).evaluate(procEvent, candidates);
            helper.assertTrue(result.activationCount() == 1
                            && Math.abs(result.evaluations().getFirst().finalChance() - .06) < 1e-12,
                    "Luck must modify Spirit Link relatively from five to six percent");
            helper.assertTrue(Math.abs(wolf.getHealth() - 8.0F) < 1e-6
                            && Math.abs(behavior.storedBonus(wearer.getUUID()) - .14) < 1e-12,
                    "aggregate XIV must heal seven HP and store one fourteen-percent charge");

            UndeadCorpseEntity corpse = ModEntities.UNDEAD_CORPSE.get().create(level, EntitySpawnReason.TRIGGERED);
            helper.assertTrue(corpse != null
                            && corpse.getAttributeBaseValue(Attributes.ARMOR) == 1.0
                            && corpse.getAttributeBaseValue(Attributes.ARMOR_TOUGHNESS) == 0.0,
                    "the real Undead Corpse must have one armor and zero toughness");
            helper.succeed();
        });
    }

    private static ItemStack enchanted(net.minecraft.world.item.Item item,
            Holder.Reference<net.minecraft.world.item.enchantment.Enchantment> enchantment, int level) {
        ItemStack stack = new ItemStack(item);
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(enchantment, level));
        return stack;
    }

    private static ProcEvent event(net.minecraft.world.entity.LivingEntity wearer,
            net.minecraft.world.entity.LivingEntity attacker, List<Double> multipliers,
            com.cosmicpve.combat.proc.ProcRandomSource random) {
        return new ProcEvent(ProcHook.ON_DAMAGE_TAKEN, 1L, OptionalLong.empty(), RecursionPolicy.NORMAL,
                wearer.getUUID(), Optional.of(wearer.getUUID()), 1L, multipliers, List.of(1.0), Set.of(),
                EffectiveEnchantments.EMPTY, Optional.empty(), attacker, wearer, random);
    }
}
