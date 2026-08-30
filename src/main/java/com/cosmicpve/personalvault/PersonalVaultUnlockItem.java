package com.cosmicpve.personalvault;

import com.cosmicpve.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class PersonalVaultUnlockItem extends Item {
    public PersonalVaultUnlockItem(Properties properties) { super(properties); }
    @Override public boolean isFoil(ItemStack stack) { return true; }
    @Override public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(style -> style.withColor(0xFFFFFF).withBold(true));
    }
    @Override public InteractionResult use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.FAIL;
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(ModItems.PERSONAL_VAULT_UNLOCK.get())) return InteractionResult.FAIL;
        var before = PersonalVaultRuntime.vaults().data(serverPlayer);
        long unlockedVault = before.nextVaultNumber();
        int unlockedRow = before.nextRowWithinVault();
        InteractionResult result = commitUnlock(held,
                () -> PersonalVaultRuntime.vaults().unlockNextRow(serverPlayer),
                () -> PersonalVaultUnlockFeedback.play(serverPlayer));
        if (!(result instanceof InteractionResult.Success)) {
            serverPlayer.sendSystemMessage(Component.translatable("message.cosmicpve.personal_vault.unlock_failed"));
            return InteractionResult.FAIL;
        }
        serverPlayer.sendSystemMessage(Component.translatable("message.cosmicpve.personal_vault.unlocked",
                unlockedVault, unlockedRow));
        return result;
    }

    static InteractionResult commitUnlock(ItemStack held, java.util.function.BooleanSupplier progression,
            Runnable successfulFeedback) {
        if (held.isEmpty() || !progression.getAsBoolean()) return InteractionResult.FAIL;
        held.shrink(1);
        successfulFeedback.run();
        return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(held);
    }
}
