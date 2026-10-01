package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.CombatBreakdown;
import com.cosmicpve.combat.api.CombatResult;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.WeaponSnapshot;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantment;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantments;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import com.cosmicpve.equipment.enchantment.EnchantmentProvenance;
import com.cosmicpve.equipment.enchantment.EnchantmentSourceKind;
import com.cosmicpve.equipment.enchantment.EnchantmentSuppressionService;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.reward.lootbox.CosmicEnchantmentTableRewards;
import com.cosmicpve.reward.lootbox.HeroicCosmicEnchantmentTableRewards;
import java.util.List;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Loaded-registry and live-entity proof for the Overload/Silence/Longbow milestone. */
public final class OverloadSilenceLongbowGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, CosmicPVE.MOD_ID);
    static { FUNCTIONS.register("overload_silence_longbow", ignored -> OverloadSilenceLongbowGameTests::verify); }
    private OverloadSilenceLongbowGameTests() {}

    public static void register(IEventBus bus) {
        FUNCTIONS.register(bus);
        bus.addListener(OverloadSilenceLongbowGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                CosmicPVE.id("overload_silence_longbow_environment"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("overload_silence_longbow"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("overload_silence_longbow")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 120, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
    }

    private static void verify(GameTestHelper helper) {
        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        assertLevel(helper, registry, ModEnchantments.OVERLOAD, 3);
        assertLevel(helper, registry, ModEnchantments.GODLY_OVERLOAD, 3);
        assertLevel(helper, registry, ModEnchantments.SILENCE, 4);
        assertLevel(helper, registry, ModEnchantments.LONGBOW, 5);
        assertLevel(helper, registry, ModEnchantments.BERSERK, 5);
        assertLevel(helper, registry, ModEnchantments.HEALING, 2);
        helper.assertTrue(registry.getOrThrow(ModEnchantments.BERSERK).value().canEnchant(new ItemStack(Items.DIAMOND_AXE))
                && !registry.getOrThrow(ModEnchantments.BERSERK).value().canEnchant(new ItemStack(Items.DIAMOND_SWORD))
                && registry.getOrThrow(ModEnchantments.HEALING).value().canEnchant(new ItemStack(Items.CROSSBOW))
                && !registry.getOrThrow(ModEnchantments.HEALING).value().canEnchant(new ItemStack(Items.BOW)),
                "Berserk and Healing loaded applicability matches Axe and Crossbow only");
        verifyBerserkHealing(helper);
        helper.assertTrue(CosmicEnchantmentTableRewards.POOL.size() == 18
                && !CosmicEnchantmentTableRewards.POOL.contains(ModEnchantments.OVERLOAD)
                && !CosmicEnchantmentTableRewards.POOL.contains(ModEnchantments.SILENCE)
                && !CosmicEnchantmentTableRewards.POOL.contains(ModEnchantments.LONGBOW),
                "ordinary Table must remain the exact starred pool");
        helper.assertTrue(HeroicCosmicEnchantmentTableRewards.POOL.contains(
                ModEnchantments.GODLY_OVERLOAD.identifier())
                && HeroicCosmicEnchantmentTableRewards.ENTRY_COUNT == 42,
                "Godly Overload must enter the generic 14-by-3 Heroic Table");

        var wearer = helper.makeMockPlayer(GameType.SURVIVAL);
        var overload = registry.getOrThrow(ModEnchantments.OVERLOAD);
        var godly = registry.getOrThrow(ModEnchantments.GODLY_OVERLOAD);
        var angelic = registry.getOrThrow(ModEnchantments.ANGELIC);
        var armored = registry.getOrThrow(ModEnchantments.ARMORED);
        wearer.setItemSlot(EquipmentSlot.CHEST, enchanted(Items.DIAMOND_CHESTPLATE, overload, 3));
        var effects = new EquippedPersistentEffectService(new EffectiveEnchantmentsResolver());
        effects.tick(wearer);
        helper.assertTrue(close(wearer.getMaxHealth(), 23) && close(wearer.getHealth(), 20),
                "Overload III must produce 20/23 without healing");
        effects.tick(wearer);
        helper.assertTrue(close(wearer.getMaxHealth(), 23), "reconciliation must not duplicate its modifier");
        wearer.setHealth(23);

        long now = helper.getLevel().getServer().getTickCount();
        Identifier otherSource = CosmicPVE.id("gametest_other_suppression");
        EnchantmentSuppressionService.GLOBAL.suppressEnchantment(wearer, SilenceBehavior.SOURCE_ID,
                ModEnchantments.OVERLOAD.identifier(), 2, now);
        EnchantmentSuppressionService.GLOBAL.suppressEnchantment(wearer, otherSource,
                ModEnchantments.OVERLOAD.identifier(), 4, now);
        effects.tick(wearer);
        helper.assertTrue(close(wearer.getMaxHealth(), 20) && close(wearer.getHealth(), 20),
                "suppression must remove Overload and clamp current health");

        helper.runAfterDelay(2, () -> {
            effects.tick(wearer);
            helper.assertTrue(close(wearer.getMaxHealth(), 20),
                    "Silence expiry must not cancel another suppression source");
        });
        helper.runAfterDelay(4, () -> {
            effects.tick(wearer);
            helper.assertTrue(close(wearer.getMaxHealth(), 23) && close(wearer.getHealth(), 20),
                    "restoring Overload must restore max health without healing");

            wearer.setItemSlot(EquipmentSlot.CHEST, enchanted(Items.DIAMOND_CHESTPLATE, godly, 3));
            effects.tick(wearer);
            helper.assertTrue(close(wearer.getMaxHealth(), 26) && close(wearer.getHealth(), 20),
                    "Godly Overload III must replace ordinary Overload with +6 HP");
            wearer.setHealth(26);
            long godlyStart = helper.getLevel().getServer().getTickCount();
            EnchantmentSuppressionService.GLOBAL.suppressEnchantment(wearer, SilenceBehavior.SOURCE_ID,
                    ModEnchantments.GODLY_OVERLOAD.identifier(), SilenceBehavior.DURATION_TICKS, godlyStart);
            effects.tick(wearer);
            helper.assertTrue(close(wearer.getMaxHealth(), 20) && close(wearer.getHealth(), 20),
                    "Silence must clamp Godly Overload health without mutating the chestplate");

            wearer.setItemSlot(EquipmentSlot.HEAD, enchanted(Items.DIAMOND_HELMET, angelic, 3));
            wearer.setItemSlot(EquipmentSlot.FEET, enchanted(Items.DIAMOND_BOOTS, angelic, 5));
            wearer.setItemSlot(EquipmentSlot.LEGS, enchanted(Items.DIAMOND_LEGGINGS, armored, 4));
            var silence = new SilenceBehavior(EnchantmentSuppressionService.GLOBAL);
            helper.assertTrue(silence.eligibleTypes(wearer, godlyStart).equals(List.of(
                    ModEnchantments.ANGELIC.identifier(), ModEnchantments.ARMORED.identifier(),
                    ModEnchantments.GODLY_OVERLOAD.identifier())),
                    "Silence pool must contain each exact equipped armor type once, including its refreshable lease");

            helper.assertTrue(SilenceBehavior.eligibleWeapon(AttackCategory.MELEE,
                    new ItemStack(Items.DIAMOND_SWORD)) && SilenceBehavior.eligibleWeapon(AttackCategory.MELEE,
                    new ItemStack(Items.DIAMOND_AXE)), "loaded sword and axe tags must qualify for Silence");
            CombatContext silenceShot = contextWithEnchantment(wearer, wearer, new ItemStack(Items.BOW),
                    DamageChannel.ORDINARY, OptionalLong.empty(), RecursionPolicy.NORMAL,
                    ModEnchantments.SILENCE.identifier(), 4);
            var rolls = new ArrayDeque<Double>(List.of(.15, .99));
            var procEvent = procEvent(silenceShot, wearer, wearer, godlyStart, 1.0, rolls::removeFirst);
            var candidates = silence.resolve(procEvent);
            var dispatch = new com.cosmicpve.combat.proc.ProcEngine(
                    new com.cosmicpve.combat.cooldown.CooldownService(),
                    new com.cosmicpve.combat.proc.ProcTraceService()).evaluate(procEvent, candidates);
            helper.assertTrue(candidates.size() == 1 && candidates.getFirst().baseProbability() == .14
                    && candidates.getFirst().childEligibility()
                    == com.cosmicpve.combat.proc.ChildProcEligibility.ROOT_ONLY
                    && dispatch.activationCount() == 1
                    && Math.abs(dispatch.evaluations().getFirst().finalChance() - .168) < 1e-12,
                    "Silence IV must roll fourteen percent once with relative Luck on a committed parent projectile");
            helper.assertTrue(silence.resolve(procEvent(contextWithEnchantment(wearer, wearer,
                    new ItemStack(Items.BOW), DamageChannel.ORDINARY, OptionalLong.of(1),
                    RecursionPolicy.LIMITED_OFFENSIVE_REROLL, ModEnchantments.SILENCE.identifier(), 4),
                    wearer, wearer, godlyStart, 1.0, () -> 0.0)).isEmpty(),
                    "Silence must reject child/proc packets before rolling");
            helper.assertTrue(silence.resolve(procEvent(silenceShot, wearer, wearer, godlyStart, 0.0,
                    () -> 0.0)).isEmpty(), "zero-damage events must not produce a Silence candidate");

            var target = helper.makeMockPlayer(GameType.SURVIVAL);
            target.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            var source = helper.makeMockPlayer(GameType.SURVIVAL);
            source.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE));
            var longbow = new LongbowBehavior();
            CombatContext fired = context(source, target, new ItemStack(Items.BOW), DamageChannel.ORDINARY,
                    OptionalLong.empty(), RecursionPolicy.NORMAL);
            helper.assertTrue(longbow.resolve(fired).getFirst().bonus() == .10,
                    "firing-Bow snapshot plus target Bow at impact must apply Longbow V");
            target.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            helper.assertTrue(longbow.resolve(fired).isEmpty(),
                    "target switching away before impact must remove the live-hand condition");
            target.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.BOW));
            helper.assertTrue(longbow.resolve(fired).size() == 1,
                    "target equipping an offhand Bow before impact must qualify");
            helper.assertTrue(longbow.resolve(context(source, target, new ItemStack(Items.CROSSBOW),
                    DamageChannel.ORDINARY, OptionalLong.empty(), RecursionPolicy.NORMAL)).isEmpty(),
                    "Crossbow source must never qualify for Longbow");
            helper.assertTrue(longbow.resolve(context(source, target, new ItemStack(Items.BOW),
                    DamageChannel.TRUE, OptionalLong.empty(), RecursionPolicy.NORMAL)).isEmpty()
                    && longbow.resolve(context(source, target, new ItemStack(Items.BOW),
                    DamageChannel.ORDINARY, OptionalLong.of(1), RecursionPolicy.NO_PROCS)).isEmpty(),
                    "true and child/proc packets must not receive Longbow");
        });
        helper.runAfterDelay(64, () -> {
            effects.tick(wearer);
            helper.assertTrue(close(wearer.getMaxHealth(), 26) && close(wearer.getHealth(), 20),
                    "exact 60-tick Silence expiry must restore Godly max health without healing");
            helper.succeed();
        });
    }

    private static CombatContext context(net.minecraft.world.entity.LivingEntity source,
            net.minecraft.world.entity.LivingEntity target, ItemStack firingWeapon, DamageChannel channel,
            OptionalLong parent, RecursionPolicy recursion) {
        return contextWithEnchantment(source, target, firingWeapon, channel, parent, recursion,
                ModEnchantments.LONGBOW.identifier(), 5);
    }

    private static void verifyBerserkHealing(GameTestHelper helper) {
        var source = helper.makeMockPlayer(GameType.SURVIVAL);
        var target = helper.makeMockPlayer(GameType.SURVIVAL);
        var resolver = new CosmicEnchantmentBehaviorResolver(null, null, null, null,
                new com.cosmicpve.combat.cooldown.CooldownService());
        var engine = new com.cosmicpve.combat.proc.ProcEngine(
                new com.cosmicpve.combat.cooldown.CooldownService(),
                new com.cosmicpve.combat.proc.ProcTraceService());
        long tick = helper.getLevel().getServer().getTickCount();
        for (int level : List.of(1, 5)) {
            var hit = milestoneEvent(source, target, source, AttackCategory.MELEE, Items.DIAMOND_AXE,
                    ModEnchantments.BERSERK.identifier(), level, com.cosmicpve.combat.proc.ProcHook.ON_VALID_HIT,
                    2.0, tick, () -> 0.0);
            var candidates = resolver.resolve(hit);
            helper.assertTrue(candidates.size() == 1 && candidates.getFirst().baseProbability() == .01 * level,
                    "Berserk level controls the central proc's base chance");
            helper.assertTrue(source.getEffect(net.minecraft.world.effect.MobEffects.STRENGTH) == null,
                    "Triggering hit is committed before Berserk applies Strength");
            var dispatch = engine.evaluate(hit, candidates);
            var strength = source.getEffect(net.minecraft.world.effect.MobEffects.STRENGTH);
            helper.assertTrue(dispatch.activationCount() == 1 && Math.abs(dispatch.evaluations().getFirst().finalChance()
                    - .012 * level) < 1e-12 && strength != null && strength.getAmplifier() == 0
                    && strength.getDuration() == level * 20 && hit.combatResult().orElseThrow().committedHealthDamage() == 2.0,
                    "Berserk uses Luck and refreshes vanilla Strength I without altering committed damage");
            source.removeEffect(net.minecraft.world.effect.MobEffects.STRENGTH);
        }
        var rejected = milestoneEvent(source, target, source, AttackCategory.MELEE, Items.DIAMOND_AXE,
                ModEnchantments.BERSERK.identifier(), 5, com.cosmicpve.combat.proc.ProcHook.ON_VALID_HIT,
                0.0, tick, () -> 0.0);
        helper.assertTrue(engine.evaluate(rejected, resolver.resolve(rejected)).activationCount() == 0,
                "Rejected zero-damage Axe hit cannot grant Berserk");

        var arrow = net.minecraft.world.entity.EntityType.ARROW.create(helper.getLevel(),
                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        helper.assertTrue(arrow != null, "Arrow projectile fixture must construct");
        for (int level : List.of(1, 2)) {
            var shot = milestoneEvent(source, target, arrow, AttackCategory.PROJECTILE, Items.CROSSBOW,
                    ModEnchantments.HEALING.identifier(), level,
                    com.cosmicpve.combat.proc.ProcHook.ON_PROJECTILE_HIT, 2.0, tick, () -> 0.0);
            var candidates = resolver.resolve(shot);
            helper.assertTrue(candidates.size() == 1 && candidates.getFirst().baseProbability() == .10,
                    "Healing base chance stays ten percent at both levels");
            var dispatch = engine.evaluate(shot, candidates);
            var absorption = source.getEffect(net.minecraft.world.effect.MobEffects.ABSORPTION);
            helper.assertTrue(dispatch.activationCount() == 1 && Math.abs(dispatch.evaluations().getFirst().finalChance() - .12) < 1e-12
                    && absorption != null && absorption.getAmplifier() == level - 1
                    && absorption.getDuration() == 80 && source.getAbsorptionAmount() == 4 * level,
                    "Healing uses Luck and refreshes actual Absorption I/II for four seconds");
        }
        for (var invalid : List.of(
                milestoneEvent(source, target, arrow, AttackCategory.PROJECTILE, Items.BOW,
                        ModEnchantments.HEALING.identifier(), 2, com.cosmicpve.combat.proc.ProcHook.ON_PROJECTILE_HIT,
                        2.0, tick, () -> 0.0),
                milestoneEvent(source, target, source, AttackCategory.MELEE, Items.CROSSBOW,
                        ModEnchantments.HEALING.identifier(), 2, com.cosmicpve.combat.proc.ProcHook.ON_PROJECTILE_HIT,
                        2.0, tick, () -> 0.0),
                milestoneEvent(source, target, arrow, AttackCategory.PROJECTILE, Items.CROSSBOW,
                        ModEnchantments.HEALING.identifier(), 2, com.cosmicpve.combat.proc.ProcHook.ON_PROJECTILE_HIT,
                        0.0, tick, () -> 0.0)))
            helper.assertTrue(engine.evaluate(invalid, resolver.resolve(invalid)).activationCount() == 0,
                    "Bow, Crossbow melee, and uncommitted shots cannot trigger Healing");
        source.discard(); target.discard(); arrow.discard();
    }

    private static com.cosmicpve.combat.proc.ProcEvent milestoneEvent(
            net.minecraft.world.entity.LivingEntity source, net.minecraft.world.entity.LivingEntity target,
            net.minecraft.world.entity.Entity direct, AttackCategory category, Item item, Identifier id, int level,
            com.cosmicpve.combat.proc.ProcHook hook, double damage, long tick,
            com.cosmicpve.combat.proc.ProcRandomSource random) {
        var effective = new EffectiveEnchantments(Map.of(id, new EffectiveEnchantment(id, level,
                List.of(new EnchantmentProvenance(EnchantmentSourceKind.ACTUAL, CosmicPVE.id("milestone_weapon"), level)))));
        var context = new CombatContext(direct, source, source, target, Optional.empty(), null,
                category, DamageChannel.ORDINARY, Set.of(), new WeaponSnapshot(new ItemStack(item)), effective,
                2, OptionalLong.empty(), RecursionPolicy.NORMAL);
        var breakdown = new CombatBreakdown(2, 0, List.of(), 2, List.of(), 1, 2, 2,
                List.of(), List.of(), 1, 2, 2);
        var result = new CombatResult(context, breakdown, List.of(), damage);
        return new com.cosmicpve.combat.proc.ProcEvent(hook, 2, OptionalLong.empty(), RecursionPolicy.NORMAL,
                source.getUUID(), Optional.of(source.getUUID()), tick, List.of(1.2), List.of(1.0), Set.of(),
                effective, Optional.of(result), source, target, random);
    }

    private static CombatContext contextWithEnchantment(net.minecraft.world.entity.LivingEntity source,
            net.minecraft.world.entity.LivingEntity target, ItemStack firingWeapon, DamageChannel channel,
            OptionalLong parent, RecursionPolicy recursion, Identifier id, int level) {
        var effective = new EffectiveEnchantments(Map.of(id, new EffectiveEnchantment(id, level,
                List.of(new EnchantmentProvenance(EnchantmentSourceKind.ACTUAL,
                        CosmicPVE.id("firing_weapon"), level)))));
        return new CombatContext(source, source, source, target, Optional.empty(), null,
                AttackCategory.PROJECTILE, channel, Set.of(), new WeaponSnapshot(firingWeapon), effective,
                2, parent, recursion);
    }

    private static com.cosmicpve.combat.proc.ProcEvent procEvent(CombatContext context,
            net.minecraft.world.entity.LivingEntity source, net.minecraft.world.entity.LivingEntity target,
            long tick, double committedDamage, com.cosmicpve.combat.proc.ProcRandomSource random) {
        var breakdown = new CombatBreakdown(1, 0, List.of(), 1, List.of(), 1, 1, 1,
                List.of(), List.of(), 1, 1, 1);
        var result = new CombatResult(context, breakdown, List.of(), committedDamage);
        return new com.cosmicpve.combat.proc.ProcEvent(com.cosmicpve.combat.proc.ProcHook.ON_VALID_HIT,
                2, context.parentSequenceId(), context.recursionPolicy(), source.getUUID(),
                Optional.of(source.getUUID()), tick, List.of(1.2), List.of(1.0), Set.of(),
                context.effectiveEnchantments(), Optional.of(result), source, target, random);
    }

    private static ItemStack enchanted(Item item, Holder.Reference<Enchantment> enchantment, int level) {
        ItemStack stack = new ItemStack(item);
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(enchantment, level));
        return stack;
    }

    private static void assertLevel(GameTestHelper helper, net.minecraft.core.Registry<Enchantment> registry,
            ResourceKey<Enchantment> key, int max) {
        helper.assertTrue(registry.getOrThrow(key).value().getMaxLevel() == max,
                key.identifier() + " must decode at max level " + max);
    }

    private static boolean close(float actual, double expected) { return Math.abs(actual - expected) < 1e-5; }
}
