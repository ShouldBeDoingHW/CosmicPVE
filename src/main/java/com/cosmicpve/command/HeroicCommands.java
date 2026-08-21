package com.cosmicpve.command;

import com.cosmicpve.registry.ModItems;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class HeroicCommands {
    private HeroicCommands() {}
    public static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("heroic").then(Commands.literal("crystal").then(Commands.literal("give")
                .then(Commands.argument("player", EntityArgument.player()).executes(context -> {
                    var player = EntityArgument.getPlayer(context, "player");
                    player.getInventory().placeItemBackInInventory(new ItemStack(ModItems.HEROIC_CRYSTAL.get()));
                    context.getSource().sendSuccess(() -> Component.literal("Gave Heroic Crystal."), false);
                    return 1;
                }))));
    }
}
