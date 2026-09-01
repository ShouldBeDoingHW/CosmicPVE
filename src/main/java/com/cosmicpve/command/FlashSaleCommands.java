package com.cosmicpve.command;

import com.cosmicpve.economy.flashsale.FlashSaleCatalog;
import com.cosmicpve.economy.flashsale.FlashSalePriceTier;
import com.cosmicpve.economy.flashsale.FlashSaleRuntime;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class FlashSaleCommands {
    private FlashSaleCommands() {}

    public static void registerPublic(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("flashsale")
                .then(Commands.literal("buy").executes(context -> buy(context.getSource())))
                .then(Commands.literal("preview").executes(context -> preview(context.getSource()))));
        event.getDispatcher().register(Commands.literal("buy").executes(context -> buy(context.getSource())));
    }

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> create() {
        var force = Commands.literal("force")
                .then(Commands.argument("entry", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                FlashSaleCatalog.productionRows().stream().map(entry -> entry.id()), builder))
                        .then(Commands.argument("tier", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        java.util.List.of("low", "medium", "high"), builder))
                                .executes(context -> force(context.getSource(),
                                        StringArgumentType.getString(context, "entry"),
                                        StringArgumentType.getString(context, "tier")))));
        return Commands.literal("flashsale")
                .then(Commands.literal("status").executes(context -> status(context.getSource())))
                .then(Commands.literal("random").executes(context -> random(context.getSource())))
                .then(Commands.literal("close").executes(context -> close(context.getSource())))
                .then(Commands.literal("reset-purchasers").executes(context -> reset(context.getSource())))
                .then(force);
    }

    private static int buy(net.minecraft.commands.CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return FlashSaleRuntime.service().buy(source.getPlayerOrException())
                == com.cosmicpve.economy.flashsale.FlashSalePurchaseTransaction.Outcome.SUCCESS ? 1 : 0;
    }
    private static int preview(net.minecraft.commands.CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return FlashSaleRuntime.service().preview(source.getPlayerOrException()) ? 1 : 0;
    }
    private static int status(net.minecraft.commands.CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(FlashSaleRuntime.service().status(source.getServer())), false); return 1;
    }
    private static int random(net.minecraft.commands.CommandSourceStack source) {
        return FlashSaleRuntime.service().startRandom(source.getServer()) ? 1 : 0;
    }
    private static int close(net.minecraft.commands.CommandSourceStack source) {
        return FlashSaleRuntime.service().close(source.getServer()) ? 1 : 0;
    }
    private static int reset(net.minecraft.commands.CommandSourceStack source) {
        return FlashSaleRuntime.service().resetPurchasers(source.getServer()) ? 1 : 0;
    }
    private static int force(net.minecraft.commands.CommandSourceStack source, String entryId, String tierName) {
        var entry = FlashSaleCatalog.find(entryId);
        if (entry.isEmpty() || !entry.orElseThrow().productionSelectable()) {
            source.sendFailure(Component.literal("Unknown or deferred Flash Sale entry: " + entryId)); return 0;
        }
        FlashSalePriceTier tier;
        try { tier = FlashSalePriceTier.valueOf(tierName.toUpperCase(java.util.Locale.ROOT)); }
        catch (IllegalArgumentException invalid) { source.sendFailure(Component.literal("Price tier must be low, medium, or high.")); return 0; }
        return FlashSaleRuntime.service().start(source.getServer(), entry.orElseThrow(), tier) ? 1 : 0;
    }
}
