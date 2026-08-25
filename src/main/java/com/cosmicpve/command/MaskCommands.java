package com.cosmicpve.command;

import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.equipment.mask.MaskItemFactory;
import com.cosmicpve.registry.ModItems;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.ArrayList;
import java.util.HashSet;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;

public final class MaskCommands {
    private MaskCommands() {}
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> create() {
        return Commands.literal("mask")
                .then(Commands.literal("give").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("mask-id", IdentifierArgument.id())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                        CosmicContent.repository().snapshot().maskDefinitions().keySet(), builder))
                                .executes(context -> give(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                        IdentifierArgument.getId(context, "mask-id"))))))
                .then(Commands.literal("give-multi").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("mask-ids", StringArgumentType.greedyString())
                                .executes(context -> giveMulti(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                        StringArgumentType.getString(context, "mask-ids"))))))
                .then(Commands.literal("splicer").then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    EntityArgument.getPlayer(context, "player").getInventory().placeItemBackInInventory(
                                            new net.minecraft.world.item.ItemStack(ModItems.MASK_SPLICER.get()));
                                    return 1;
                                }))));
    }
    private static int give(net.minecraft.commands.CommandSourceStack source, net.minecraft.server.level.ServerPlayer player,
            net.minecraft.resources.Identifier id) {
        if (CosmicContent.repository().findMaskDefinition(id).isEmpty()) { source.sendFailure(Component.literal("Unknown mask: " + id)); return 0; }
        player.getInventory().placeItemBackInInventory(MaskItemFactory.create(id)); return 1;
    }
    private static int giveMulti(net.minecraft.commands.CommandSourceStack source, net.minecraft.server.level.ServerPlayer player,
            String raw) {
        var ids = new ArrayList<net.minecraft.resources.Identifier>();
        for (String value : raw.trim().split("[ ,]+")) {
            var id = net.minecraft.resources.Identifier.tryParse(value);
            if (id == null || CosmicContent.repository().findMaskDefinition(id).isEmpty()) {
                source.sendFailure(Component.literal("Unknown mask: " + value)); return 0;
            }
            ids.add(id);
        }
        if (ids.isEmpty() || ids.size() > 5 || new HashSet<>(ids).size() != ids.size()) {
            source.sendFailure(Component.literal("Supply 1-5 distinct mask IDs.")); return 0;
        }
        player.getInventory().placeItemBackInInventory(MaskItemFactory.create(ids)); return 1;
    }
}
