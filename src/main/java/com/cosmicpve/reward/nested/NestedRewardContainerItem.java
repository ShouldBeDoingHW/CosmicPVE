package com.cosmicpve.reward.nested;

import com.cosmicpve.registry.ModAttachments;
import com.cosmicpve.reward.animation.SingleRewardAnimationService;
import com.cosmicpve.reward.preview.LootPreviewProvider;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class NestedRewardContainerItem extends Item implements LootPreviewProvider {
    public enum Kind { TRIALS_CREATION_KIT, COSMIC_SWAG_BAG }
    private static final int[] SWAG_COLORS = {0x20F5EF, 0xFFFFFF, 0xE994F2};
    private final Kind kind;

    public NestedRewardContainerItem(Properties properties, Kind kind) { super(properties); this.kind = kind; }
    public Kind kind() { return kind; }
    @Override public boolean isFoil(ItemStack stack) { return true; }
    @Override public Component getName(ItemStack stack) { return name(kind); }

    public static Component name(Kind kind) {
        if (kind == Kind.TRIALS_CREATION_KIT) return Component.literal("Trials Creation Kit")
                .withStyle(style -> style.withColor(0xFA9E05).withBold(true));
        MutableComponent name = Component.empty();
        String label = "Cosmic Swag Bag";
        for (int index = 0; index < label.length(); index++) {
            int color = SWAG_COLORS[index % SWAG_COLORS.length];
            name.append(Component.literal(Character.toString(label.charAt(index)))
                    .withStyle(style -> style.withColor(color).withBold(true).withItalic(true)));
        }
        return name;
    }

    public static List<Component> lore(Kind kind) {
        return kind == Kind.TRIALS_CREATION_KIT
                ? List.of(Component.literal("The ultimate stash of goodies needed to create the most powerful trial portal in existence. Right click to open!")
                        .withStyle(style -> style.withColor(0xFA9E05).withItalic(true)))
                : List.of(Component.literal("Contains 3 spicy items to give you a little more swagger in your life. Click to open!")
                        .withStyle(style -> style.withColor(0xFFFFFF).withItalic(true)));
    }

    private List<WeightedNestedRewards.Row> rows() {
        return kind == Kind.TRIALS_CREATION_KIT ? TrialsCreationKitRewards.ROWS : CosmicSwagBagRewards.ROWS;
    }
    @Override public List<ItemStack> previewOutcomes(ServerPlayer player, ItemStack source) {
        return kind == Kind.TRIALS_CREATION_KIT ? TrialsCreationKitRewards.preview() : CosmicSwagBagRewards.preview();
    }
    @Override public Component previewTitle(ItemStack source) {
        return Component.literal(kind == Kind.TRIALS_CREATION_KIT ? "Trials Creation Kit" : "Cosmic Swag Bag");
    }
    @Override public InteractionResult use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.FAIL;
        ItemStack source = player.getItemInHand(hand);
        if (source.getItem() != this || SingleRewardAnimationService.INSTANCE.active(serverPlayer)
                || serverPlayer.getData(ModAttachments.LOOT_ANIMATION).valid()) return InteractionResult.FAIL;
        var catalog = rows();
        int count = kind == Kind.TRIALS_CREATION_KIT ? 5 : 3;
        var selected = WeightedNestedRewards.selectRows(catalog, count,
                kind == Kind.TRIALS_CREATION_KIT, serverPlayer.getRandom());
        var finals = WeightedNestedRewards.create(selected, serverPlayer.getRandom());
        return SingleRewardAnimationService.INSTANCE.open(serverPlayer, finals,
                WeightedNestedRewards.cosmeticFrames(catalog), () -> source.shrink(1))
                ? InteractionResult.SUCCESS_SERVER : InteractionResult.FAIL;
    }
}
