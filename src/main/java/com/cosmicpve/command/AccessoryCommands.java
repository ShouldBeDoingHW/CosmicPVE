package com.cosmicpve.command;

import com.cosmicpve.data.component.AccessorySocketData;
import com.cosmicpve.data.component.AccessorySlot;
import com.cosmicpve.equipment.accessory.AmuletDefinition;
import com.cosmicpve.equipment.accessory.AmuletItemFactory;
import com.cosmicpve.equipment.accessory.BeltDefinition;
import com.cosmicpve.equipment.accessory.BeltItemFactory;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class AccessoryCommands {
    private AccessoryCommands() {}
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> create() {
        var give = Commands.literal("give")
                .then(Commands.literal("socket").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("success", IntegerArgumentType.integer(1, 100)).executes(context -> {
                            var stack = new ItemStack(ModItems.AMULET_SOCKET.get());
                            int rate = IntegerArgumentType.getInteger(context, "success");
                            stack.set(ModDataComponents.ACCESSORY_SOCKET.get(), new AccessorySocketData(
                                    AccessorySocketData.DATA_VERSION, AccessorySlot.AMULET, rate));
                            return give(context.getSource(), EntityArgument.getPlayer(context, "player"), stack,
                                    "Amulet Socket (" + rate + "% Success)");
                        }))))
                .then(Commands.literal("belt_socket").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("success", IntegerArgumentType.integer(1, 100)).executes(context -> {
                            int rate = IntegerArgumentType.getInteger(context, "success");
                            var stack = new ItemStack(ModItems.BELT_SOCKET.get());
                            stack.set(ModDataComponents.ACCESSORY_SOCKET.get(), new AccessorySocketData(
                                    AccessorySocketData.DATA_VERSION, AccessorySlot.BELT, rate));
                            return give(context.getSource(), EntityArgument.getPlayer(context, "player"), stack,
                                    "Belt Socket (" + rate + "% Success)");
                        }))))
                .then(Commands.literal("omni_socket").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("success", IntegerArgumentType.integer(1, 100)).executes(context -> {
                            int rate = IntegerArgumentType.getInteger(context, "success");
                            var stack = new ItemStack(ModItems.OMNI_SOCKET.get());
                            stack.set(ModDataComponents.OMNI_SOCKET_SUCCESS.get(), rate);
                            return give(context.getSource(), EntityArgument.getPlayer(context, "player"), stack,
                                    "Omni Socket (" + rate + "% Success)");
                        }))));
        for (var definition : AmuletDefinition.values()) {
            give.then(Commands.literal(definition.id().getPath()).then(Commands.argument("player", EntityArgument.player())
                    .executes(context -> give(context.getSource(), EntityArgument.getPlayer(context, "player"),
                            AmuletItemFactory.create(definition), definition.displayName()))));
        }
        for (var definition : BeltDefinition.values()) {
            give.then(Commands.literal(definition.id().getPath()).then(Commands.argument("player", EntityArgument.player())
                    .executes(context -> give(context.getSource(), EntityArgument.getPlayer(context, "player"),
                            BeltItemFactory.create(definition), definition.displayName()))));
        }
        return Commands.literal("accessory").then(give);
    }
    private static int give(net.minecraft.commands.CommandSourceStack source,
            net.minecraft.server.level.ServerPlayer player, ItemStack stack, String name) {
        player.getInventory().placeItemBackInInventory(stack);
        source.sendSuccess(() -> Component.literal("Gave " + player.getName().getString() + " " + name + "."), false);
        return 1;
    }
}
