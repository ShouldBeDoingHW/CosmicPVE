package com.cosmicpve.registry;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.entity.spacepirate.SpacePirateVariant1;
import com.cosmicpve.entity.spacepirate.SpacePirateVariant2;
import com.cosmicpve.entity.undeadcorpse.UndeadCorpseEntity;
import com.cosmicpve.entity.inventor.InventorEntity;
import com.cosmicpve.entity.woodlands.DreadmaneEntity;
import com.cosmicpve.entity.woodlands.ForestFanaticEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(CosmicPVE.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<SpacePirateVariant1>> SPACE_PIRATE_VARIANT_1 =
            ENTITIES.registerEntityType("space_pirate_variant_1", SpacePirateVariant1::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.6F * 1.35F, 1.95F * 1.35F).clientTrackingRange(8)
                            .noLootTable().notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<SpacePirateVariant2>> SPACE_PIRATE_VARIANT_2 =
            ENTITIES.registerEntityType("space_pirate_variant_2", SpacePirateVariant2::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.7F * 1.35F, 2.4F * 1.35F).clientTrackingRange(8)
                            .noLootTable().notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<UndeadCorpseEntity>> UNDEAD_CORPSE =
            ENTITIES.registerEntityType("undead_corpse", UndeadCorpseEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.6F * UndeadCorpseEntity.RENDER_SCALE,
                                    1.95F * UndeadCorpseEntity.RENDER_SCALE)
                            .clientTrackingRange(8).noLootTable().notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<InventorEntity>> INVENTOR =
            ENTITIES.registerEntityType("inventor", InventorEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.6F * InventorEntity.SCALE, 1.95F * InventorEntity.SCALE)
                            .clientTrackingRange(8).noLootTable().notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<ForestFanaticEntity>> FOREST_FANATIC =
            ENTITIES.registerEntityType("forest_fanatic", ForestFanaticEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.6F * ForestFanaticEntity.RENDER_SCALE,
                                    1.99F * ForestFanaticEntity.RENDER_SCALE)
                            .clientTrackingRange(8).noLootTable().notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<DreadmaneEntity>> DREADMANE =
            ENTITIES.registerEntityType("dreadmane", DreadmaneEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(1.3965F * DreadmaneEntity.RENDER_SCALE,
                                    1.6F * DreadmaneEntity.RENDER_SCALE)
                            .clientTrackingRange(8).noLootTable().notInPeaceful());

    private ModEntities() {}

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        modBus.addListener(ModEntities::registerAttributes);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(SPACE_PIRATE_VARIANT_1.get(), SpacePirateVariant1.createAttributes().build());
        event.put(SPACE_PIRATE_VARIANT_2.get(), SpacePirateVariant2.createAttributes().build());
        event.put(UNDEAD_CORPSE.get(), UndeadCorpseEntity.createAttributes().build());
        event.put(INVENTOR.get(), InventorEntity.createAttributes().build());
        event.put(FOREST_FANATIC.get(), ForestFanaticEntity.createAttributes().build());
        event.put(DREADMANE.get(), DreadmaneEntity.createAttributes().build());
    }
}
