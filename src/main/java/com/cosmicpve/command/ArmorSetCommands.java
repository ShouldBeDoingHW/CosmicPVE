package com.cosmicpve.command;

import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.data.component.ArmorSetCrystalData;
import com.cosmicpve.data.component.ArmorSetIdentity;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class ArmorSetCommands {
    private ArmorSetCommands() {}

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> create() {
        return Commands.literal("armor").then(Commands.literal("crystal").then(Commands.literal("give")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("set-id", IdentifierArgument.id())
                                .then(Commands.argument("success-rate", IntegerArgumentType.integer(1, 100))
                                        .executes(context -> give(
                                                context.getSource(), EntityArgument.getPlayer(context, "player"),
                                                IdentifierArgument.getId(context, "set-id"),
                                                IntegerArgumentType.getInteger(context, "success-rate"))))))));
    }

    private static int give(net.minecraft.commands.CommandSourceStack source, net.minecraft.server.level.ServerPlayer player,
            net.minecraft.resources.Identifier id, int successRate) {
        var definition = CosmicContent.repository().findArmorSetDefinition(id);
        if (definition.isEmpty()) {
            source.sendFailure(Component.translatable("command.cosmicpve.armor.unknown_set", id));
            return 0;
        }
        ItemStack stack = com.cosmicpve.equipment.armor.ArmorSetCrystals.create(definition.orElseThrow(), successRate);
        player.getInventory().placeItemBackInInventory(stack);
        source.sendSuccess(() -> Component.translatable("command.cosmicpve.armor.given", id, successRate, player.getName()), true);
        return 1;
    }
}
