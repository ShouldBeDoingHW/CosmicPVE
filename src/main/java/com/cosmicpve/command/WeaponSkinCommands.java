package com.cosmicpve.command;

import com.cosmicpve.equipment.skin.WeaponSkinDefinitions;
import com.cosmicpve.equipment.skin.WeaponSkinItemFactory;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;

public final class WeaponSkinCommands {
    private WeaponSkinCommands() {}

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> create() {
        return Commands.literal("skin").then(Commands.literal("give")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("skin-id", IdentifierArgument.id())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                        WeaponSkinDefinitions.ids(), builder))
                                .executes(context -> give(
                                        context.getSource(), EntityArgument.getPlayer(context, "player"),
                                        IdentifierArgument.getId(context, "skin-id"))))));
    }

    private static int give(
            net.minecraft.commands.CommandSourceStack source,
            net.minecraft.server.level.ServerPlayer player,
            net.minecraft.resources.Identifier id) {
        if (WeaponSkinDefinitions.find(id).isEmpty()) {
            source.sendFailure(Component.translatable("command.cosmicpve.skin.unknown", id.toString()));
            return 0;
        }
        player.getInventory().placeItemBackInInventory(WeaponSkinItemFactory.create(id));
        source.sendSuccess(() -> Component.translatable("command.cosmicpve.skin.given", id.toString()), false);
        return 1;
    }
}
