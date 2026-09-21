package com.cosmicpve.entity.woodlands;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

public final class DreadmaneEntity extends ZombieHorse implements net.minecraft.world.entity.OwnableEntity {
    public static final float RENDER_SCALE = 1.15F;
    public static final double MAX_HEALTH = 40.0;
    public static final double ARMOR = 6.5;
    public static final double MOVEMENT_SPEED = 0.31;
    public static final double ATTACK_DAMAGE = 7.5;
    public static final float BUCK_DAMAGE = 11.0F;
    public static final double BUCK_RANGE = 0.5;
    public static final double BUCK_KNOCKBACK = 1.5;
    public static final int BUCK_SLOWNESS_TICKS = 35;
    public static final int BUCK_COOLDOWN_TICKS = 200;
    public static final double DROP_CHANCE = 0.40;
    public static final int MIN_EXPERIENCE = 25;
    public static final int MAX_EXPERIENCE = 35;
    private long nextBuckTick;
    private net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity> encounterOwner;

    public DreadmaneEntity(EntityType<? extends DreadmaneEntity> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return ZombieHorse.createAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, ARMOR).add(Attributes.ARMOR_TOUGHNESS, 0.0)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty,
            EntitySpawnReason reason, SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty,
                reason == EntitySpawnReason.NATURAL ? EntitySpawnReason.EVENT : reason, data);
        setAge(0); setTamed(false);
        for (var slot : EquipmentSlot.VALUES) setItemSlot(slot, ItemStack.EMPTY);
        setCanPickUpLoot(false);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(MAX_HEALTH);
        getAttribute(Attributes.ARMOR).setBaseValue(ARMOR);
        getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(0.0);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(ATTACK_DAMAGE);
        setHealth((float) MAX_HEALTH);
        if (reason == EntitySpawnReason.NATURAL
                && level.getLevel().dimension().equals(com.cosmicpve.adventure.DenseWoodlandsSessionService.DIMENSION)
                && shouldSpawnJockey(getRandom().nextInt(100))) {
            createJockeyRider(level, difficulty);
        }
        return result;
    }

    public static boolean shouldSpawnJockey(int roll) {
        if (roll < 0 || roll >= 100) throw new IllegalArgumentException("Jockey roll must be 0-99");
        return roll < 15;
    }
    public ForestFanaticEntity createJockeyRider(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty) {
        if (!getPassengers().isEmpty()) return null;
        var rider = com.cosmicpve.registry.ModEntities.FOREST_FANATIC.get().create(level.getLevel(), EntitySpawnReason.JOCKEY);
        if (rider == null) return null;
        rider.snapTo(getX(), getY(), getZ(), getYRot(), 0);
        rider.finalizeSpawn(level, difficulty, EntitySpawnReason.JOCKEY, null);
        if (encounterOwner != null) rider.markEncounterOwner(encounterOwner);
        return rider.startRiding(this, true, false) ? rider : null;
    }
    public void markEncounterOwner(net.minecraft.world.entity.LivingEntity owner) {
        encounterOwner=net.minecraft.world.entity.EntityReference.of(owner); setPersistenceRequired();
    }
    void markEncounterOwner(net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity> owner) {
        encounterOwner=owner; setPersistenceRequired();
    }
    @Override public net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity> getOwnerReference(){return encounterOwner;}
    @Override protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput out){
        super.addAdditionalSaveData(out);net.minecraft.world.entity.EntityReference.store(encounterOwner,out,"CosmicEncounterOwner");
    }
    @Override protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput in){
        super.readAdditionalSaveData(in);encounterOwner=net.minecraft.world.entity.EntityReference.read(in,"CosmicEncounterOwner");
    }
    @Override public boolean canAttack(net.minecraft.world.entity.LivingEntity target){
        return !com.cosmicpve.combat.ownership.OwnedAllyResolver.allied(this,target)&&super.canAttack(target);
    }
    @Override public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level, EntitySpawnReason reason) { return true; }
    // The Fanatic remains a ranged passenger; it must not disable the mount's autonomous melee goals.
    @Override public net.minecraft.world.entity.LivingEntity getControllingPassenger() { return null; }
    @Override public boolean isMobControlled() { return false; }

    @Override public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (target instanceof net.minecraft.world.entity.LivingEntity living && buckReady(level.getGameTime())
                && collisionDistanceSquared(getBoundingBox(), living.getBoundingBox()) <= BUCK_RANGE * BUCK_RANGE) {
            standIfPossible();
            boolean hit = living.hurtServer(level, damageSources().mobAttack(this), BUCK_DAMAGE);
            if (hit) {
                living.knockback(BUCK_KNOCKBACK, getX() - living.getX(), getZ() - living.getZ());
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, BUCK_SLOWNESS_TICKS, 1), this);
                nextBuckTick = level.getGameTime() + BUCK_COOLDOWN_TICKS;
            }
            return hit;
        }
        return super.doHurtTarget(level, target);
    }

    public boolean buckReady(long gameTime) { return gameTime >= nextBuckTick; }
    public long nextBuckTick() { return nextBuckTick; }
    public static double collisionDistanceSquared(AABB first, AABB second) {
        double x = axisGap(first.minX, first.maxX, second.minX, second.maxX);
        double y = axisGap(first.minY, first.maxY, second.minY, second.maxY);
        double z = axisGap(first.minZ, first.maxZ, second.minZ, second.maxZ);
        return x * x + y * y + z * z;
    }
    private static double axisGap(double minA, double maxA, double minB, double maxB) {
        return maxA < minB ? minB - maxA : maxB < minA ? minA - maxB : 0.0;
    }

    @Override public InteractionResult interact(Player player, InteractionHand hand) { return InteractionResult.FAIL; }
    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) { return InteractionResult.FAIL; }
    @Override public boolean canUseSlot(EquipmentSlot slot) { return false; }
    @Override public boolean isFood(ItemStack stack) { return false; }
    @Override public void setTamed(boolean tamed) { super.setTamed(false); }
    @Override public boolean canMate(Animal other) { return false; }
    @Override public boolean canFallInLove() { return false; }
    @Override public boolean canBeLeashed() { return false; }
    @Override protected boolean canAddPassenger(Entity passenger) {
        return passenger instanceof ForestFanaticEntity && getPassengers().isEmpty();
    }

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
}
