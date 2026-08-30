package com.cosmicpve.registry;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.enchantment.DominateMobEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMobEffects {
    private static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, CosmicPVE.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> DOMINATED =
            EFFECTS.register("dominated", DominateMobEffect::new);

    private ModMobEffects() {}

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}
