package com.cosmicpve.equipment.accessory;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.CosmicCombat;
import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatBreakdown;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.CombatResult;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.WeaponSnapshot;
import com.cosmicpve.combat.cooldown.CooldownScope;
import com.cosmicpve.combat.enchantment.BleedBehavior;
import com.cosmicpve.combat.proc.ChildProcEligibility;
import com.cosmicpve.combat.proc.ProcCandidate;
import com.cosmicpve.combat.proc.ProcEngine;
import com.cosmicpve.combat.proc.ProcEvent;
import com.cosmicpve.combat.proc.ProcHook;
import com.cosmicpve.combat.proc.ProcProvenance;
import com.cosmicpve.combat.proc.ProcRandomSource;
import com.cosmicpve.combat.proc.ProcSourceKind;
import com.cosmicpve.combat.proc.ProcTraceService;
import com.cosmicpve.combat.stack.BleedRuntimeService;
import com.cosmicpve.combat.stack.StackApplication;
import com.cosmicpve.data.component.AccessoryLoadout;
import com.cosmicpve.data.component.AccessorySlot;
import com.cosmicpve.data.component.AccessorySocketData;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantments;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.registry.ModItems;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Loaded tags/components plus live equipped-entity coverage for the Amulet milestone. */
public final class AmuletGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, CosmicPVE.MOD_ID);
    static { FUNCTIONS.register("amulet_milestone", ignored -> AmuletGameTests::verify); }
    private AmuletGameTests() {}
    public static void register(IEventBus bus) { FUNCTIONS.register(bus); bus.addListener(AmuletGameTests::registerTests); }
    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                CosmicPVE.id("amulet_environment"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("amulet_milestone"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("amulet_milestone")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 120, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
    }
    private static void verify(GameTestHelper helper) {
        var wearer = helper.makeMockPlayer(GameType.SURVIVAL);
        var target = helper.makeMockPlayer(GameType.SURVIVAL);
        var application = new AccessoryApplicationService();
        var chest = new ItemStack(Items.DIAMOND_CHESTPLATE);
        var socket = new ItemStack(ModItems.AMULET_SOCKET.get());
        socket.set(ModDataComponents.ACCESSORY_SOCKET.get(), new AccessorySocketData(1, AccessorySlot.AMULET, 100));
        helper.assertTrue(application.socket(socket, chest, socket, chest, () -> 100)
                == AccessoryApplicationService.SocketOutcome.SUCCESS && socket.isEmpty(),
                "loaded chest tag must accept one non-destructive Amulet Socket");
        var blood = AmuletItemFactory.create(AmuletDefinition.BLOOD_DIAMOND);
        helper.assertTrue(application.attach(blood, chest, blood, chest)
                == AccessoryApplicationService.AttachOutcome.SUCCESS && blood.isEmpty(), "Blood Diamond must attach once");
        wearer.setItemSlot(EquipmentSlot.CHEST, chest);
        var resolver = new AccessoryResolver();
        helper.assertTrue(resolver.hasAmulet(wearer, AmuletDefinition.BLOOD_DIAMOND),
                "only the equipped chestplate must resolve its Amulet");

        long tick = helper.getLevel().getServer().getTickCount();
        var stacks = CosmicCombat.stacks();
        stacks.addStack(wearer, BleedBehavior.STACK_ID, 3, StackApplication.unattributed(), tick);
        stacks.addStack(target, BleedBehavior.STACK_ID, 5, StackApplication.unattributed(), tick);
        var blackState = new BlackHeartStateService();
        var combat = new AmuletCombatService(resolver, stacks,
                new BleedRuntimeService(CosmicCombat.childActions()), blackState);
        helper.assertTrue(close(combat.resolve(context(wearer, target, DamageChannel.ORDINARY,
                OptionalLong.empty())).getFirst().bonus(), .08), "3 + 5 active Bleed must add exactly eight percent");
        wearer.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        helper.assertTrue(combat.resolve(context(wearer, target, DamageChannel.ORDINARY,
                OptionalLong.empty())).isEmpty(), "inventory/unequipped chestplates must provide no effect");

        chest.set(ModDataComponents.ACCESSORY_LOADOUT.get(), chest.get(ModDataComponents.ACCESSORY_LOADOUT.get())
                .withAttachment(AccessorySlot.AMULET, AmuletDefinition.ICICLE.id()));
        wearer.setItemSlot(EquipmentSlot.CHEST, chest);
        stacks.removeAll(target, BleedBehavior.STACK_ID, tick);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100));
        var ordinary = context(wearer, target, DamageChannel.ORDINARY, OptionalLong.empty());
        var event = event(ordinary, wearer, target, tick, () -> .099);
        var candidates = combat.resolve(event);
        helper.assertTrue(candidates.size() == 1 && close(candidates.getFirst().baseProbability(), .10)
                && candidates.getFirst().classifications().contains(ProcEngine.UNMODIFIED_CHANCE_CLASSIFICATION),
                "Icicle must expose one fixed ten-percent parent-hit roll");
        new ProcEngine(new com.cosmicpve.combat.cooldown.CooldownService(), new ProcTraceService())
                .evaluate(event, candidates);
        helper.assertTrue(stacks.count(target, BleedBehavior.STACK_ID, tick) == 1,
                "successful Icicle must add exactly one canonical Bleed stack");
        helper.assertTrue(combat.resolve(event(context(wearer, target, DamageChannel.ORDINARY,
                OptionalLong.of(1)), wearer, target, tick, () -> 0)).isEmpty(),
                "child packets must not produce an Icicle candidate");

        chest.set(ModDataComponents.ACCESSORY_LOADOUT.get(), chest.get(ModDataComponents.ACCESSORY_LOADOUT.get())
                .withAttachment(AccessorySlot.AMULET, AmuletDefinition.BLACK_HEART.id()));
        var engine = new ProcEngine(new com.cosmicpve.combat.cooldown.CooldownService(), new ProcTraceService());
        engine.registerActivationListener(combat);
        engine.evaluate(activationEvent(target, wearer, tick, () -> .99), List.of(candidate(
                ModEnchantments.PERMAFROST.identifier(), 1.0, activation -> activation.markAffected(wearer), true)));
        helper.assertTrue(blackState.active(wearer, tick) && blackState.expiryTick(wearer) == tick + 80,
                "hostile successful Mastery action affecting wearer must activate Black Heart for exactly 80 ticks");
        long originalExpiry = blackState.expiryTick(wearer);
        engine.evaluate(activationEvent(target, wearer, tick + 1, () -> .50), List.of(candidate(
                ModEnchantments.MIGHTY_CACTUS.identifier(), .10,
                activation -> activation.markAffected(wearer), false)));
        engine.evaluate(activationEvent(target, wearer, tick + 2, () -> 0), List.of(candidate(
                ModEnchantments.CACTUS.identifier(), 1.0,
                activation -> activation.markAffected(wearer), true)));
        engine.evaluate(activationEvent(target, wearer, tick + 3, () -> 0), List.of(candidate(
                ModEnchantments.PERMAFROST.identifier(), 1.0,
                activation -> activation.markAffected(target), true)));
        engine.evaluate(activationEvent(wearer, target, tick + 4, () -> 0), List.of(candidate(
                ModEnchantments.PERMAFROST.identifier(), 1.0,
                activation -> activation.markAffected(wearer), true)));
        helper.assertTrue(blackState.expiryTick(wearer) == originalExpiry,
                "failed Heroic, lower rarity, enemy self-only, and wearer's own procs must not refresh Black Heart");
        engine.evaluate(activationEvent(target, wearer, tick + 5, () -> .99), List.of(candidate(
                ModEnchantments.MIGHTY_CACTUS.identifier(), 1.0,
                activation -> activation.markAffected(wearer), true)));
        helper.assertTrue(blackState.expiryTick(wearer) == tick + 85,
                "hostile successful Heroic action affecting wearer must refresh Black Heart to a full 80 ticks");
        helper.assertTrue(close(combat.resolve(context(wearer, target, DamageChannel.ORDINARY,
                OptionalLong.empty())).getFirst().bonus(), .05)
                && combat.resolve(context(wearer, target, DamageChannel.TRUE, OptionalLong.empty())).isEmpty(),
                "Black Heart must add five percent only to ordinary root damage");
        blackState.activate(wearer, tick + 20);
        helper.assertTrue(blackState.expiryTick(wearer) == tick + 100 && !blackState.active(wearer, tick + 100),
                "retrigger must refresh, not stack, and expire on the exact boundary");
        wearer.discard(); target.discard(); helper.succeed();
    }
    private static CombatContext context(net.minecraft.world.entity.LivingEntity attacker,
            net.minecraft.world.entity.LivingEntity target, DamageChannel channel, OptionalLong parent) {
        return new CombatContext(attacker, attacker, attacker, target, Optional.of(attacker.getUUID()), null,
                AttackCategory.MELEE, channel, Set.of(), new WeaponSnapshot(new ItemStack(Items.DIAMOND_SWORD)),
                EffectiveEnchantments.EMPTY, 2, parent, RecursionPolicy.NORMAL);
    }
    private static ProcEvent event(CombatContext context, net.minecraft.world.entity.LivingEntity attacker,
            net.minecraft.world.entity.LivingEntity target, long tick, ProcRandomSource random) {
        var breakdown = new CombatBreakdown(1, 0, List.of(), 1, List.of(), 1, 1, 1, List.of(), List.of(), 1, 1, 1);
        var result = new CombatResult(context, breakdown, List.of(), 1);
        return new ProcEvent(ProcHook.ON_VALID_HIT, 2, context.parentSequenceId(), context.recursionPolicy(),
                attacker.getUUID(), Optional.of(attacker.getUUID()), tick, List.of(99.0), List.of(1.0), Set.of(),
                EffectiveEnchantments.EMPTY, Optional.of(result), attacker, target, random);
    }
    private static ProcEvent activationEvent(net.minecraft.world.entity.LivingEntity owner,
            net.minecraft.world.entity.LivingEntity affectedTarget, long tick, ProcRandomSource random) {
        return new ProcEvent(ProcHook.ON_DAMAGE_TAKEN, 3, OptionalLong.empty(), RecursionPolicy.NORMAL,
                owner.getUUID(), Optional.empty(), tick, List.of(), List.of(), Set.of(), EffectiveEnchantments.EMPTY,
                Optional.empty(), owner, affectedTarget, random);
    }
    private static ProcCandidate candidate(net.minecraft.resources.Identifier id, double chance,
            com.cosmicpve.combat.proc.ProcAction action, boolean deterministic) {
        return new ProcCandidate(id, ProcHook.ON_DAMAGE_TAKEN, chance, Optional.empty(), 0L,
                CooldownScope.EPHEMERAL_COMBAT, Optional.empty(), List.of(), List.of(), Optional.empty(),
                ChildProcEligibility.ROOT_ONLY,
                deterministic ? Set.of(ProcEngine.DETERMINISTIC_CLASSIFICATION) : Set.of(), id, action,
                new ProcProvenance(ProcSourceKind.ACTUAL_ENCHANTMENT, id));
    }
    private static boolean close(double actual, double expected) { return Math.abs(actual - expected) < 1e-12; }
}
