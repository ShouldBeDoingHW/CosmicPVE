package com.cosmicpve.client;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.registry.ModDataComponents;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class CosmicBookItemTintSource implements ItemTintSource {
    public static final CosmicBookItemTintSource INSTANCE = new CosmicBookItemTintSource();
    public static final MapCodec<CosmicBookItemTintSource> MAP_CODEC = MapCodec.unit(INSTANCE);
    private CosmicBookItemTintSource() {}
    @Override public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        var data = stack.get(ModDataComponents.COSMIC_ENCHANT_BOOK.get());
        if (data != null) return 0xFF000000 | CosmicEnchantmentSpecs.find(data.enchantmentId())
                .map(spec -> spec.tier().tooltipColor()).orElse(0xFFFFFF);
        var unexamined = stack.get(ModDataComponents.UNEXAMINED_BOOK.get());
        return unexamined == null ? 0xFFFFFFFF : 0xFF000000 | unexamined.tier().tooltipColor();
    }
    @Override public MapCodec<? extends ItemTintSource> type() { return MAP_CODEC; }
}
