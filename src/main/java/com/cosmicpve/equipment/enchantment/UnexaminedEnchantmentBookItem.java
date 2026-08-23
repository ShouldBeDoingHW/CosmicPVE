package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.registry.ModDataComponents;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;

public final class UnexaminedEnchantmentBookItem extends Item {
    public static final boolean FORCE_GLINT = true;

    public UnexaminedEnchantmentBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return FORCE_GLINT;
    }

    @Override
    public Component getName(ItemStack stack) {
        var data = stack.get(ModDataComponents.UNEXAMINED_BOOK.get());
        if (data == null) return super.getName(stack);
        return Component.translatable("item.cosmicpve.unexamined_enchantment_book.named",
                Component.translatable("cosmic_tier.cosmicpve." + data.tier().serializedName()))
                .withColor(data.tier().tooltipColor());
    }

    @Override
    public InteractionResult use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }
        ItemStack held = player.getItemInHand(hand);
        var data = held.get(ModDataComponents.UNEXAMINED_BOOK.get());
        if (data == null) {
            player.displayClientMessage(Component.translatable("message.cosmicpve.unexamined.invalid"), true);
            return InteractionResult.FAIL;
        }
        var registry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        List<CosmicEnchantmentSpec> available = CosmicEnchantmentSpecs.ALL.stream()
                .filter(spec -> registry.get(spec.id()).isPresent())
                .toList();
        var result = UnexaminedBooks.openingService().roll(data.tier(), available, serverLevel.getRandom(), serverPlayer);
        if (result.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.cosmicpve.unexamined.empty"), true);
            return InteractionResult.FAIL;
        }

        ItemStack revealed = UnexaminedBooks.revealed(result.orElseThrow());
        ItemStack heldAfter = UnexaminedRewardDelivery.deliver(
                held, revealed, player.getInventory()::placeItemBackInInventory);
        launchFeedback(serverLevel, serverPlayer);
        var rolled = result.orElseThrow();
        player.displayClientMessage(Component.translatable("message.cosmicpve.unexamined.revealed",
                rolled.enchantment().displayName(),
                Component.translatable("enchantment.level." + rolled.level())), true);
        return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(heldAfter);
    }

    public static List<Component> lore() {
        return List.of(
                Component.translatable("tooltip.cosmicpve.unexamined.purpose").withStyle(ChatFormatting.YELLOW),
                Component.translatable("tooltip.cosmicpve.unexamined.instruction").withStyle(ChatFormatting.GRAY));
    }

    static ItemStack fireworkStack() {
        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
        firework.set(DataComponents.FIREWORKS, new Fireworks(1, List.of()));
        return firework;
    }

    private static void launchFeedback(ServerLevel level, ServerPlayer player) {
        level.addFreshEntity(new FireworkRocketEntity(level, player,
                player.getX(), player.getY() + 0.25, player.getZ(), fireworkStack()));
    }
}
