package com.cosmicpve.command;

import com.cosmicpve.economy.Banknotes;
import com.cosmicpve.economy.MoneyAmount;
import com.cosmicpve.economy.MoneyService;
import com.cosmicpve.registry.ModItems;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class EconomyCommands {
    private static final MoneyService MONEY = new MoneyService();
    private EconomyCommands() {}

    public static void registerPublic(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("bal").executes(c -> balance(c.getSource())));
        event.getDispatcher().register(Commands.literal("balance").executes(c -> balance(c.getSource())));
        event.getDispatcher().register(Commands.literal("withdraw")
                .then(Commands.argument("amount", StringArgumentType.word())
                        .executes(c -> withdraw(c.getSource(), StringArgumentType.getString(c, "amount")))));
    }

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> create() {
        var player = Commands.argument("player", EntityArgument.player())
                .executes(c -> inspect(c.getSource(), EntityArgument.getPlayer(c, "player")))
                .then(Commands.literal("set").then(amountArg((source, target, amount) -> MONEY.set(target, amount))))
                .then(Commands.literal("add").then(amountArg((source, target, amount) -> MONEY.add(target, amount))))
                .then(Commands.literal("subtract").then(amountArg((source, target, amount) -> MONEY.subtract(target, amount))));
        var note = Commands.literal("banknote").then(Commands.literal("give")
                .then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("amount", StringArgumentType.word())
                        .executes(c -> giveNote(c.getSource(), EntityArgument.getPlayer(c, "player"),
                                StringArgumentType.getString(c, "amount"))))));
        var repair = Commands.literal("repair-scroll").then(Commands.literal("give")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(c -> giveRepair(EntityArgument.getPlayer(c, "player"), 1))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                .executes(c -> giveRepair(EntityArgument.getPlayer(c, "player"),
                                        IntegerArgumentType.getInteger(c, "count"))))));
        return Commands.literal("money").then(player).then(note).then(repair);
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> amountArg(Operation op) {
        return Commands.argument("amount", StringArgumentType.word()).executes(c -> {
            var target = EntityArgument.getPlayer(c, "player");
            long amount = parse(c.getSource(), StringArgumentType.getString(c, "amount"));
            if (amount <= 0 || !op.apply(c.getSource(), target, amount)) {
                c.getSource().sendFailure(Component.translatable("command.cosmicpve.money.rejected")); return 0;
            }
            return inspect(c.getSource(), target);
        });
    }

    private static int balance(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        source.sendSuccess(() -> Component.translatable("command.cosmicpve.balance", MoneyAmount.format(MONEY.balance(player))), false);
        return 1;
    }

    private static int withdraw(CommandSourceStack source, String raw) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        long amount = parse(source, raw);
        if (amount <= 0 || amount > MONEY.balance(player)) {
            source.sendFailure(Component.translatable("command.cosmicpve.withdraw.insufficient")); return 0;
        }
        var note = Banknotes.create(amount);
        if (note.isEmpty() || !MONEY.subtract(player, amount)) return 0;
        player.getInventory().placeItemBackInInventory(note);
        source.sendSuccess(() -> Component.translatable("command.cosmicpve.withdraw.success", MoneyAmount.format(amount)), false);
        return 1;
    }

    private static int inspect(CommandSourceStack source, net.minecraft.server.level.ServerPlayer target) {
        source.sendSuccess(() -> Component.literal(target.getScoreboardName() + ": "
                + MoneyAmount.format(MONEY.balance(target))), false); return 1;
    }
    private static int giveNote(CommandSourceStack source, net.minecraft.server.level.ServerPlayer target, String raw) {
        long amount = parse(source, raw); if (amount <= 0) return 0;
        target.getInventory().placeItemBackInInventory(Banknotes.create(amount)); return 1;
    }
    private static int giveRepair(net.minecraft.server.level.ServerPlayer target, int count) {
        target.getInventory().placeItemBackInInventory(new net.minecraft.world.item.ItemStack(ModItems.REPAIR_SCROLL.get(), count));
        return count;
    }
    private static long parse(CommandSourceStack source, String raw) {
        try { return MoneyAmount.parseCents(raw); }
        catch (IllegalArgumentException exception) { source.sendFailure(Component.translatable("command.cosmicpve.money.invalid")); return -1; }
    }
    @FunctionalInterface private interface Operation {
        boolean apply(CommandSourceStack source, net.minecraft.server.level.ServerPlayer target, long amount);
    }
}
