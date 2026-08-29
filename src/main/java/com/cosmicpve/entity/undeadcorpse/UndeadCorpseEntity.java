package com.cosmicpve.entity.undeadcorpse;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Reusable hostile corpse mob. Trial provenance is optional and persisted independently of its presentation. */
public final class UndeadCorpseEntity extends Zombie implements OwnableEntity {
    public static final float RENDER_SCALE = 0.9F;
    public static final double MAX_HEALTH = 15.0D;
    public static final double MOVEMENT_SPEED = 0.4D;
    private static final String EQUIPMENT = "CosmicEquipmentInitialized";
    private static final String SESSION = "CosmicTrialSession";
    private static final String ATTEMPT = "CosmicTrialAttempt";
    private static final String WAVE = "CosmicTrialWave";
    private static final String SUMMONED_OWNER = "CosmicSummonedOwner";
    private static final String SUMMONED_EXPIRES = "CosmicSummonedExpires";
    private boolean equipmentInitialized;
    private Optional<UUID> trialSession = Optional.empty();
    private Optional<UUID> trialAttempt = Optional.empty();
    private int trialWave;
    private EntityReference<LivingEntity> summonedOwner;
    private long summonedExpires;
    private transient boolean summonedTracked;

    public UndeadCorpseEntity(EntityType<? extends UndeadCorpseEntity> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.ARMOR, 0.0D).add(Attributes.ARMOR_TOUGHNESS, 0.0D);
    }

    @Override protected void registerGoals() {
        super.registerGoals();
        targetSelector.addGoal(1, new OwnedAllyTargetGoal(this,
                LivingEntity::getLastHurtByMob, LivingEntity::getLastHurtByMobTimestamp));
        targetSelector.addGoal(2, new OwnedAllyTargetGoal(this,
                LivingEntity::getLastHurtMob, LivingEntity::getLastHurtMobTimestamp));
    }

    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            EntitySpawnReason reason, SpawnGroupData spawnData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        if (!equipmentInitialized) {
            UndeadCorpseEquipmentService.equipBase(this, level.registryAccess(), getRandom());
            equipmentInitialized = true;
        }
        return result;
    }

    public void markTrialEncounter(UUID session, UUID attempt, int wave) {
        trialSession = Optional.of(session); trialAttempt = Optional.of(attempt); trialWave = wave;
        setPersistenceRequired(); skipDropExperience();
    }
    public void markSummonedAlly(LivingEntity owner, long expiresAtGameTime) {
        summonedOwner = EntityReference.of(owner); summonedExpires = expiresAtGameTime;
        summonedTracked = true;
        setPersistenceRequired(); skipDropExperience();
    }
    @Override public EntityReference<LivingEntity> getOwnerReference() { return summonedOwner; }
    public Optional<UUID> summonedOwnerId() {
        return summonedOwner == null ? Optional.empty() : Optional.of(summonedOwner.getUUID());
    }
    @Override public boolean canAttack(LivingEntity target) {
        return !com.cosmicpve.combat.ownership.OwnedAllyResolver.allied(this, target) && super.canAttack(target);
    }
    @Override public void tick() {
        super.tick();
        if (!level().isClientSide() && summonedOwner != null && !summonedTracked) {
            com.cosmicpve.combat.enchantment.UndeadRuseBehavior.track(this);
            summonedTracked = true;
        }
        if (!level().isClientSide() && summonedOwner != null && level().getGameTime() >= summonedExpires) discard();
    }
    public boolean trialEncounter(UUID session, UUID attempt) {
        return trialSession.filter(session::equals).isPresent() && trialAttempt.filter(attempt::equals).isPresent();
    }
    public Optional<UUID> trialSession() { return trialSession; }
    public Optional<UUID> trialAttempt() { return trialAttempt; }
    public int trialWave() { return trialWave; }
    public boolean equipmentInitialized() { return equipmentInitialized; }

    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output); output.putBoolean(EQUIPMENT, equipmentInitialized);
        trialSession.ifPresent(value -> output.store(SESSION, net.minecraft.core.UUIDUtil.CODEC, value));
        trialAttempt.ifPresent(value -> output.store(ATTEMPT, net.minecraft.core.UUIDUtil.CODEC, value));
        if (trialWave > 0) output.putInt(WAVE, trialWave);
        EntityReference.store(summonedOwner, output, SUMMONED_OWNER);
        if (summonedExpires > 0) output.putLong(SUMMONED_EXPIRES, summonedExpires);
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input); equipmentInitialized = input.getBooleanOr(EQUIPMENT, false);
        trialSession = input.read(SESSION, net.minecraft.core.UUIDUtil.CODEC);
        trialAttempt = input.read(ATTEMPT, net.minecraft.core.UUIDUtil.CODEC);
        trialWave = input.getIntOr(WAVE, 0);
        summonedOwner = EntityReference.read(input, SUMMONED_OWNER);
        summonedExpires = input.getLongOr(SUMMONED_EXPIRES, 0L);
    }
}
