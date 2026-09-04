package com.cosmicpve.entity.inventor;

import com.cosmicpve.registry.ModEntities;
import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Production Inventor boss. Encounter state lives on the entity so combat contributors stay generic. */
public final class InventorEntity extends ZombieVillager {
    public static final float SCALE = 0.88F;
    public static final double MOVEMENT_SPEED = 0.32D;
    public static final double ATTACK_DAMAGE = 2.0D;
    private static final String ACTIVE_STATIONS = "CosmicInventorActiveStations";
    private static final String STRIKE_CHARGES = "CosmicInventorStrikeCharges";

    private int activeStations;
    private final Set<UUID> strikeCharges = new HashSet<>();

    public InventorEntity(EntityType<? extends InventorEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.0D)
                .add(Attributes.SCALE, SCALE);
    }

    public int activeStations() { return activeStations; }
    public void setActiveStations(int value) { activeStations = Math.max(0, Math.min(4, value)); }
    public void grantStrikeCharge(UUID player) { strikeCharges.add(player); }
    public boolean hasStrikeCharge(UUID player) { return strikeCharges.contains(player); }
    public boolean consumeStrikeCharge(UUID player) { return strikeCharges.remove(player); }
    public Set<UUID> strikeCharges() { return Set.copyOf(strikeCharges); }
    public void clearStrikeCharges() { strikeCharges.clear(); }

    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(ACTIVE_STATIONS, activeStations);
        output.store(STRIKE_CHARGES, Codec.list(UUIDUtil.CODEC), strikeCharges.stream().toList());
    }

    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        activeStations = Math.max(0, Math.min(4, input.getIntOr(ACTIVE_STATIONS, 0)));
        strikeCharges.clear();
        input.read(STRIKE_CHARGES, Codec.list(UUIDUtil.CODEC)).ifPresent(strikeCharges::addAll);
    }
}
