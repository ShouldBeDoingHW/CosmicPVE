package com.cosmicpve.entity.woodlands;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.adventure.DenseWoodlandsSessionService;
import com.cosmicpve.combat.enchantment.EnchantmentLevels;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.registry.ModEntities;
import java.util.function.Consumer;
import java.util.List;
import java.util.Set;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
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
        assertAttribute(helper, fanatic, Attributes.ARMOR, 4.0);
        assertAttribute(helper, fanatic, Attributes.ARMOR_TOUGHNESS, 0.0);
        assertAttribute(helper, fanatic, Attributes.MOVEMENT_SPEED, .22);
        helper.assertTrue(fanatic.equipmentInitialized(), "Fanatic must generate its equipment at spawn");
        helper.assertTrue(fanatic.getMainHandItem().is(Items.BOW), "Fanatic must wield a real bow");
        helper.assertTrue(EnchantmentLevels.onStack(fanatic.getMainHandItem(), ModEnchantments.VENOM) == 3,
                "Fanatic bow must always carry Venom III");
        helper.assertTrue(ForestFanaticEquipmentService.DROP_CHANCE == 0.0F,
                "Fanatic equipment drop chances must be disabled");
        helper.assertTrue(ForestFanaticEntity.MIN_EXPERIENCE == 15 && ForestFanaticEntity.MAX_EXPERIENCE == 20,
                "Ordinary Fanatics must use the inclusive 15-20 vanilla XP range");

        DreadmaneEntity dreadmane = helper.spawn(ModEntities.DREADMANE.get(), new BlockPos(5, 2, 5));
        dreadmane.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(dreadmane.blockPosition()),
                EntitySpawnReason.TRIGGERED, null);
        assertAttribute(helper, dreadmane, Attributes.MAX_HEALTH, 40.0);
        assertAttribute(helper, dreadmane, Attributes.ARMOR, 6.5);
        assertAttribute(helper, dreadmane, Attributes.ARMOR_TOUGHNESS, 0.0);
        assertAttribute(helper, dreadmane, Attributes.MOVEMENT_SPEED, .31);
        assertAttribute(helper, dreadmane, Attributes.ATTACK_DAMAGE, 7.5);
        helper.assertTrue(DreadmaneEntity.MIN_EXPERIENCE == 25 && DreadmaneEntity.MAX_EXPERIENCE == 35,
                "Ordinary Dreadmanes must use the inclusive 25-35 vanilla XP range");
        verifyDreadmaneCombat(helper, dreadmane, "ordinary Dreadmane");
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(dreadmane.interact(player, InteractionHand.MAIN_HAND) == InteractionResult.FAIL,
                "Dreadmane must reject horse interaction");
        helper.assertTrue(!dreadmane.isFood(Items.APPLE.getDefaultInstance()) && !dreadmane.canMate(dreadmane),
                "Dreadmane must reject feeding and breeding");
        helper.assertTrue(!dreadmane.canUseSlot(EquipmentSlot.SADDLE), "Dreadmane must reject saddles");
        var rider = dreadmane.createJockeyRider(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(dreadmane.blockPosition()));
        helper.assertTrue(rider != null && rider.getVehicle() == dreadmane && rider.equipmentInitialized(),
                "Jockey must attach a normally equipped Forest Fanatic");
        helper.assertTrue(dreadmane.getControllingPassenger() == null && !dreadmane.isMobControlled(),
                "Jockey rider must not disable autonomous mount AI");
        helper.assertTrue(!com.cosmicpve.adventure.AdventureRules.restricted(rider), "Native rider is not player-restricted");
        rider.stopRiding();
        helper.assertTrue(!dreadmane.isVehicle() && dreadmane.interact(player, InteractionHand.MAIN_HAND) == InteractionResult.FAIL,
                "Dismount leaves a hostile non-player-mountable Dreadmane");
        rider.discard();

        DreadmaneEntity jockeyMount = helper.spawn(ModEntities.DREADMANE.get(), new BlockPos(12, 2, 4));
        jockeyMount.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(jockeyMount.blockPosition()),
                EntitySpawnReason.TRIGGERED, null);
        ForestFanaticEntity jockeyRider = jockeyMount.createJockeyRider(helper.getLevel(),
                helper.getLevel().getCurrentDifficultyAt(jockeyMount.blockPosition()));
        helper.assertTrue(jockeyRider != null && jockeyRider.getVehicle() == jockeyMount,
                "Jockey combat test requires two real attached entities");
        verifyDreadmaneCombat(helper, jockeyMount, "Woodlands Jockey mount");
        jockeyRider.discard();
        jockeyMount.discard();

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
        verifyRanger(helper, fanatic);
        helper.succeed();
    }

    private static void verifyDreadmaneCombat(GameTestHelper helper, DreadmaneEntity dreadmane, String subject) {
        var level = helper.getLevel();
        var normalTarget = helper.spawn(EntityType.COW, dreadmane.blockPosition().offset(3, 0, 0));
        normalTarget.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0);
        normalTarget.setHealth(40.0F);
        normalTarget.snapTo(dreadmane.getX() + 2.0, dreadmane.getY(), dreadmane.getZ(), 0, 0);
        helper.assertTrue(dreadmane.doHurtTarget(level, normalTarget), subject + " normal melee must hit");
        helper.assertTrue(Math.abs(normalTarget.getHealth() - 32.5F) < .001F,
                subject + " normal melee must remain one 7.5-damage packet");

        var failedTarget = helper.spawn(EntityType.COW, dreadmane.blockPosition().offset(1, 0, 0));
        failedTarget.setInvulnerable(true);
        failedTarget.snapTo(dreadmane.getX() + 1.2, dreadmane.getY(), dreadmane.getZ(), 0, 0);
        long beforeFailedBuck = dreadmane.nextBuckTick();
        helper.assertTrue(!dreadmane.doHurtTarget(level, failedTarget), subject + " invulnerable Buck must fail");
        helper.assertTrue(dreadmane.nextBuckTick() == beforeFailedBuck,
                subject + " failed Buck must not begin its cooldown");

        var buckTarget = helper.spawn(EntityType.COW, dreadmane.blockPosition().offset(1, 0, 1));
        buckTarget.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0);
        buckTarget.setHealth(40.0F);
        buckTarget.snapTo(dreadmane.getX() + 1.2, dreadmane.getY(), dreadmane.getZ(), 0, 0);
        var packets = new java.util.concurrent.atomic.AtomicInteger();
        float[] packetAmount = {Float.NaN};
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent> observer = event -> {
            if (event.getEntity() == buckTarget && event.getSource().getEntity() == dreadmane) {
                packets.incrementAndGet();
                packetAmount[0] = event.getAmount();
            }
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                net.neoforged.bus.api.EventPriority.HIGHEST, observer);
        long hitTime = level.getGameTime();
        boolean hit;
        try { hit = dreadmane.doHurtTarget(level, buckTarget); }
        finally { net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(observer); }
        helper.assertTrue(hit && packets.get() == 1 && Math.abs(packetAmount[0] - 11.0F) < .001F,
                subject + " Buck must resolve as exactly one direct 11-damage packet");
        helper.assertTrue(Math.abs(buckTarget.getHealth() - 29.0F) < .001F,
                subject + " Buck must replace normal melee rather than stack with it");
        helper.assertTrue(dreadmane.nextBuckTick() == hitTime + 200,
                subject + " successful Buck must begin the unchanged 10-second cooldown");
        var slowness = buckTarget.getEffect(MobEffects.SLOWNESS);
        helper.assertTrue(slowness != null && slowness.getAmplifier() == 1 && slowness.getDuration() == 35,
                subject + " Buck must retain Slowness II for 1.75 seconds");
        helper.assertTrue(buckTarget.getDeltaMovement().horizontalDistance() > 0.0,
                subject + " Buck must retain its knockback");
        helper.assertTrue(dreadmane.isStanding(), subject + " Buck must retain the horse rear animation");
        normalTarget.discard();
        failedTarget.discard();
        buckTarget.discard();
    }

    private static void verifyRanger(GameTestHelper helper, ForestFanaticEntity attacker) {
        var level=helper.getLevel();var ranger=helper.spawn(ModEntities.COSMIC_RANGER.get(),new BlockPos(12,2,12));
        ranger.finalizeSpawn(level,level.getCurrentDifficultyAt(ranger.blockPosition()),EntitySpawnReason.TRIGGERED,null);
        var markers=List.of(helper.absolutePos(new BlockPos(10,2,10)),helper.absolutePos(new BlockPos(14,2,10)),
                helper.absolutePos(new BlockPos(10,2,14)),helper.absolutePos(new BlockPos(14,2,14)));
        BlockPos min=helper.absolutePos(new BlockPos(6,1,6)),max=helper.absolutePos(new BlockPos(18,8,18));
        var participant=mockServerPlayer(helper);participant.snapTo(ranger.getX(),ranger.getY(),ranger.getZ(),0,0);
        ranger.initializeArena(markers,42L,BoundingBox.fromCorners(min,max),participant);
        var server=level.getServer();
        var home=new com.cosmicpve.adventure.AdventureSession.ReturnPoint(level.dimension(),participant.position(),0,0,GameType.SURVIVAL);
        var session=new com.cosmicpve.adventure.AdventureSession(participant.getUUID(),java.util.UUID.randomUUID(),10,
                com.cosmicpve.adventure.AdventureSession.Phase.ACTIVE,home,participant.blockPosition(),BlockPos.ZERO,
                level.getGameTime()+12_000,0,1);
        com.cosmicpve.adventure.AdventureSavedData.get(server).put(server,session);
        assertAttribute(helper,ranger,Attributes.MAX_HEALTH,350);assertAttribute(helper,ranger,Attributes.ARMOR,3);
        assertAttribute(helper,ranger,Attributes.ARMOR_TOUGHNESS,1);assertAttribute(helper,ranger,Attributes.MOVEMENT_SPEED,.28);
        helper.assertTrue(EnchantmentLevels.onStack(ranger.getMainHandItem(),ModEnchantments.PINPOINT)==6,"Ranger Bow Pinpoint VI");
        helper.assertTrue(EnchantmentLevels.onStack(ranger.getItemBySlot(EquipmentSlot.CHEST),ModEnchantments.LEADERSHIP)==10,"Leadership X clarification is live");
        helper.assertTrue(com.cosmicpve.combat.enchantment.SpiritLinkBehavior.equippedLevel(ranger)==14,"Aggregate Spirit Link XIV");
        participant.getInventory().setItem(4,new net.minecraft.world.item.ItemStack(Items.DIAMOND,3));
        participant.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.SPEED,200));
        float participantHealth=participant.getHealth();var participantSafe=participant.position();
        participant.snapTo(max.getX()+12,max.getY()+12,max.getZ()+12,0,0);participant.setDeltaMovement(3,3,3);ranger.tick();
        helper.assertTrue(participant.position().distanceToSqr(participantSafe)<.01&&participant.getDeltaMovement().lengthSqr()==0,
                "participating Adventure player cannot leave the actual 3D arena bounds");
        helper.assertTrue(participant.getHealth()==participantHealth&&participant.getInventory().getItem(4).is(Items.DIAMOND)
                &&participant.getInventory().getItem(4).getCount()==3&&participant.hasEffect(MobEffects.SPEED),
                "player correction preserves health, inventory, and effects");
        helper.assertTrue(ranger.hurtServer(level,level.damageSources().mobAttack(attacker),10_000),"Overkill threshold hit accepted");
        helper.assertTrue(ranger.phaseState()==1,"66% hit must enter wave one; state="+ranger.phaseState()+", health="+ranger.getHealth());
        helper.assertTrue(Math.abs(ranger.getHealth()-231)<.1,"Wave one must clamp at 66% health; health="+ranger.getHealth());
        helper.assertTrue(ranger.waveMembers().size()==8,"66% wave has four complete jockeys; tracked="+ranger.waveMembers().size());
        var expectedCardinals=Set.of(ranger.blockPosition().offset(0,0,-4),ranger.blockPosition().offset(4,0,0),
                ranger.blockPosition().offset(0,0,4),ranger.blockPosition().offset(-4,0,0));
        var actualMounts=ranger.waveMembers().stream().map(level::getEntity)
                .filter(com.cosmicpve.entity.woodlands.DreadmaneEntity.class::isInstance)
                .map(Entity::blockPosition).collect(java.util.stream.Collectors.toSet());
        helper.assertTrue(actualMounts.equals(expectedCardinals),"Outside Dense Woodlands, four mounts spawn exactly four blocks in each cardinal direction: "+actualMounts);
        for(var id:ranger.waveMembers())helper.assertTrue(com.cosmicpve.combat.ownership.OwnedAllyResolver.ownerId(level.getEntity(id)).orElseThrow().equals(ranger.getUUID()),"Every component is Ranger-owned");
        float boundaryHealth=ranger.getHealth();int boundaryPhase=ranger.phaseState();var safe=ranger.position();
        ranger.snapTo(max.getX()+10,max.getY()+10,max.getZ()+10,ranger.getYRot(),ranger.getXRot());ranger.setDeltaMovement(2,2,2);ranger.tick();
        helper.assertTrue(ranger.position().distanceToSqr(safe)<.01,"six-axis hard boundary returns Ranger to its last valid position");
        helper.assertTrue(ranger.getDeltaMovement().lengthSqr()==0&&ranger.getHealth()==boundaryHealth&&ranger.phaseState()==boundaryPhase,
                "boundary correction clears motion without healing, resetting, or changing phase");
        var mountId=ranger.waveMembers().stream().filter(id->level.getEntity(id) instanceof DreadmaneEntity).findFirst().orElseThrow();
        var escaped=level.getEntity(mountId);escaped.snapTo(max.getX()+10,max.getY()+10,max.getZ()+10,0,0);escaped.setDeltaMovement(2,2,2);ranger.tick();
        helper.assertTrue(CosmicRangerEntity.contains(ranger.arenaBounds().orElseThrow(),escaped.getBoundingBox()),"Ranger-owned Dreadmane is corrected in-bounds");
        helper.assertTrue(com.cosmicpve.combat.ownership.OwnedAllyResolver.ownerId(escaped).orElseThrow().equals(ranger.getUUID())&&ranger.phaseState()==1,
                "add correction preserves ownership and does not count it as defeated");
        float locked=ranger.getHealth();helper.assertTrue(!ranger.hurtServer(level,level.damageSources().mobAttack(attacker),5)&&ranger.getHealth()==locked,"Wave blocks direct damage");
        ranger.waveMembers().forEach(id->{var e=level.getEntity(id);if(e!=null)e.discard();});ranger.tick();
        helper.assertTrue(ranger.phaseState()==2,"Wave one completion unlocks boss");
        for(int i=0;i<11;i++)ranger.tick(); // model the combat gap after clearing a full wave
        helper.assertTrue(ranger.hurtServer(level,level.damageSources().mobAttack(attacker),10_000),"Second overkill threshold hit accepted");
        helper.assertTrue(ranger.phaseState()==3&&Math.abs(ranger.getHealth()-115.5)<.1&&ranger.waveMembers().size()==8,"33% wave cannot be skipped");
        ranger.waveMembers().forEach(id->{var e=level.getEntity(id);if(e!=null)e.discard();});ranger.tick();
        helper.assertTrue(ranger.phaseState()==4,"Wave two completion unlocks final combat");
        com.cosmicpve.adventure.AdventureSavedData.get(server).remove(server,participant.getUUID());
        var freePosition=new net.minecraft.world.phys.Vec3(max.getX()+14,max.getY()+14,max.getZ()+14);participant.snapTo(freePosition.x,freePosition.y,freePosition.z,0,0);ranger.tick();
        helper.assertTrue(participant.position().distanceToSqr(freePosition)<.01&&!ranger.participants().contains(participant.getUUID()),
                "Adventure termination immediately takes precedence and clears participant containment");
        ranger.discard();
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
    private static net.minecraft.server.level.ServerPlayer mockServerPlayer(GameTestHelper helper){
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent> configure=event->{
            if(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)
                net.neoforged.neoforge.network.registration.NetworkRegistry.configureMockConnection(player.connection.getConnection());
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGHEST,configure);
        try{return helper.makeMockServerPlayerInLevel();}finally{net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(configure);}
    }
}
