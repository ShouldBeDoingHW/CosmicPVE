package com.cosmicpve.entity.woodlands;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class ForestFanaticEntity extends AbstractSkeleton implements net.minecraft.world.entity.OwnableEntity {
    public static final float RENDER_SCALE = 1.1F;
    public static final double MAX_HEALTH = 25.0;
    public static final double ARMOR = 4.0;
    public static final double MOVEMENT_SPEED = 0.22;
    public static final double DROP_CHANCE = 0.25;
    public static final int MIN_EXPERIENCE = 15;
    public static final int MAX_EXPERIENCE = 20;
    private static final String EQUIPMENT = "CosmicEquipmentInitialized";
    private boolean equipmentInitialized;
    private net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity> encounterOwner;

    public ForestFanaticEntity(EntityType<? extends ForestFanaticEntity> type, Level level) { super(type, level); }

    // PathfinderMob otherwise rejects bright spawn positions through Monster's negative walk value.
    @Override public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level, EntitySpawnReason reason) { return true; }
    /** AbstractSkeleton may ignite after a valid bright spawn; native Woodlands mobs are daylight-safe. */
    @Override public void aiStep() {
        super.aiStep();
        if (!level().isClientSide() && level().dimension().equals(
                com.cosmicpve.adventure.DenseWoodlandsSessionService.DIMENSION)) clearFire();
    }
    @Override public boolean requiresCustomPersistence() {
        return getVehicle() instanceof DreadmaneEntity ? false : super.requiresCustomPersistence();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractSkeleton.createAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, ARMOR).add(Attributes.ARMOR_TOUGHNESS, 0.0)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED).add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            EntitySpawnReason reason, SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        if (!equipmentInitialized) {
            for (var slot : net.minecraft.world.entity.EquipmentSlot.VALUES) setItemSlot(slot, net.minecraft.world.item.ItemStack.EMPTY);
            new ForestFanaticEquipmentService().equip(this, level.registryAccess(), getRandom());
            equipmentInitialized = true;
            reassessWeaponGoal();
        }
        return result;
    }

    /** A disarmed Fanatic never gains a damaging skeleton melee fallback. */
    @Override public boolean doHurtTarget(ServerLevel level, Entity target) { return false; }

    @Override protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        DenseWoodlandsMobLoot.drop(this, level, DROP_CHANCE);
    }

    @Override protected int getBaseExperienceReward(ServerLevel level) {
        return experienceReward(getRandom().nextInt(MAX_EXPERIENCE - MIN_EXPERIENCE + 1));
    }

    static int experienceReward(int roll) {
        if (roll < 0 || roll > MAX_EXPERIENCE - MIN_EXPERIENCE) {
            throw new IllegalArgumentException("Experience roll is outside the inclusive reward range");
        }
        return MIN_EXPERIENCE + roll;
    }

    @Override protected SoundEvent getStepSound() { return SoundEvents.SKELETON_STEP; }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.BOGGED_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.BOGGED_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.BOGGED_DEATH; }

    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output); output.putBoolean(EQUIPMENT, equipmentInitialized);
        net.minecraft.world.entity.EntityReference.store(encounterOwner,output,"CosmicEncounterOwner");
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input); equipmentInitialized = input.getBooleanOr(EQUIPMENT, false);
        encounterOwner=net.minecraft.world.entity.EntityReference.read(input,"CosmicEncounterOwner");
    }
    public void markEncounterOwner(net.minecraft.world.entity.LivingEntity owner) {
        encounterOwner=net.minecraft.world.entity.EntityReference.of(owner);setPersistenceRequired();
    }
    void markEncounterOwner(net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity> owner) {
        encounterOwner=owner;setPersistenceRequired();
    }
    @Override public net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity> getOwnerReference(){return encounterOwner;}
    @Override public boolean canAttack(net.minecraft.world.entity.LivingEntity target){
        return !com.cosmicpve.combat.ownership.OwnedAllyResolver.allied(this,target)&&super.canAttack(target);
    }
    public boolean equipmentInitialized() { return equipmentInitialized; }
}
