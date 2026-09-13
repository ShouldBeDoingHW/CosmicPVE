package com.cosmicpve.entity.woodlands;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.adventure.DenseWoodlandsSessionService;
import com.cosmicpve.combat.enchantment.EnchantmentLevels;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.registry.ModEntities;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Loaded-registry/entity proof for the Dense Woodlands hostile-mob milestone. */
public final class DenseWoodlandsGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, CosmicPVE.MOD_ID);
    static { FUNCTIONS.register("dense_woodlands_mobs", ignored -> DenseWoodlandsGameTests::verify); }
    private DenseWoodlandsGameTests() {}

    public static void register(IEventBus bus) {
        FUNCTIONS.register(bus);
        bus.addListener(DenseWoodlandsGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                CosmicPVE.id("dense_woodlands_mobs_environment"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("dense_woodlands_mobs"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("dense_woodlands_mobs")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
    }

    private static void verify(GameTestHelper helper) {
        var enchantments = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        helper.assertTrue(enchantments.getOrThrow(ModEnchantments.NIMBLE).value().getMaxLevel() == 4,
                "Nimble IV must decode from the loaded registry");
        helper.assertTrue(enchantments.getOrThrow(ModEnchantments.THUNDERING_BLOW).value().getMaxLevel() == 3,
                "Thundering Blow III must decode from the loaded registry");
        helper.assertTrue(enchantments.getOrThrow(ModEnchantments.NEUTRALIZE).value().getMaxLevel() == 5,
                "Neutralize V must decode from the loaded registry");

        ForestFanaticEntity fanatic = helper.spawn(ModEntities.FOREST_FANATIC.get(), new BlockPos(2, 2, 2));
        fanatic.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(fanatic.blockPosition()),
                EntitySpawnReason.TRIGGERED, null);
        assertAttribute(helper, fanatic, Attributes.MAX_HEALTH, 25.0);
        assertAttribute(helper, fanatic, Attributes.ARMOR, 3.0);
        assertAttribute(helper, fanatic, Attributes.ARMOR_TOUGHNESS, 0.0);
        assertAttribute(helper, fanatic, Attributes.MOVEMENT_SPEED, .22);
        helper.assertTrue(fanatic.equipmentInitialized(), "Fanatic must generate its equipment at spawn");
        helper.assertTrue(fanatic.getMainHandItem().is(Items.BOW), "Fanatic must wield a real bow");
        helper.assertTrue(EnchantmentLevels.onStack(fanatic.getMainHandItem(), ModEnchantments.VENOM) == 3,
                "Fanatic bow must always carry Venom III");
        helper.assertTrue(ForestFanaticEquipmentService.DROP_CHANCE == 0.0F,
                "Fanatic equipment drop chances must be disabled");

        DreadmaneEntity dreadmane = helper.spawn(ModEntities.DREADMANE.get(), new BlockPos(5, 2, 5));
        dreadmane.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(dreadmane.blockPosition()),
                EntitySpawnReason.TRIGGERED, null);
        assertAttribute(helper, dreadmane, Attributes.MAX_HEALTH, 40.0);
        assertAttribute(helper, dreadmane, Attributes.ARMOR, 5.0);
        assertAttribute(helper, dreadmane, Attributes.ARMOR_TOUGHNESS, 0.0);
        assertAttribute(helper, dreadmane, Attributes.MOVEMENT_SPEED, .31);
        assertAttribute(helper, dreadmane, Attributes.ATTACK_DAMAGE, 7.5);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(dreadmane.interact(player, InteractionHand.MAIN_HAND) == InteractionResult.FAIL,
                "Dreadmane must reject horse interaction");
        helper.assertTrue(!dreadmane.isFood(Items.APPLE.getDefaultInstance()) && !dreadmane.canMate(dreadmane),
                "Dreadmane must reject feeding and breeding");
        helper.assertTrue(!dreadmane.canUseSlot(EquipmentSlot.SADDLE), "Dreadmane must reject saddles");

        helper.assertTrue(DenseWoodlandsMobSpawns.shouldSuppress(EntityType.ZOMBIE,
                DenseWoodlandsSessionService.DIMENSION, EntitySpawnReason.NATURAL),
                "vanilla natural monsters must be suppressed in Dense Woodlands");
        helper.assertTrue(!DenseWoodlandsMobSpawns.shouldSuppress(EntityType.ZOMBIE,
                Level.OVERWORLD, EntitySpawnReason.NATURAL), "ordinary dimensions must be unaffected");
        helper.assertTrue(!DenseWoodlandsMobSpawns.shouldSuppress(EntityType.ZOMBIE,
                DenseWoodlandsSessionService.DIMENSION, EntitySpawnReason.COMMAND),
                "manual/programmatic creation must remain available");
        helper.assertTrue(!DenseWoodlandsMobSpawns.shouldSuppress(ModEntities.FOREST_FANATIC.get(),
                DenseWoodlandsSessionService.DIMENSION, EntitySpawnReason.NATURAL),
                "native Fanatics must remain naturally spawnable");

        verifyDamageWithoutLivingAttacker(helper, fanatic);
        verifyLivingAndProjectileAttribution(helper);
        helper.succeed();
    }

    private static void verifyDamageWithoutLivingAttacker(GameTestHelper helper, ForestFanaticEntity fanatic) {
        var level = helper.getLevel();
        var poisonedPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
        var poisonedMob = helper.spawn(EntityType.COW, new BlockPos(7, 2, 2));

        float playerHealth = poisonedPlayer.getHealth();
        float mobHealth = poisonedMob.getHealth();
        float fanaticHealth = fanatic.getHealth();
        MobEffects.POISON.value().applyEffectTick(level, poisonedPlayer, 1);
        MobEffects.POISON.value().applyEffectTick(level, poisonedMob, 1);
        MobEffects.POISON.value().applyEffectTick(level, fanatic, 1);
        helper.assertTrue(poisonedPlayer.getHealth() < playerHealth,
                "an unattributed Poison tick must still damage a player");
        helper.assertTrue(poisonedMob.getHealth() < mobHealth,
                "an unattributed Poison tick must still damage a living mob");
        helper.assertTrue(fanatic.getHealth() < fanaticHealth,
                "an unattributed Poison tick must still damage a Forest Fanatic");

        var fallTarget = helper.spawn(EntityType.PIG, new BlockPos(8, 2, 2));
        float fallHealth = fallTarget.getHealth();
        helper.assertTrue(fallTarget.hurtServer(level, level.damageSources().fall(), 2.0F),
                "unattributed fall damage must remain accepted");
        helper.assertTrue(fallTarget.getHealth() < fallHealth,
                "unattributed fall damage must still reduce health");
    }

    private static void verifyLivingAndProjectileAttribution(GameTestHelper helper) {
        var level = helper.getLevel();
        var shooter = helper.spawn(EntityType.SKELETON, new BlockPos(7, 2, 5));
        var meleeTarget = helper.spawn(EntityType.COW, new BlockPos(8, 2, 5));
        float meleeHealth = meleeTarget.getHealth();
        helper.assertTrue(meleeTarget.hurtServer(level, level.damageSources().mobAttack(shooter), 2.0F),
                "living melee damage must retain pre-defense processing");
        helper.assertTrue(meleeTarget.getHealth() < meleeHealth, "living melee damage must still apply");

        var projectileTarget = helper.spawn(EntityType.COW, new BlockPos(9, 2, 5));
        Arrow ownedArrow = EntityType.ARROW.create(level, EntitySpawnReason.TRIGGERED);
        helper.assertTrue(ownedArrow != null, "test arrow must construct");
        ownedArrow.setOwner(shooter);
        var ownedSource = level.damageSources().arrow(ownedArrow, shooter);
        var ownedAttribution = new com.cosmicpve.combat.attribution.DamageAttributionService().resolve(ownedSource);
        helper.assertTrue(ownedAttribution.attacker() == shooter,
                "a projectile must resolve its living owner as the attacker");
        float projectileHealth = projectileTarget.getHealth();
        helper.assertTrue(projectileTarget.hurtServer(level, ownedSource, 2.0F),
                "living-owned projectile damage must retain pre-defense processing");
        helper.assertTrue(projectileTarget.getHealth() < projectileHealth,
                "living-owned projectile damage must still apply");

        var ownerlessTarget = helper.spawn(EntityType.COW, new BlockPos(10, 2, 5));
        Arrow ownerlessArrow = EntityType.ARROW.create(level, EntitySpawnReason.TRIGGERED);
        helper.assertTrue(ownerlessArrow != null, "ownerless test arrow must construct");
        var ownerlessSource = level.damageSources().arrow(ownerlessArrow, null);
        var ownerlessAttribution = new com.cosmicpve.combat.attribution.DamageAttributionService().resolve(ownerlessSource);
        helper.assertTrue(ownerlessAttribution.attacker() == null,
                "an ownerless projectile must use the safe no-attacker path");
        float ownerlessHealth = ownerlessTarget.getHealth();
        helper.assertTrue(ownerlessTarget.hurtServer(level, ownerlessSource, 2.0F),
                "ownerless projectile damage must remain accepted");
        helper.assertTrue(ownerlessTarget.getHealth() < ownerlessHealth,
                "ownerless projectile damage must still apply");
    }

    private static void assertAttribute(GameTestHelper helper, net.minecraft.world.entity.LivingEntity entity,
            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double expected) {
        helper.assertTrue(Math.abs(entity.getAttributeValue(attribute) - expected) < 1.0E-9,
                attribute.getRegisteredName() + " must equal " + expected);
    }
}
