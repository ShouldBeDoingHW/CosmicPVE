package com.cosmicpve.cosmiccrate;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class CosmicCrateItem extends Item {
    private final CosmicCrateSeason season;
    public CosmicCrateItem(Properties properties, CosmicCrateSeason season) { super(properties); this.season = season; }
    public CosmicCrateSeason season() { return season; }
    @Override public Component getName(ItemStack stack) { return header(season); }
    @Override public InteractionResult use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }
    public static Component header(CosmicCrateSeason season) {
        MutableComponent result = Component.literal("*** ").withColor(0xFFFFFF);
        result.append(Component.literal("COSMIC CRATE:").withStyle(style -> style.withColor(0xFFFFFF).withBold(true)));
        result.append(Component.literal(" " + season.displayName() + " ***")
                .withStyle(style -> style.withColor(season.color())));
        return result;
    }
    public static List<Component> lore(CosmicCrateSeason season) {
        var lines = new ArrayList<Component>();
        lines.add(Component.empty());
        lines.add(Component.literal("TREASURE ITEMS").withStyle(style -> style.withColor(0xFFD24A).withBold(true)));
        season.treasure().forEach(entry -> lines.add(Component.literal("* " + entry).withColor(0xFFD24A)));
        lines.add(Component.empty());
        lines.add(Component.literal("BONUS ITEMS (1)").withStyle(style -> style.withColor(0x559BFF).withBold(true)));
        season.bonus().forEach(entry -> lines.add(Component.literal("* " + entry).withColor(0x559BFF)));
        lines.add(Component.empty());
        lines.add(Component.literal("Ensure you have a lot of free space!").withColor(0xFF5555));
        return List.copyOf(lines);
    }
}
