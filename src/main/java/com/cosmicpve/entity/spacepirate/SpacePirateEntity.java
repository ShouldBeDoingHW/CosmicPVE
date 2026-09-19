package com.cosmicpve.entity.spacepirate;

import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class SpacePirateEntity extends Monster {
    private static final String EQUIPMENT_INITIALIZED_TAG = "CosmicEquipmentInitialized";
    private boolean equipmentInitialized;

    protected SpacePirateEntity(EntityType<? extends SpacePirateEntity> type, Level level) {
        super(type, level);
    }

    public abstract SpacePirateVariant variant();

    public static AttributeSupplier.Builder createAttributes(float health) {
        return net.minecraft.world.entity.Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, SpacePirateDefinition.MOVEMENT_SPEED)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ATTACK_DAMAGE, 1.5)
                .add(Attributes.ARMOR, 0.0)
                .add(Attributes.ARMOR_TOUGHNESS, 0.0);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            EntitySpawnReason reason, SpawnGroupData spawnData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        if (!equipmentInitialized) {
            new SpacePirateEquipmentService().equip(this, level.registryAccess(), getRandom());
            equipmentInitialized = true;
        }
        return result;
    }

    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean(EQUIPMENT_INITIALIZED_TAG, equipmentInitialized);
    }

    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        equipmentInitialized = input.getBooleanOr(EQUIPMENT_INITIALIZED_TAG, false);
    }

    public boolean isEquipmentInitialized() { return equipmentInitialized; }
}
