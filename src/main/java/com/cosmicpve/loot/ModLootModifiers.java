package com.cosmicpve.loot;

import com.cosmicpve.CosmicPVE;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModLootModifiers {
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, CosmicPVE.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<DiamondArmorLootModifier>>
            DIAMOND_ARMOR_REPLACEMENT = TYPES.register("diamond_armor_replacement",
                    () -> DiamondArmorLootModifier.CODEC);

    private ModLootModifiers() {}

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
