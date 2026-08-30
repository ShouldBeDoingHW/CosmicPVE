package com.cosmicpve.command;

import com.cosmicpve.personalvault.PersonalVaultMenu;
import com.cosmicpve.personalvault.PersonalVaultRuntime;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

public final class PersonalVaultCommands {
    private PersonalVaultCommands() {}

    public static void registerPublic(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("pv")
                .then(Commands.argument("number", LongArgumentType.longArg(1L))
                        .executes(context -> open(context.getSource().getPlayerOrException(),
                                LongArgumentType.getLong(context, "number")))));
    }

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> create() {
        return Commands.literal("pv")
                .then(Commands.literal("inspect").then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> inspect(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("unlocks")
                        .then(Commands.literal("set").then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("rows", LongArgumentType.longArg(0L))
                                        .executes(context -> setRows(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                                LongArgumentType.getLong(context, "rows"), true)))))
                        .then(Commands.literal("add").then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("rows", LongArgumentType.longArg(1L))
                                        .executes(context -> setRows(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                                LongArgumentType.getLong(context, "rows"), false))))))
                .then(Commands.literal("combat-tag")
                        .then(Commands.literal("inspect").then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> inspectTag(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                        .then(Commands.literal("clear").then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> clearTag(context.getSource(), EntityArgument.getPlayer(context, "player"))))));
    }

    static int open(ServerPlayer player, long number) {
        var access = PersonalVaultRuntime.access().evaluate(player);
        if (!access.allowed()) { player.sendSystemMessage(Component.literal(access.message())); return 0; }
        int rows = PersonalVaultRuntime.vaults().rowsUnlocked(player, number);
        if (rows == 0) { player.sendSystemMessage(Component.translatable("message.cosmicpve.personal_vault.locked", number)); return 0; }
        var opened = player.openMenu(new SimpleMenuProvider(
                (id, inventory, ignored) -> PersonalVaultMenu.server(id, inventory, number, rows),
                Component.translatable("container.cosmicpve.personal_vault", number)),
                buffer -> { buffer.writeVarLong(number); buffer.writeByte(rows); });
        return opened.isPresent() ? 1 : 0;
    }

    private static int inspect(net.minecraft.commands.CommandSourceStack source, ServerPlayer player) {
        var data = PersonalVaultRuntime.vaults().data(player);
        source.sendSuccess(() -> Component.literal(player.getName().getString() + ": unlocked_rows="
                + data.totalUnlockedRows() + ", stored_vaults=" + data.vaults().size()
                + ", combat_tag_ticks=" + PersonalVaultRuntime.combatTags().remainingTicks(player)), false);
        return 1;
    }

    private static int setRows(net.minecraft.commands.CommandSourceStack source, ServerPlayer player, long rows, boolean absolute) {
        long value = rows;
        if (!absolute) {
            try { value = Math.addExact(PersonalVaultRuntime.vaults().totalUnlockedRows(player), rows); }
            catch (ArithmeticException overflow) { source.sendFailure(Component.literal("Unlock row count overflow.")); return 0; }
        }
        if (!PersonalVaultRuntime.vaults().setUnlockedRows(player, value)) return 0;
        long finalValue = value;
        source.sendSuccess(() -> Component.literal("Set " + player.getName().getString()
                + " Personal Vault rows to " + finalValue + "."), true);
        return 1;
    }

    private static int inspectTag(net.minecraft.commands.CommandSourceStack source, ServerPlayer player) {
        long remaining = PersonalVaultRuntime.combatTags().remainingTicks(player);
        source.sendSuccess(() -> Component.literal(player.getName().getString() + " has " + remaining
                + " Personal Vault combat-tag ticks remaining."), false);
        return 1;
    }
    private static int clearTag(net.minecraft.commands.CommandSourceStack source, ServerPlayer player) {
        PersonalVaultRuntime.combatTags().clear(player);
        source.sendSuccess(() -> Component.literal("Cleared " + player.getName().getString()
                + " Personal Vault combat tag."), true);
        return 1;
    }
}
