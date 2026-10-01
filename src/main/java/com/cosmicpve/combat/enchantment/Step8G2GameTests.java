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
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NEW_MASK_COMBAT =
            FUNCTIONS.register("tiki_jester_combat", ignored -> helper -> {
                try { tikiJesterCombat(helper); }
                catch (RuntimeException exception) {
                    throw new IllegalStateException("Tiki/Jester loaded regression: " + exception + " at "
                            + exception.getStackTrace()[1], exception);
                }
            });

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
        event.registerTest(CosmicPVE.id("tiki_jester_combat"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("tiki_jester_combat")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
    }

    private static void tikiJesterCombat(GameTestHelper helper) {
        var level = helper.getLevel();
        var wearer = helper.makeMockPlayer(GameType.SURVIVAL);
        var target = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
        var masks = new com.cosmicpve.equipment.mask.MaskResolver(com.cosmicpve.content.CosmicContent.repository());
        var stacks = new com.cosmicpve.combat.stack.CombatStackService(com.cosmicpve.content.CosmicContent.repository());
        var maskCombat = new com.cosmicpve.equipment.mask.MaskCombatResolver(masks,
                new com.cosmicpve.activity.ActivityContextService(), stacks);
        var tikiHelmet = new ItemStack(Items.IRON_HELMET);
        tikiHelmet.set(com.cosmicpve.registry.ModDataComponents.MASK_LOADOUT.get(),
                com.cosmicpve.equipment.mask.MaskItemFactory.create(CosmicPVE.id("tiki"))
                        .get(com.cosmicpve.registry.ModDataComponents.MASK_ITEM.get()));
        wearer.setItemSlot(EquipmentSlot.HEAD, tikiHelmet);
        var outgoing = hit(wearer, target, 11, com.cosmicpve.combat.api.DamageChannel.ORDINARY, OptionalLong.empty());
        var incoming = hit(target, wearer, 12, com.cosmicpve.combat.api.DamageChannel.ORDINARY, OptionalLong.empty());
        helper.assertTrue(maskCombat.resolve(outgoing).isEmpty() && maskCombat.resolveIncoming(incoming).isEmpty(),
                "Tiki without negative stacks must be inert");
        long tick = level.getServer().getTickCount();
        stacks.addStack(wearer, CosmicPVE.id("development_positive"), 1,
                com.cosmicpve.combat.stack.StackApplication.unattributed(), tick);
        helper.assertTrue(maskCombat.resolve(outgoing).isEmpty() && maskCombat.resolveIncoming(incoming).isEmpty(),
                "positive stacks alone must not activate Tiki");
        stacks.removeAll(wearer, CosmicPVE.id("development_positive"), tick);
        stacks.addStack(wearer, CosmicPVE.id("bleed"), 1,
                com.cosmicpve.combat.stack.StackApplication.unattributed(), tick);
        helper.assertTrue(maskCombat.resolve(outgoing).getFirst().bonus() == .03
                        && maskCombat.resolveIncoming(incoming).getFirst().multiplier() == .97,
                "one negative stack must yield Tiki's exact ordinary outgoing and incoming modifiers");
        stacks.addStack(wearer, CosmicPVE.id("bleed"), 1,
                com.cosmicpve.combat.stack.StackApplication.unattributed(), tick);
        helper.assertTrue(maskCombat.resolve(outgoing).getFirst().bonus() == .03,
                "multiple negative stacks must not scale Tiki");
        helper.assertTrue(maskCombat.resolve(hit(wearer, target, 13,
                com.cosmicpve.combat.api.DamageChannel.TRUE, OptionalLong.empty())).isEmpty(),
                "Tiki must not modify true damage");
        stacks.removeAll(wearer, CosmicPVE.id("bleed"), tick);
        helper.assertTrue(maskCombat.resolve(outgoing).isEmpty(), "cleansing the last negative stack removes Tiki immediately");

        var jesterHelmet = new ItemStack(Items.IRON_HELMET);
        jesterHelmet.set(com.cosmicpve.registry.ModDataComponents.MASK_LOADOUT.get(),
                com.cosmicpve.equipment.mask.MaskItemFactory.create(
                        List.of(CosmicPVE.id("jester"), CosmicPVE.id("turkey")))
                        .get(com.cosmicpve.registry.ModDataComponents.MASK_ITEM.get()));
        wearer.setItemSlot(EquipmentSlot.HEAD, jesterHelmet);
        var nextHits = new com.cosmicpve.combat.empowerment.NextHitEmpowermentService(masks);
        var dodge = new DodgeProcResolver(masks);
        var engine = new ProcEngine(new CooldownService(), new ProcTraceService());
        engine.registerActivationListener(nextHits);
        var failed = targeted(wearer, target, tick, () -> .99);
        helper.assertTrue(engine.evaluate(failed, dodge.resolve(failed)).activationCount() == 0
                        && !nextHits.hasCharge(wearer.getUUID(), nextHits.JESTER),
                "failed canonical Dodge must not arm Jester");
        var succeeded = targeted(wearer, target, tick, () -> 0.0);
        helper.assertTrue(engine.evaluate(succeeded, dodge.resolve(succeeded)).activationCount() == 1
                        && nextHits.hasCharge(wearer.getUUID(), nextHits.JESTER),
                "successful canonical Turkey/Dodge roll must arm one Jester charge");
        helper.assertTrue(engine.evaluate(succeeded, dodge.resolve(succeeded)).activationCount() == 1,
                "a second successful Dodge must refresh Jester rather than queueing charges");
        var empowered = hit(wearer, target, 14, com.cosmicpve.combat.api.DamageChannel.ORDINARY, OptionalLong.empty());
        helper.assertTrue(nextHits.resolve(empowered).getFirst().bonus() == .12,
                "Jester must contribute twelve percent to the ordinary outgoing bucket");
        helper.assertTrue(nextHits.resolve(hit(wearer, target, 15,
                com.cosmicpve.combat.api.DamageChannel.TRUE, OptionalLong.empty())).isEmpty()
                        && nextHits.hasCharge(wearer.getUUID(), nextHits.JESTER),
                "true damage must not consume Jester");
        var committed = new com.cosmicpve.combat.api.CombatResult(empowered,
                new com.cosmicpve.combat.api.CombatBreakdown(10, 0, List.of(), 10, List.of(), 1, 10, 10,
                        List.of(), List.of(), 1, 10, 10), List.of(), 8);
        var committedEvent = new ProcEvent(ProcHook.ON_VALID_HIT, 14, OptionalLong.empty(), RecursionPolicy.NORMAL,
                wearer.getUUID(), Optional.of(wearer.getUUID()), tick, List.of(1.0), List.of(1.0), Set.of(),
                EffectiveEnchantments.EMPTY, Optional.of(committed), wearer, target, () -> .5);
        helper.assertTrue(engine.evaluate(committedEvent, nextHits.resolve(committedEvent)).activationCount() == 1
                        && !nextHits.hasCharge(wearer.getUUID(), nextHits.JESTER),
                "one committed ordinary parent hit must consume Jester exactly once");
        nextHits.armEpidemic(wearer, 7);
        var epidemicHit = hit(wearer, target, 16,
                com.cosmicpve.combat.api.DamageChannel.ORDINARY, OptionalLong.empty());
        helper.assertTrue(nextHits.resolve(epidemicHit).getFirst().bonus() == .07,
                "Epidemic VII must add seven percent ordinary outgoing damage");
        var zeroResult = new com.cosmicpve.combat.api.CombatResult(epidemicHit, committed.breakdown(), List.of(), 0);
        var zeroEvent = new ProcEvent(ProcHook.ON_VALID_HIT, 16, OptionalLong.empty(), RecursionPolicy.NORMAL,
                wearer.getUUID(), Optional.of(wearer.getUUID()), tick, List.of(1.0), List.of(1.0), Set.of(),
                EffectiveEnchantments.EMPTY, Optional.of(zeroResult), wearer, target, () -> .5);
        helper.assertTrue(nextHits.resolve(zeroEvent).isEmpty()
                        && nextHits.hasCharge(wearer.getUUID(), nextHits.EPIDEMIC),
                "zero-damage attacks must not consume the pending Epidemic charge");
        wearer.setHealth(10);
        var committedEpidemic = new com.cosmicpve.combat.api.CombatResult(epidemicHit,
                committed.breakdown(), List.of(), 8);
        var hitEvent = new ProcEvent(ProcHook.ON_VALID_HIT, 16, OptionalLong.empty(), RecursionPolicy.NORMAL,
                wearer.getUUID(), Optional.of(wearer.getUUID()), tick, List.of(1.0), List.of(1.0), Set.of(),
                EffectiveEnchantments.EMPTY, Optional.of(committedEpidemic), wearer, target, () -> .5);
        helper.assertTrue(engine.evaluate(hitEvent, nextHits.resolve(hitEvent)).activationCount() == 1
                        && Math.abs(wearer.getHealth() - 11.68F) < .001F
                        && !nextHits.hasCharge(wearer.getUUID(), nextHits.EPIDEMIC),
                "Epidemic VII must consume once and heal for 21 percent of committed parent health damage");
        engine.evaluate(targeted(wearer, target, tick, () -> 0.0),
                dodge.resolve(targeted(wearer, target, tick, () -> 0.0)));
        helper.runAfterDelay(61, () -> {
            helper.assertTrue(nextHits.resolve(hit(wearer, target, 17,
                    com.cosmicpve.combat.api.DamageChannel.ORDINARY, OptionalLong.empty())).isEmpty(),
                    "Jester charge must expire after 60 server ticks without a qualifying hit");
            helper.succeed();
        });
    }

    private static com.cosmicpve.combat.api.CombatContext hit(net.minecraft.world.entity.LivingEntity attacker,
            net.minecraft.world.entity.LivingEntity target, long sequence,
            com.cosmicpve.combat.api.DamageChannel channel, OptionalLong parent) {
        return new com.cosmicpve.combat.api.CombatContext(attacker, attacker, attacker, target, Optional.empty(),
                attacker.damageSources().mobAttack(attacker), com.cosmicpve.combat.api.AttackCategory.MELEE,
                channel, Set.of(), com.cosmicpve.combat.api.WeaponSnapshot.empty(),
                EffectiveEnchantments.EMPTY, sequence, parent, RecursionPolicy.NORMAL);
    }

    private static ProcEvent targeted(net.minecraft.world.entity.LivingEntity wearer,
            net.minecraft.world.entity.LivingEntity attacker, long tick,
            com.cosmicpve.combat.proc.ProcRandomSource random) {
        return new ProcEvent(ProcHook.ON_TARGETED, 20, OptionalLong.empty(), RecursionPolicy.NORMAL,
                wearer.getUUID(), Optional.of(wearer.getUUID()), tick, List.of(1.0), List.of(1.0), Set.of(),
                EffectiveEnchantments.EMPTY, Optional.empty(), attacker, wearer, random);
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
