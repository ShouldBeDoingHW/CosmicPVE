package com.cosmicpve.reward.lootbox;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class AnimatedLootboxItem extends Item {
    private final Kind kind;
    public AnimatedLootboxItem(Properties properties, Kind kind) { super(properties); this.kind = kind; }
    public Kind kind() { return kind; }
    @Override public boolean isFoil(ItemStack stack) { return true; }
    @Override public Component getName(ItemStack stack) {
        return displayName(kind);
    }
    static Component displayName(Kind kind) {
        return switch (kind) {
            case SECRET_WEAPON_CACHE -> Component.literal("Secret Weapon Cache").withStyle(style -> style.withColor(0x00AAAA).withBold(true));
            case COSMIC_ENCHANTMENT_TABLE -> Component.literal("Cosmic Enchantment Table")
                    .withStyle(style -> style.withColor(0x32045C).withBold(true));
            case HEROIC_COSMIC_ENCHANTMENT_TABLE -> Component.literal("Heroic")
                    .withStyle(style -> style.withColor(com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier.HEROIC.tooltipColor()).withBold(true))
                    .append(Component.literal(" Cosmic Enchantment Table")
                            .withStyle(style -> style.withColor(0x32045C).withBold(true)));
            case ADMIN_ABUSE -> adminName();
        };
    }
    public static List<Component> secretWeaponCacheLore() {
        return List.of(Component.literal("The most devastating weapons known to life, all inside a single box...")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
    }
    @Override public InteractionResult use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return InteractionResult.FAIL;
        return Step8ELootboxService.INSTANCE.open(serverPlayer, player.getItemInHand(hand), kind)
                ? InteractionResult.SUCCESS_SERVER : InteractionResult.FAIL;
    }
    static MutableComponent adminName() {
        MutableComponent result = Component.empty();
        String name = "Admin Abuse";
        int letter = 0;
        for (int index = 0; index < name.length(); index++) {
            char value = name.charAt(index);
            int color = letter % 2 == 0 ? 0xD41432 : 0x8F1022;
            result.append(Component.literal(Character.toString(value)).withStyle(style -> style.withColor(color)
                    .withBold(true).withItalic(true).withStrikethrough(true)));
            if (!Character.isWhitespace(value)) letter++;
        }
        return result;
    }
    public enum Kind { SECRET_WEAPON_CACHE, COSMIC_ENCHANTMENT_TABLE, HEROIC_COSMIC_ENCHANTMENT_TABLE, ADMIN_ABUSE }
}
