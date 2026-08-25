package com.cosmicpve.command;

import com.cosmicpve.equipment.mask.MaskLimitSavedData;
import com.cosmicpve.equipment.mask.MaskLimitService;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class MaskLimitCommands {
    private MaskLimitCommands() {}
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("masklimit")
                .requires(Commands.hasPermission(new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER)))
                .executes(context -> report(context.getSource()))
                .then(Commands.argument("limit", IntegerArgumentType.integer(
                        MaskLimitSavedData.MIN_LIMIT, MaskLimitSavedData.MAX_LIMIT))
                        .executes(context -> set(context.getSource(), IntegerArgumentType.getInteger(context, "limit")))));
    }
    private static int report(net.minecraft.commands.CommandSourceStack source) {
        int limit = new MaskLimitService().get(source.getServer());
        source.sendSuccess(() -> Component.literal("Multi-Mask creation limit: " + limit), false);
        return limit;
    }
    private static int set(net.minecraft.commands.CommandSourceStack source, int limit) {
        new MaskLimitService().set(source.getServer(), limit);
        source.sendSuccess(() -> Component.literal("Set Multi-Mask creation limit to " + limit + "."), true);
        return limit;
    }
}
