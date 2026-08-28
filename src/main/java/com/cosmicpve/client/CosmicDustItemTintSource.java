package com.cosmicpve.client;

import com.cosmicpve.registry.ModDataComponents;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class CosmicDustItemTintSource implements ItemTintSource {
    public static final CosmicDustItemTintSource INSTANCE = new CosmicDustItemTintSource();
    public static final MapCodec<CosmicDustItemTintSource> MAP_CODEC = MapCodec.unit(INSTANCE);
    private CosmicDustItemTintSource() {}
    @Override public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        var data = stack.get(ModDataComponents.COSMIC_DUST.get());
        return data == null ? 0xFFFFFFFF : 0xFF000000 | data.tier().tooltipColor();
    }
    @Override public MapCodec<? extends ItemTintSource> type() { return MAP_CODEC; }
}
