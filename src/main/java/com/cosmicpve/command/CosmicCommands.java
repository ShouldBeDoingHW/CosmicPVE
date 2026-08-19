package com.cosmicpve.command;

import com.cosmicpve.combat.CosmicCombat;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class CosmicCommands {
    private CosmicCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cosmic")
                .requires(Commands.hasPermission(new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER)))
                .then(Commands.literal("combat")
                        .then(Commands.literal("trace")
                                .then(Commands.literal("on").executes(context -> setTrace(context.getSource(), true)))
                                .then(Commands.literal("off").executes(context -> setTrace(context.getSource(), false)))
                                .then(Commands.literal("last").executes(context -> showLast(context.getSource()))))));
    }

    private static int setTrace(net.minecraft.commands.CommandSourceStack source, boolean enabled)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        CosmicCombat.traces().setEnabled(player.getUUID(), enabled);
        source.sendSuccess(() -> Component.literal("Cosmic combat tracing " + (enabled ? "enabled" : "disabled") + "."), false);
        return 1;
    }

    private static int showLast(net.minecraft.commands.CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        var result = CosmicCombat.traces().last(player.getUUID());
        if (result.isEmpty()) {
            source.sendFailure(Component.literal("No committed damaging hit has been traced yet."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(CosmicCombat.traces().format(result.orElseThrow())), false);
        return 1;
    }
}
