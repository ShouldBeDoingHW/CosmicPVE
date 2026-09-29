package com.cosmicpve.vkit;

import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.reward.RewardDeliveryService;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/** Shared behavior for each typed V-Kit Crystal variant. */
public final class VKitCrystalItem extends Item {
    public static final int MAX_STACK_SIZE = 1;

    private final VKitProgressionService progression = new VKitProgressionService();
    private final VKitEquipmentGenerator generator = new VKitEquipmentGenerator();
    private final RewardDeliveryService delivery = new RewardDeliveryService();

    public VKitCrystalItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public Component getName(ItemStack stack) {
        VKitDefinition definition = definition(stack);
        if (definition == null) return super.getName(stack);
        return Component.literal("V-Kit Crystal ").withStyle(style -> style.withColor(0xFFFFFF).withBold(true))
                .append(Component.literal("(" + definition.displayName() + ")").withStyle(style -> style
                        .withColor(definition.color()).withBold(true).withItalic(true).withUnderlined(true)));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> consumer, TooltipFlag flag) {
        VKitDefinition definition = definition(stack);
        if (definition != null) {
            consumer.accept(Component.translatable(definition.flavorTranslation() + ".lore")
                    .withStyle(style -> style.withColor(definition.color()).withItalic(true)));
            consumer.accept(Component.empty());
            consumer.accept(flavor(definition));
        }
    }

    public static Component flavor(VKitDefinition definition) {
        var baseStyle = net.minecraft.network.chat.Style.EMPTY.withColor(definition.color()).withItalic(true);
        return Component.translatable(definition.flavorTranslation() + ".prefix").withStyle(baseStyle)
                .append(Component.literal(definition.displayName() + " Vkit").withStyle(baseStyle.withUnderlined(true)))
                .append(Component.translatable(definition.flavorTranslation() + ".suffix").withStyle(baseStyle));
    }

    @Override
    public InteractionResult use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.FAIL;
        ItemStack held = player.getItemInHand(hand);
        VKitDefinition definition = definition(held);
        if (definition == null) return InteractionResult.FAIL;
        int previousLevel = progression.level(serverPlayer, definition);
        int rollLevel = progression.nextRollLevel(serverPlayer, definition);
        ItemStack reward;
        try {
            reward = generator.roll(definition, rollLevel,
                    level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT), serverPlayer.getRandom());
        } catch (RuntimeException invalidConfiguration) {
            serverPlayer.sendSystemMessage(Component.literal("V-Kit reward generation failed; crystal was not consumed."));
            return InteractionResult.FAIL;
        }
        InteractionResult.Success result = commitRedemption(held, rollLevel, reward,
                committedLevel -> progression.set(serverPlayer, definition, committedLevel),
                committedReward -> delivery.deliver(serverPlayer, List.of(committedReward)),
                () -> VKitCrystalFeedback.playSuccess(serverPlayer));
        serverPlayer.sendSystemMessage(redemptionMessage(definition, previousLevel, rollLevel));
        return result;
    }

    static Component redemptionMessage(VKitDefinition definition, int previousLevel, int rollLevel) {
        if (previousLevel >= VKitEquipmentGenerator.MAX_KIT_LEVEL) {
            return Component.literal("Your " + definition.displayName()
                    + " Vkit is already level 10. Nice!").withStyle(style -> style.withColor(definition.color()));
        }
        return Component.literal(definition.displayName() + " V-Kit is now level "
                + VKitEquipmentGenerator.roman(rollLevel) + ".").withStyle(style -> style.withColor(definition.color()));
    }

    static InteractionResult.Success commitRedemption(ItemStack crystal, int rollLevel, ItemStack reward,
            IntConsumer progressionUpdate, Consumer<ItemStack> rewardDelivery, Runnable successfulFeedback) {
        progressionUpdate.accept(rollLevel);
        crystal.shrink(1);
        InteractionResult.Success result;
        if (crystal.isEmpty()) {
            // ItemStack.use always writes a successful use's transformed stack back to the used
            // hand. Make the generated equipment that authoritative replacement so it cannot be
            // erased when the selected slot was the first available inventory destination.
            result = InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(reward);
        } else {
            // Compatibility for synthetic/pre-fix stacked crystals: keep the remaining crystals
            // in hand and deliver this one reward through the ordinary safe overflow path.
            rewardDelivery.accept(reward);
            result = InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(crystal);
        }
        successfulFeedback.run();
        return result;
    }

    private static VKitDefinition definition(ItemStack stack) {
        var data = stack.get(ModDataComponents.VKIT_CRYSTAL.get());
        if (data == null || !data.isCurrent()) return null;
        return VKitDefinition.find(data.kitId()).orElse(null);
    }
}
