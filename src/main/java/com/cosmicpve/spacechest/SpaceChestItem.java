package com.cosmicpve.spacechest;

import com.cosmicpve.registry.ModDataComponents;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class SpaceChestItem extends Item {
    public SpaceChestItem(Properties properties) { super(properties); }

    @Override public Component getName(ItemStack stack) {
        var data = stack.get(ModDataComponents.SPACE_CHEST.get());
        if (data == null) return super.getName(stack);
        return Component.translatable("item.cosmicpve.space_chest.named",
                Component.translatable("cosmic_tier.cosmicpve." + data.tier().serializedName()))
                .withColor(data.tier().color());
    }

    @Override public InteractionResult use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.FAIL;
        ItemStack stack = player.getItemInHand(hand);
        var data = stack.get(ModDataComponents.SPACE_CHEST.get());
        if (data == null || !SpaceChestSessionService.INSTANCE.open(serverPlayer, stack, data.tier())) {
            player.displayClientMessage(Component.translatable("message.cosmicpve.space_chest.invalid"), true);
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    public static List<Component> lore() {
        return List.of(Component.translatable("tooltip.cosmicpve.space_chest.purpose").withStyle(ChatFormatting.YELLOW),
                Component.translatable("tooltip.cosmicpve.space_chest.instruction").withStyle(ChatFormatting.GRAY));
    }
    public static List<Component> lore(com.cosmicpve.spacechest.SpaceChestTier tier) {
        return switch (tier) {
            case ULTIMATE, LEGENDARY, MASTERY -> List.of(Component.literal(
                    "Contains 5 powerful " + tier.name().charAt(0) + tier.name().substring(1).toLowerCase(java.util.Locale.ROOT)
                            + " tier items! Right click to try your luck!").withStyle(ChatFormatting.YELLOW));
            default -> lore();
        };
    }
}
