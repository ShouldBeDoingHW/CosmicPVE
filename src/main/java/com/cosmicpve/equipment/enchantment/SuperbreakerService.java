package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

/** Each unstackable enchanted pickaxe carries its own persistent ability expiry. */
public final class SuperbreakerService {
    public static final long COOLDOWN_TICKS = 2400L;
    public static final int HASTE_AMPLIFIER = 4;
    private final EffectiveEnchantmentsResolver enchantments = new EffectiveEnchantmentsResolver();

    public static int durationTicks(int level) {
        return 20 * (10 + Math.clamp(level, 1, 10));
    }

    public static long remainingTicks(ItemStack tool, long currentGameTime) {
        return Math.max(0L, tool.getOrDefault(ModDataComponents.SUPERBREAKER_READY_AT.get(), 0L)
                - currentGameTime);
    }

    public boolean activate(ServerPlayer player, ItemStack tool) {
        if (!tool.is(ItemTags.PICKAXES) || player.isSpectator()) return false;
        int level = enchantments.resolve(player, tool, List.of()).level(ModEnchantments.SUPERBREAKER.identifier());
        if (level <= 0) return false;
        long gameTime = player.level().getServer().overworld().getGameTime();
        if (remainingTicks(tool, gameTime) > 0L) return false;
        int duration = durationTicks(level);
        var existing = player.getEffect(MobEffects.HASTE);
        if (existing != null && existing.getAmplifier() > HASTE_AMPLIFIER) return false;
        // Vanilla rejects an equal/shorter same-level reapplication. That already-active Haste V
        // satisfies this pickaxe's effect without shortening it, so the second item still activates.
        boolean alreadyCovered = existing != null && existing.getAmplifier() == HASTE_AMPLIFIER
                && existing.getDuration() >= duration;
        if (!alreadyCovered && !player.addEffect(new MobEffectInstance(MobEffects.HASTE, duration, HASTE_AMPLIFIER)))
            return false;
        tool.set(ModDataComponents.SUPERBREAKER_READY_AT.get(), gameTime + COOLDOWN_TICKS);
        return true;
    }
}
