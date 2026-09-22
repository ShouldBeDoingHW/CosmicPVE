package com.cosmicpve.reward.memory;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class MemoryChestItem extends Item implements com.cosmicpve.reward.preview.LootPreviewProvider {
    private static final int[] MEMORY_COLORS = {0xBF0F0F, 0xBF0F76, 0x870FBF, 0x0F26BF, 0x0FB0BF, 0x0FBF3E};

    public MemoryChestItem(Properties properties) { super(properties); }

    @Override public Component getName(ItemStack stack) { return displayName(); }
    @Override public boolean isFoil(ItemStack stack) { return true; }

    public static Component displayName() {
        MutableComponent name = Component.empty();
        String memory = "Memory";
        for (int index = 0; index < memory.length(); index++) {
            int color = MEMORY_COLORS[index];
            name.append(Component.literal(Character.toString(memory.charAt(index)))
                    .withStyle(style -> style.withColor(color).withBold(true)));
        }
        return name.append(Component.literal(" Chest")
                .withStyle(style -> style.withColor(0xFFFFFF).withBold(true)));
    }

    public static List<Component> lore() {
        return List.of(
                Component.literal("Contains one high-variance Cosmic reward.")
                        .withStyle(net.minecraft.ChatFormatting.YELLOW),
                Component.literal("RIGHT-CLICK TO REVEAL")
                        .withStyle(style -> style.withColor(0x55FFFF).withBold(true)),
                Component.literal("LEFT-CLICK AIR TO PREVIEW")
                        .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    @Override public List<ItemStack> previewOutcomes(ServerPlayer player, ItemStack source) {
        return MemoryChestRewards.previewOutcomes();
    }

    @Override public Component previewTitle(ItemStack source) { return Component.literal("Memory Chest"); }

    @Override public InteractionResult use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.FAIL;
        return MemoryChestService.INSTANCE.open(serverPlayer, player.getItemInHand(hand))
                ? InteractionResult.SUCCESS_SERVER : InteractionResult.FAIL;
    }
}
