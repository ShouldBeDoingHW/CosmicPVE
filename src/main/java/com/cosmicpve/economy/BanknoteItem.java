package com.cosmicpve.economy;

import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class BanknoteItem extends Item {
    public BanknoteItem(Properties properties) { super(properties); }

    @Override public Component getName(ItemStack stack) {
        var data = stack.get(ModDataComponents.BANKNOTE.get());
        return data == null ? super.getName(stack)
                : Component.translatable("item.cosmicpve.banknote.valued", MoneyAmount.format(data.valueCents()));
    }

    @Override public InteractionResult use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.FAIL;
        ItemStack held = player.getItemInHand(hand);
        var data = held.get(ModDataComponents.BANKNOTE.get());
        var money = new MoneyService();
        if (data == null || data.dataVersion() != com.cosmicpve.data.component.BanknoteData.CURRENT_DATA_VERSION
                || !money.add(serverPlayer, data.valueCents())) {
            player.displayClientMessage(Component.translatable("message.cosmicpve.banknote.invalid"), true);
            return InteractionResult.FAIL;
        }
        held.shrink(1);
        level.playSound(null, player.blockPosition(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1F, 1F);
        player.displayClientMessage(Component.translatable("message.cosmicpve.banknote.redeemed",
                MoneyAmount.format(data.valueCents()), MoneyAmount.format(money.balance(serverPlayer))), true);
        return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(held);
    }
}
