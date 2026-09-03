package com.cosmicpve.command;

import com.cosmicpve.economy.Banknotes;
import com.cosmicpve.economy.MoneyAmount;
import com.cosmicpve.economy.MoneyService;
import com.cosmicpve.economy.SellService;
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
    private static final SellService SELL = new SellService();
    private EconomyCommands() {}

    public static void registerPublic(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("bal").executes(c -> balance(c.getSource())));
        event.getDispatcher().register(Commands.literal("balance").executes(c -> balance(c.getSource())));
        event.getDispatcher().register(Commands.literal("withdraw")
                .then(Commands.argument("amount", StringArgumentType.word())
                        .executes(c -> withdraw(c.getSource(), StringArgumentType.getString(c, "amount")))));
        event.getDispatcher().register(Commands.literal("sell")
                .then(Commands.literal("hand").executes(c -> sell(c.getSource(), true)))
                .then(Commands.literal("all").executes(c -> sell(c.getSource(), false))));
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
        source.sendSuccess(() -> balanceMessage(MONEY.balance(player)), false);
        return 1;
    }

    static Component balanceMessage(long cents) {
        return Component.translatable("command.cosmicpve.balance", MoneyAmount.format(cents))
                .withStyle(style -> style.withColor(0x55FF55).withBold(true));
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
    private static int sell(CommandSourceStack source, boolean hand)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var result = hand ? SELL.sellHand(source.getPlayerOrException()) : SELL.sellAll(source.getPlayerOrException());
        if (!result.succeeded()) {
            String text = switch (result.status()) {
                case INVALID_HAND -> "That item cannot be sold.";
                case NONE_MATCHING -> "You have none of that item to sell.";
                case NOTHING_TO_SELL -> "You have nothing to sell.";
                case OVERFLOW -> "That sale could not be completed safely.";
                case SUCCESS -> throw new IllegalStateException();
            };
            source.sendFailure(Component.literal(text).withColor(0xFF5555));
            return 0;
        }
        if (hand) {
            var line = result.lines().getFirst();
            source.sendSuccess(() -> Component.literal("You sold ").withColor(0xAAAAAA)
                    .append(Component.literal(Integer.toString(line.quantity()))
                            .withStyle(style -> style.withColor(0xFFFFFF).withBold(true)))
                    .append(Component.literal(" ").append(new net.minecraft.world.item.ItemStack(line.item()).getHoverName()))
                    .append(Component.literal("! ").withColor(0xAAAAAA))
                    .append(Component.literal(MoneyAmount.format(result.totalCents()))
                            .withStyle(style -> style.withColor(0x55FF55).withBold(true)))
                    .append(Component.literal(" has been added to your account!").withColor(0xAAAAAA)), false);
        } else {
            source.sendSuccess(() -> Component.literal("SOLD ITEMS").withStyle(style -> style.withColor(0xFFAA00).withBold(true)), false);
            for (var line : result.lines()) source.sendSuccess(() -> Component.literal(line.quantity() + " × ")
                    .withStyle(style -> style.withColor(0xFFFFFF).withBold(true))
                    .append(new net.minecraft.world.item.ItemStack(line.item()).getHoverName())
                    .append(Component.literal(" — ").withColor(0xAAAAAA))
                    .append(Component.literal(MoneyAmount.format(line.subtotalCents()))
                            .withStyle(style -> style.withColor(0x55FF55).withBold(true))), false);
            source.sendSuccess(() -> Component.literal("TOTAL: " + MoneyAmount.format(result.totalCents()))
                    .withStyle(style -> style.withColor(0x55FF55).withBold(true)), false);
        }
        return 1;
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
