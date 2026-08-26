package com.cosmicpve.entity.undeadcorpse;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Reusable hostile corpse mob. Trial provenance is optional and persisted independently of its presentation. */
public final class UndeadCorpseEntity extends Zombie {
    public static final float RENDER_SCALE = 0.9F;
    public static final double MAX_HEALTH = 15.0D;
    public static final double MOVEMENT_SPEED = 0.4D;
    private static final String EQUIPMENT = "CosmicEquipmentInitialized";
    private static final String SESSION = "CosmicTrialSession";
    private static final String ATTEMPT = "CosmicTrialAttempt";
    private static final String WAVE = "CosmicTrialWave";
    private boolean equipmentInitialized;
    private Optional<UUID> trialSession = Optional.empty();
    private Optional<UUID> trialAttempt = Optional.empty();
    private int trialWave;

    public UndeadCorpseEntity(EntityType<? extends UndeadCorpseEntity> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.ARMOR, 0.0D).add(Attributes.ARMOR_TOUGHNESS, 0.0D);
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
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input); equipmentInitialized = input.getBooleanOr(EQUIPMENT, false);
        trialSession = input.read(SESSION, net.minecraft.core.UUIDUtil.CODEC);
        trialAttempt = input.read(ATTEMPT, net.minecraft.core.UUIDUtil.CODEC);
        trialWave = input.getIntOr(WAVE, 0);
    }
}
