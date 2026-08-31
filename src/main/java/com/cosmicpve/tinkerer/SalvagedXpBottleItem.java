package com.cosmicpve.tinkerer;

import com.cosmicpve.registry.ModDataComponents;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.IntConsumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A non-throwable typed XP receipt redeemed directly into raw player experience points. */
public final class SalvagedXpBottleItem extends Item {
    static final float REDEEM_PITCH = 1.5F;
    static SoundEvent redeemSound() { return SoundEvents.PLAYER_LEVELUP; }
    public SalvagedXpBottleItem(Properties properties) { super(properties); }

    @Override public Component getName(ItemStack stack) {
        return Component.translatable("item.cosmicpve.salvaged_xp_bottle");
    }

    public static long storedXp(ItemStack stack) {
        var data = stack.get(ModDataComponents.STORED_XP_BOTTLE.get());
        return data == null ? 0 : data.storedXp();
    }

    @Override public InteractionResult use(Level level, net.minecraft.world.entity.player.Player player,
            InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        long amount = storedXp(held);
        if (level.isClientSide()) return validForRedemption(held, player.totalExperience)
                ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        if (!(player instanceof ServerPlayer serverPlayer)
                || !redeem(held, player.totalExperience, player::giveExperiencePoints)) {
            return InteractionResult.FAIL;
        }
        serverPlayer.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(redeemSound()), SoundSource.PLAYERS,
                player.getX(), player.getY(), player.getZ(), 1.0F, REDEEM_PITCH, player.getRandom().nextLong()));
        serverPlayer.sendSystemMessage(successMessage(amount));
        return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(held);
    }

    static Component successMessage(long amount) {
        return Component.literal("+" + NumberFormat.getIntegerInstance(Locale.US).format(amount) + " XP")
                .withStyle(style -> style.withColor(0x55FF55).withBold(true));
    }

    static boolean redeem(ItemStack stack, int currentTotalXp, IntConsumer grant) {
        if (!validForRedemption(stack, currentTotalXp)) return false;
        int amount = (int) storedXp(stack);
        grant.accept(amount);
        stack.shrink(1);
        return true;
    }

    static boolean validForRedemption(ItemStack stack, int currentTotalXp) {
        var data = stack.get(ModDataComponents.STORED_XP_BOTTLE.get());
        if (data == null || data.dataVersion() != com.cosmicpve.data.component.StoredXpBottleData.CURRENT_DATA_VERSION)
            return false;
        long amount = data.storedXp();
        return amount > 0 && amount <= Integer.MAX_VALUE && currentTotalXp >= 0
                && amount <= (long) Integer.MAX_VALUE - currentTotalXp;
    }
}
