package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public final class OxygenateService {
    public static final int DISPLAYED_AIR_BUBBLES = 10;
    public static final int AIR_PER_BUBBLE = Entity.TOTAL_AIR_SUPPLY / DISPLAYED_AIR_BUBBLES;
    private final EffectiveEnchantmentsResolver enchantments;

    public OxygenateService(EffectiveEnchantmentsResolver enchantments) { this.enchantments = enchantments; }

    public int applyCompletedBreak(ServerPlayer player, ItemStack tool) {
        boolean underwater = player.isEyeInFluid(FluidTags.WATER);
        boolean pickaxe = tool.is(ItemTags.PICKAXES);
        if (!underwater || !pickaxe) return 0;
        int level = enchantments.resolve(tool, List.of()).level(ModEnchantments.OXYGENATE.identifier());
        int restoration = restorationAmount(underwater, pickaxe, level);
        if (restoration == 0) return 0;
        int before = player.getAirSupply();
        int after = Math.min(player.getMaxAirSupply(), before + restoration);
        player.setAirSupply(after);
        return after - before;
    }

    public static int restorationAmount(boolean underwater, boolean pickaxe, int level) {
        return underwater && pickaxe && level > 0 ? AIR_PER_BUBBLE * level : 0;
    }

    public static int restoredAir(int current, int maximum, int level) {
        if (level <= 0) return Math.min(current, maximum);
        return Math.min(maximum, current + restorationAmount(true, true, level));
    }
}
