package com.cosmicpve.client;

import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.equipment.armor.ArmorSetPresentationResolver;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jspecify.annotations.Nullable;

public final class ArmorSetItemTintSource implements ItemTintSource {
    public static final ArmorSetItemTintSource INSTANCE = new ArmorSetItemTintSource();
    public static final MapCodec<ArmorSetItemTintSource> MAP_CODEC = MapCodec.unit(INSTANCE);
    private ArmorSetItemTintSource() {}

    @Override public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        var presentationColor = ArmorSetPresentationResolver.color(stack);
        if (presentationColor.isPresent()) return 0xFF000000 | presentationColor.getAsInt();
        if (stack.has(ModDataComponents.HEROIC.get()) || stack.has(DataComponents.DYED_COLOR)
                || BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().startsWith("leather_"))
            return DyedItemColor.getOrDefault(stack, DyedItemColor.LEATHER_COLOR);
        return 0xFFFFFFFF;
    }

    @Override public MapCodec<? extends ItemTintSource> type() { return MAP_CODEC; }
}
