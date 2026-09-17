package com.cosmicpve.equipment.accessory;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatBreakdown;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.CombatResult;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.WeaponSnapshot;
import com.cosmicpve.data.component.AccessoryLoadout;
import com.cosmicpve.data.component.AccessorySlot;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantments;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BeltGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, CosmicPVE.MOD_ID);
    static { FUNCTIONS.register("belt_milestone", ignored -> BeltGameTests::verify); }
    private BeltGameTests() {}
    public static void register(IEventBus bus) { FUNCTIONS.register(bus); bus.addListener(BeltGameTests::registerTests); }
    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                CosmicPVE.id("belt_environment"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("belt_milestone"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("belt_milestone")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 120, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
    }
    private static void verify(GameTestHelper helper) {
        var wearer = helper.makeMockPlayer(GameType.SURVIVAL); var target = helper.makeMockPlayer(GameType.SURVIVAL);
        var secondTarget = helper.makeMockPlayer(GameType.SURVIVAL);
        var resolver = new AccessoryResolver(); var state = new BandolierStateService();
        var combat = new BeltCombatService(resolver, state);
        equip(wearer, BeltDefinition.BANDOLIER);
        var context = context(wearer, target, DamageChannel.ORDINARY, new ItemStack(Items.DIAMOND_SWORD));
        helper.assertTrue(resolver.hasBelt(wearer, BeltDefinition.BANDOLIER), "equipped leggings must resolve Bandolier");
        helper.assertTrue(BeltCombatService.qualifyingSword(context), "loaded Diamond Sword must qualify");
        helper.assertTrue(combat.resolve(context).isEmpty(), "Bandolier hits one through three must be ordinary");
        combat.onCommitted(result(context)); combat.onCommitted(result(context)); combat.onCommitted(result(context));
        helper.assertTrue(state.count(wearer) == 3, "three committed Sword hits must charge Bandolier; got " + state.count(wearer));
        helper.assertTrue(combat.resolve(context).size() == 1
                && close(combat.resolve(context).getFirst().bonus(), .12), "fourth Sword hit must receive +12%");
        var axe = context(wearer, target, DamageChannel.ORDINARY, new ItemStack(Items.DIAMOND_AXE));
        combat.onCommitted(result(axe));
        helper.assertTrue(combat.resolve(context).size() == 1, "non-Sword attacks must not consume charge");
        combat.onCommitted(result(context));
        helper.assertTrue(combat.resolve(context).isEmpty(), "committed fourth Sword hit must reset charge");
        wearer.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY); combat.reconcile(wearer);
        helper.assertTrue(state.count(wearer) == 0, "unequipping Bandolier must clear transient charge");

        equip(wearer, BeltDefinition.JELLY_ROLL);
        helper.assertTrue(combat.resolveIncoming(context(target, wearer, DamageChannel.ORDINARY,
                new ItemStack(Items.DIAMOND_SWORD))).getFirst().multiplier() == .98,
                "Jelly Roll must use the ordinary incoming contribution bucket");
        helper.assertTrue(combat.resolveIncoming(context(target, wearer, DamageChannel.TRUE,
                new ItemStack(Items.DIAMOND_SWORD))).isEmpty(), "Jelly Roll must not reduce true damage");
        wearer.getFoodData().setFoodLevel(20); wearer.getFoodData().setSaturation(5); combat.afterDevour(wearer);
        helper.assertTrue(close(wearer.getFoodData().getSaturationLevel(), 6), "Jelly Roll must multiply saturation by 1.20");
        wearer.getFoodData().setSaturation(0); combat.afterDevour(wearer);
        helper.assertTrue(wearer.getFoodData().getSaturationLevel() == 0, "zero saturation must remain zero");

        equip(wearer, BeltDefinition.SHOCK_THERAPY); wearer.setHealth(10); target.setHealth(20); secondTarget.setHealth(20);
        var lightningParent = context(wearer, target, DamageChannel.ORDINARY, new ItemStack(Items.BOW));
        var activation = activation(wearer, target, result(lightningParent));
        var lightning = new CosmicLightningService(resolver);
        helper.assertTrue(lightning.deliver(activation, wearer, target, lightningParent,
                        com.cosmicpve.registry.ModEnchantments.LIGHTNING.identifier(), 2.0,
                        com.cosmicpve.combat.CosmicCombat.childActions()),
                "owned Cosmic lightning must resolve through the shared service");
        helper.assertTrue(close(target.getHealth(), 17) && close(wearer.getHealth(), 10.25),
                "Shock Therapy must add exactly one true damage and heal exactly 0.25 HP");
        lightning.deliver(activation, wearer, secondTarget, lightningParent,
                com.cosmicpve.registry.ModEnchantments.LIGHTNING.identifier(), 2.0,
                com.cosmicpve.combat.CosmicCombat.childActions());
        helper.assertTrue(close(secondTarget.getHealth(), 17) && close(wearer.getHealth(), 10.25),
                "one activation may extend each target but heal its owner only once");

        target.setHealth(20); wearer.setHealth(10);
        com.cosmicpve.combat.enchantment.LightningBehavior.activate(
                activation(wearer, target, result(lightningParent)),
                com.cosmicpve.combat.CosmicCombat.childActions());
        helper.assertTrue(close(target.getHealth(), 17) && close(wearer.getHealth(), 10.25),
                "Lightning must route its base two plus Shock one through the shared service");

        target.setHealth(20); wearer.setHealth(10);
        var stormcallerParent = context(target, wearer, DamageChannel.ORDINARY,
                new ItemStack(Items.DIAMOND_SWORD));
        com.cosmicpve.combat.enchantment.StormcallerBehavior.activate(
                activation(target, wearer, result(stormcallerParent)), 10,
                com.cosmicpve.combat.CosmicCombat.childActions());
        helper.assertTrue(close(target.getHealth(), 16) && close(wearer.getHealth(), 10.25),
                "Stormcaller must route its base three plus Shock one for the defending owner");

        target.setHealth(20); wearer.setHealth(10);
        var stormAxe = new ItemStack(Items.DIAMOND_AXE);
        var stormSkin = com.cosmicpve.equipment.skin.WeaponSkinItemFactory.create(
                com.cosmicpve.equipment.skin.WeaponSkinDefinitions.STORMBRINGER);
        var skinApplication = new com.cosmicpve.equipment.skin.WeaponSkinApplicationService();
        helper.assertTrue(skinApplication.apply(stormSkin, stormAxe, stormSkin, stormAxe)
                        == com.cosmicpve.equipment.skin.WeaponSkinApplicationService.ApplyOutcome.SUCCESS,
                "Stormbringer test axe must accept its real skin identity");
        var stormParent = context(wearer, target, DamageChannel.ORDINARY, stormAxe);
        var stormEvent = procEvent(wearer, target, result(stormParent));
        var skinCombat = new com.cosmicpve.equipment.skin.WeaponSkinCombatResolver(
                new com.cosmicpve.equipment.skin.WeaponSkinResolver(),
                com.cosmicpve.combat.CosmicCombat.stacks(), com.cosmicpve.combat.CosmicCombat.childActions());
        var stormCandidate = skinCombat.resolve(stormEvent).stream()
                .filter(value -> value.effectId().equals(
                        com.cosmicpve.equipment.skin.WeaponSkinDefinitions.STORMBRINGER))
                .findFirst().orElseThrow();
        stormCandidate.action().execute(new com.cosmicpve.combat.proc.ProcActivation(
                stormEvent, stormCandidate, 1.0, 0.0));
        helper.assertTrue(close(target.getHealth(), 17) && close(wearer.getHealth(), 10.25),
                "Stormbringer must route its base two plus Shock one through the shared service");
        wearer.discard(); target.discard(); secondTarget.discard(); helper.succeed();
    }
    private static void equip(net.minecraft.world.entity.LivingEntity entity, BeltDefinition belt) {
        var legs = new ItemStack(Items.DIAMOND_LEGGINGS);
        legs.set(com.cosmicpve.registry.ModDataComponents.ACCESSORY_LOADOUT.get(), AccessoryLoadout.empty()
                .withSocket(AccessorySlot.BELT).withAttachment(AccessorySlot.BELT, belt.id()));
        entity.setItemSlot(EquipmentSlot.LEGS, legs);
    }
    private static CombatContext context(net.minecraft.world.entity.LivingEntity attacker,
            net.minecraft.world.entity.LivingEntity target, DamageChannel channel, ItemStack weapon) {
        return new CombatContext(attacker, attacker, attacker, target, Optional.of(attacker.getUUID()), null,
                AttackCategory.MELEE, channel, Set.of(), new WeaponSnapshot(weapon), EffectiveEnchantments.EMPTY,
                2, OptionalLong.empty(), RecursionPolicy.NORMAL);
    }
    private static CombatResult result(CombatContext context) {
        return new CombatResult(context, new CombatBreakdown(10, 0, List.of(), 10, List.of(), 1, 10, 10,
                List.of(), List.of(), 1, 10, 10), List.of(), 10);
    }
    private static com.cosmicpve.combat.proc.ProcActivation activation(
            net.minecraft.world.entity.LivingEntity owner, net.minecraft.world.entity.LivingEntity target,
            CombatResult result) {
        var event = procEvent(owner, target, result);
        var id = com.cosmicpve.registry.ModEnchantments.LIGHTNING.identifier();
        var candidate = new com.cosmicpve.combat.proc.ProcCandidate(id,
                com.cosmicpve.combat.proc.ProcHook.ON_VALID_HIT, 1.0, Optional.empty(), 0L,
                com.cosmicpve.combat.cooldown.CooldownScope.EPHEMERAL_COMBAT, Optional.empty(), List.of(), List.of(),
                Optional.empty(), com.cosmicpve.combat.proc.ChildProcEligibility.ROOT_ONLY, Set.of(), id, ignored -> {},
                new com.cosmicpve.combat.proc.ProcProvenance(
                        com.cosmicpve.combat.proc.ProcSourceKind.ACTUAL_ENCHANTMENT, id));
        return new com.cosmicpve.combat.proc.ProcActivation(event, candidate, 1.0, 0.0);
    }
    private static com.cosmicpve.combat.proc.ProcEvent procEvent(
            net.minecraft.world.entity.LivingEntity owner, net.minecraft.world.entity.LivingEntity target,
            CombatResult result) {
        return new com.cosmicpve.combat.proc.ProcEvent(com.cosmicpve.combat.proc.ProcHook.ON_VALID_HIT,
                result.context().attackSequenceId(), OptionalLong.empty(), RecursionPolicy.NORMAL, owner.getUUID(),
                Optional.of(owner.getUUID()), 0, List.of(), List.of(), Set.of(), EffectiveEnchantments.EMPTY,
                Optional.of(result), owner, target, () -> 0.0);
    }
    private static boolean close(double actual, double expected) { return Math.abs(actual - expected) < 1e-6; }
}
